/*
 * Copyright 2019 HM Revenue & Customs
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

package controllers.movement

import config.AppConfig
import connectors.CustomsDeclareExportsMovementsConnector
import controllers.actions.{AuthAction, JourneyAction}
import controllers.util.CacheIdGenerator.movementCacheId
import forms.GoodsDeparted
import forms.inventorylinking.MovementRequestSummaryMappingProvider
import handlers.ErrorHandler
import javax.inject.Inject
import metrics.{MetricIdentifiers, MovementsMetrics}
import models.requests.JourneyRequest
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Result}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.wco.dec.inventorylinking.common.UcrBlock
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest
import views.html.movement.{movement_confirmation_page, movement_summary_page}

import scala.concurrent.{ExecutionContext, Future}

class MovementSummaryController @Inject()(
  appConfig: AppConfig,
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService,
  customsDeclareExportsMovementsConnector: CustomsDeclareExportsMovementsConnector,
  exportsMetrics: MovementsMetrics
)(implicit ec: ExecutionContext)
    extends FrontendController with I18nSupport {

  def displaySummary(): Action[AnyContent] =
    (authenticate andThen journeyType).async { implicit request =>
      val form = Form(
        MovementRequestSummaryMappingProvider
          .provideMappingForMovementSummaryPage()
      )

      val movementRequestOpt = customsCacheService
        .fetchMovementRequest(movementCacheId, request.authenticatedRequest.user.eori)

      val backOrOutTheUKOpt = customsCacheService.fetchAndGetEntry[GoodsDeparted](movementCacheId, GoodsDeparted.formId)

      movementRequestOpt.zip(backOrOutTheUKOpt).map {
        case (Some(movementRequest), Some(backOrOutTheUK)) =>
          Ok(movement_summary_page(appConfig, form.fill(movementRequest), backOrOutTheUK))
        case _ => handleError(s"Could not obtain data from DB")

      }
    }

  private def parseDUCR(ucrBlock: UcrBlock): Option[String] =
    ucrBlock.ucrType match {
      case "D" => Some(ucrBlock.ucr)
      case _   => None
    }

  def submitMovementRequest(): Action[AnyContent] =
    (authenticate andThen journeyType).async { implicit request =>
      customsCacheService
        .fetchMovementRequest(movementCacheId, request.authenticatedRequest.user.eori)
        .flatMap {
          case Some(data) => {
            val ducrVal = parseDUCR(data.ucrBlock).getOrElse("")
            val eoriVal = request.authenticatedRequest.user.eori
            val mucrVal = data.masterUCR
            val movementType = "EAL" // EDL for departure

            val metricIdentifier = getMetricIdentifierFrom(data)
            exportsMetrics.startTimer(metricIdentifier)

            customsDeclareExportsMovementsConnector
              .submitMovementDeclaration(ducrVal, mucrVal, movementType, data.toXml)
              .map { submitResponse =>
                submitResponse.status match {
                  case ACCEPTED =>
                    exportsMetrics.incrementCounter(metricIdentifier)
                    Redirect(
                      controllers.movement.routes.MovementSummaryController
                        .displayConfirmation()
                    )
                  case _ => handleError(s"Unable to save data")
                }
              }
          }
          case _ =>
            Future.successful(handleError(s"Could not obtain data from DB"))
        }
    }

  def displayConfirmation(): Action[AnyContent] =
    (authenticate andThen journeyType).async { implicit request =>
      customsCacheService
        .fetchMovementRequest(movementCacheId, request.authenticatedRequest.user.eori)
        .flatMap {
          case Some(data) =>
            customsCacheService.remove(movementCacheId).map { _ =>
              Ok(movement_confirmation_page(appConfig, data.messageCode, data.ucrBlock.ucr))
            }
          case _ =>
            Future.successful(handleError(s"Could not obtain data from DB"))
        }
    }

  private def handleError(logMessage: String)(implicit request: JourneyRequest[_]): Result = {
    Logger.error(logMessage)
    InternalServerError(
      errorHandler.standardErrorTemplate(
        pageTitle = messagesApi("global.error.title"),
        heading = messagesApi("global.error.heading"),
        message = messagesApi("global.error.message")
      )
    )
  }

  private def getMetricIdentifierFrom(movementData: InventoryLinkingMovementRequest): String =
    movementData.messageCode match {
      case "EAL" => MetricIdentifiers.arrivalMetric
      case "EDL" => MetricIdentifiers.departureMetric
    }

}
