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

package controllers

import config.AppConfig
import connectors.CustomsDeclareExportsMovementsConnector
import controllers.actions.{AuthAction, JourneyAction}
import controllers.util.CacheIdGenerator.movementCacheId
import forms.Choice.AllowedChoiceValues._
import handlers.ErrorHandler
import javax.inject.Inject
import metrics.{MetricIdentifiers, MovementsMetrics}
import models.requests.JourneyRequest
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Result}
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.wco.dec.inventorylinking.common.UcrBlock
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest
import views.html.summary.{arrival_summary_page,confirmation_page, departure_summary_page}

import scala.concurrent.{ExecutionContext, Future}

class SummaryController @Inject()(
  override val messagesApi: MessagesApi,
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService,
  customsDeclareExportsMovementsConnector: CustomsDeclareExportsMovementsConnector,
  exportsMetrics: MovementsMetrics
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends FrontendController with I18nSupport {

  private val logger = Logger(this.getClass())

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val cacheMapOpt = customsCacheService.fetch(movementCacheId)

    val typeOfJourney = request.choice.value

    cacheMapOpt.map {
      case Some(data) if typeOfJourney == Arrival => Ok(arrival_summary_page(data))
      case Some(data) if typeOfJourney == Departure => Ok(departure_summary_page(data))
      case _ => handleError("Could not obtain data from DB")
    }
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
                    Redirect(controllers.routes.SummaryController.displayConfirmation())
                  case _ => handleError(s"Unable to save data")
                }
              }
          }
          case _ =>
            Future.successful(handleError(s"Could not obtain data from DB"))
        }
    }

  def displayConfirmation(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    Future.successful(Ok(confirmation_page(request.choice.value)))
  }

  private def handleError(logMessage: String)(implicit request: JourneyRequest[_]): Result = {
    logger.error(logMessage)
    InternalServerError(
      errorHandler.standardErrorTemplate(
        pageTitle = messagesApi("global.error.title"),
        heading = messagesApi("global.error.heading"),
        message = messagesApi("global.error.message")
      )
    )
  }

  private def parseDUCR(ucrBlock: UcrBlock): Option[String] =
    ucrBlock.ucrType match {
      case "D" => Some(ucrBlock.ucr)
      case _   => None
    }

  private def getMetricIdentifierFrom(movementData: InventoryLinkingMovementRequest): String =
    movementData.messageCode match {
      case "EAL" => MetricIdentifiers.arrivalMetric
      case "EDL" => MetricIdentifiers.departureMetric
    }
}
