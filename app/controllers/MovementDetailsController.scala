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
import forms.Choice.AllowedChoiceValues
import forms.MovementDetails._
import forms.{ArrivalDetails, DepartureDetails}
import javax.inject.{Inject, Singleton}
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import play.filters.csrf.CSRF.ErrorHandler
import play.twirl.api.Html
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.{arrival_details, departure_details}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MovementDetailsController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  customsCacheService: CustomsCacheService,
  errorHandler: ErrorHandler,
  mcc: MessagesControllerComponents
)(implicit appConfig: AppConfig, ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    request.choice.value match {
      case AllowedChoiceValues.Arrival   => arrivalPage().map(Ok(_))
      case AllowedChoiceValues.Departure => departurePage().map(Ok(_))
    }
  }

  private def arrivalPage()(implicit request: JourneyRequest[AnyContent]): Future[Html] =
    customsCacheService
      .fetchAndGetEntry[ArrivalDetails](movementCacheId, formId)
      .map(data => arrival_details(data.fold(arrivalForm)(arrivalForm.fill(_))))

  private def departurePage()(implicit request: JourneyRequest[AnyContent]): Future[Html] =
    customsCacheService
      .fetchAndGetEntry[DepartureDetails](movementCacheId, formId)
      .map(data => departure_details(data.fold(departureForm)(departureForm.fill(_))))

  def saveMovementDetails(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    (request.choice.value match {
      case AllowedChoiceValues.Arrival   => handleSavingArrival()
      case AllowedChoiceValues.Departure => handleSavingDeparture()
    }).flatMap {
      case Left(resultView) => Future.successful(BadRequest(resultView))
      case Right(call)      => Future.successful(Redirect(call))
    }
  }

  private def handleSavingArrival()(implicit request: JourneyRequest[AnyContent]): Future[Either[Html, Call]] =
    arrivalForm
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[ArrivalDetails]) => Future.successful(Left(arrival_details(formWithErrors))),
        validForm =>
          customsCacheService.cache[ArrivalDetails](movementCacheId, formId, validForm.formatTime()).map { _ =>
            Right(controllers.routes.LocationController.displayPage())
        }
      )

  private def handleSavingDeparture()(implicit request: JourneyRequest[AnyContent]): Future[Either[Html, Call]] =
    departureForm
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[DepartureDetails]) => Future.successful(Left(departure_details(formWithErrors))),
        validForm =>
          customsCacheService.cache[DepartureDetails](movementCacheId, formId, validForm).map { _ =>
            Right(controllers.routes.TransportController.displayPage())
        }
      )
}
