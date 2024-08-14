/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.ileQuery

import connectors.CustomsDeclareExportsMovementsConnector
import connectors.exchanges.IleQueryExchange
import controllers.actions.AuthAction
import controllers.ileQuery.routes.IleQueryController
import forms.IleQueryForm.form
import forms.UcrType._
import handlers.ErrorHandler
import models.UcrBlock
import models.cache.{Cache, IleQuery}
import models.notifications.queries.IleQueryResponseExchangeData.{SuccessfulResponseExchangeData, UcrNotFoundResponseExchangeData}
import models.notifications.queries.{DucrInfo, IleQueryResponseExchange, MucrInfo}
import models.requests.AuthenticatedRequest
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._
import repositories.{CacheRepository, IleQueryRepository}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.validators.forms.FieldValidator._
import views.html._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IleQueryController @Inject() (
  authenticate: AuthAction,
  mcc: MessagesControllerComponents,
  errorHandler: ErrorHandler,
  cacheRepository: CacheRepository,
  ileQueryRepository: IleQueryRepository,
  connector: CustomsDeclareExportsMovementsConnector,
  ileQueryPage: ile_query,
  loadingScreenPage: loading_screen,
  ileQueryDucrResponsePage: ile_query_ducr_response,
  ileQueryMucrResponsePage: ile_query_mucr_response,
  consignmentNotFound: consignment_not_found_page,
  timeoutPage: ile_query_timeout
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  private val logger = Logger(this.getClass)

  def getConsignmentData(ucr: String): Action[AnyContent] = authenticate.async { implicit request =>
    ileQueryRepository.findBySessionIdAndUcr(retrieveSessionId, ucr).flatMap {
      case Some(query) => checkForNotifications(query)
      case None        => sendIleQuery(ucr)
    }
  }

  private def checkForNotifications(query: IleQuery)(implicit request: AuthenticatedRequest[AnyContent]): Future[Result] =
    connector.fetchQueryNotifications(query.conversationId, request.eori).flatMap { response =>
      response.status match {

        case OK =>
          val queryResponse = Json.parse(response.body).as[Seq[IleQueryResponseExchange]]
          (queryResponse: @unchecked) match {
            case Seq() => Future.successful(loadingPageResult)
            case response +: _ =>
              ileQueryRepository.removeByConversationId(query.conversationId).flatMap { _ =>
                processQueryResults(response)
              }
          }

        case FAILED_DEPENDENCY =>
          logger.warn(s"ILE Query for ${query.ucr}, Conversation ID: [${query.conversationId}] timed out")
          ileQueryRepository.removeByConversationId(query.conversationId).map { _ =>
            Ok(timeoutPage(query.ucr))
          }

        case _ =>
          logger.warn(s"Movements backend returned status: ${response.status}")
          ileQueryRepository.removeByConversationId(query.conversationId).map(_ => errorHandler.internalServerError)
      }
    }

  private def loadingPageResult(implicit request: Request[AnyContent]) =
    Ok(loadingScreenPage()).withHeaders("refresh" -> "5")

  private def processQueryResults(queryResponse: IleQueryResponseExchange)(implicit request: AuthenticatedRequest[AnyContent]): Future[Result] =
    queryResponse.data match {
      case Some(response: SuccessfulResponseExchangeData) =>
        response.queriedUcr match {

          case ducrInfo: DucrInfo =>
            val ucrBlock = UcrBlock(ucr = ducrInfo.ucr, ucrType = Ducr)
            cacheRepository.upsert(Cache(request.eori, ucrBlock, true)).map { _ =>
              Ok(ileQueryDucrResponsePage(ducrInfo, response.parentMucr))
            }

          case mucrInfo: MucrInfo =>
            val ucrBlock = UcrBlock(ucr = mucrInfo.ucr, ucrType = Mucr)
            cacheRepository.upsert(Cache(request.eori, ucrBlock, true)).map { _ =>
              Ok(ileQueryMucrResponsePage(mucrInfo, response.parentMucr, response.sortedChildrenUcrs))
            }

          case _ => Future.successful(loadingPageResult)
        }

      case Some(response: UcrNotFoundResponseExchangeData) =>
        response.ucrBlock match {
          case Some(UcrBlock(ucr, _, _)) => Future.successful(Ok(consignmentNotFound(ucr)))
          case _                         => Future.successful(errorHandler.internalServerError)
        }

      case _ => Future.successful(errorHandler.internalServerError)
    }

  private def sendIleQuery(ucr: String)(implicit request: AuthenticatedRequest[AnyContent]): Future[Result] =
    form
      .fillAndValidate(ucr)
      .fold(
        formWithErrors => Future.successful(BadRequest(ileQueryPage(formWithErrors))),
        validUcr => {
          val ileQueryExchange = buildIleQuery(request.eori, validUcr)

          connector.submit(ileQueryExchange).flatMap { conversationId =>
            val ileQuery = IleQuery(retrieveSessionId, validUcr, conversationId)

            ileQueryRepository.insertOne(ileQuery).map { _ =>
              Redirect(IleQueryController.getConsignmentData(ucr))
            }
          }
        }
      )

  private def buildIleQuery(eori: String, ucr: String): IleQueryExchange = {
    val ucrType = if (validDucr(ucr)) Ducr.codeValue else Mucr.codeValue

    val ucrBlock = UcrBlock(ucr = ucr, ucrType = ucrType)

    IleQueryExchange(eori, ucrBlock)
  }

  private def retrieveSessionId(implicit hc: HeaderCarrier): String =
    hc.sessionId.getOrElse(throw new Exception("Session ID is missing")).value
}
