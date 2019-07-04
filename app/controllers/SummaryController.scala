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
import controllers.actions.{AuthAction, JourneyAction}
import controllers.util.CacheIdGenerator.movementCacheId
import forms.Choice.AllowedChoiceValues._
import handlers.ErrorHandler
import javax.inject.Inject
import models.requests.JourneyRequest
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.{CustomsCacheService, SubmissionService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.movement.movement_confirmation_page
import views.html.summary.{arrival_summary_page, departure_summary_page}

import scala.concurrent.{ExecutionContext, Future}

class SummaryController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  errorHandler: ErrorHandler,
  customsCacheService: CustomsCacheService,
  submissionService: SubmissionService,
  mcc: MessagesControllerComponents,
  arrivalSummaryPage: arrival_summary_page,
  departureSummaryPage: departure_summary_page,
  movementConfirmationPage: movement_confirmation_page
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends FrontendController(mcc) with I18nSupport {

  private val logger = Logger(this.getClass())

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val cacheMapOpt = customsCacheService.fetch(movementCacheId)

    val typeOfJourney = request.choice.value

    cacheMapOpt.map {
      case Some(data) if typeOfJourney == Arrival   => Ok(arrivalSummaryPage(data))
      case Some(data) if typeOfJourney == Departure => Ok(departureSummaryPage(data))
      case _                                        => handleError("Could not obtain data from DB")
    }
  }

  def submitMovementRequest(): Action[AnyContent] =
    (authenticate andThen journeyType).async { implicit request =>
      submissionService
        .submitMovementRequest(movementCacheId, request.authenticatedRequest.user.eori, request.choice)
        .flatMap {
          case ACCEPTED =>
            customsCacheService.remove(movementCacheId).map { _ =>
              Ok(movementConfirmationPage(request.choice.value))
            }
          case _ => Future.successful(handleError(s"Unable to submit movement data"))
        }
    }

  private def handleError(logMessage: String)(implicit request: JourneyRequest[_]): Result = {
    logger.error(logMessage)
    InternalServerError(
      errorHandler.standardErrorTemplate(
        pageTitle = Messages("global.error.title"),
        heading = Messages("global.error.heading"),
        message = Messages("global.error.message")
      )
    )
  }

}
