/*
 * Copyright 2020 HM Revenue & Customs
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

import controllers.actions.{AuthAction, JourneyRefiner}
import forms.SpecificDateTimeChoice.form
import forms.{ArrivalDetails, DepartureDetails, SpecificDateTimeChoice}
import javax.inject.{Inject, Singleton}
import models.cache.{ArrivalAnswers, DepartureAnswers, JourneyType, MovementAnswers}
import models.requests.JourneyRequest
import models.{DateTimeProvider, ReturnToStartException}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.specific_date_and_time

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SpecificDateTimeController @Inject()(
  authenticate: AuthAction,
  getJourney: JourneyRefiner,
  cache: CacheRepository,
  mcc: MessagesControllerComponents,
  specificDateTimePage: specific_date_and_time,
  dateTimeProvider: DateTimeProvider
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen getJourney(JourneyType.ARRIVE, JourneyType.DEPART)) { implicit request =>
    val choice = request.answersAs[MovementAnswers].specificDateTimeChoice
    Ok(buildPage(choice.fold(form())(form().fill(_))))
  }

  def submit(): Action[AnyContent] =
    (authenticate andThen getJourney(JourneyType.ARRIVE, JourneyType.DEPART)).async { implicit request =>
      form()
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[SpecificDateTimeChoice]) => Future.successful(BadRequest(buildPage(formWithErrors))),
          validForm => {
            val answers = request.answers match {
              case arrivalAnswers: ArrivalAnswers     => updateArrivalAnswers(arrivalAnswers, validForm)
              case departureAnswers: DepartureAnswers => updateDepartureAnswers(departureAnswers, validForm)
            }
            cache.upsert(request.cache.update(answers)).map { _ =>
              validForm.choice match {
                case SpecificDateTimeChoice.UserDateTime    => Redirect(controllers.routes.MovementDetailsController.displayPage())
                case SpecificDateTimeChoice.CurrentDateTime => Redirect(controllers.routes.LocationController.displayPage())
              }
            }
          }
        )
    }

  private def buildPage(form: Form[SpecificDateTimeChoice])(implicit request: JourneyRequest[_]) =
    specificDateTimePage(form, request.answersAs[MovementAnswers].consignmentReferences.map(_.referenceValue).getOrElse(throw ReturnToStartException))

  private def updateArrivalAnswers(arrivalAnswers: ArrivalAnswers, specificDateTimeChoice: SpecificDateTimeChoice): ArrivalAnswers = {
    def createArrivalDetails(existingDetails: Option[ArrivalDetails]) = specificDateTimeChoice.choice match {
      case SpecificDateTimeChoice.UserDateTime    => existingDetails
      case SpecificDateTimeChoice.CurrentDateTime => Some(ArrivalDetails(dateTimeProvider.dateNow, dateTimeProvider.timeNow))
    }

    arrivalAnswers.copy(specificDateTimeChoice = Some(specificDateTimeChoice), arrivalDetails = createArrivalDetails(arrivalAnswers.arrivalDetails))
  }

  private def updateDepartureAnswers(departureAnswers: DepartureAnswers, specificDateTimeChoice: SpecificDateTimeChoice): DepartureAnswers = {
    def createDepartureDetails(existingDetails: Option[DepartureDetails]) = specificDateTimeChoice.choice match {
      case SpecificDateTimeChoice.UserDateTime    => existingDetails
      case SpecificDateTimeChoice.CurrentDateTime => Some(DepartureDetails(dateTimeProvider.dateNow, dateTimeProvider.timeNow))
    }

    departureAnswers.copy(
      specificDateTimeChoice = Some(specificDateTimeChoice),
      departureDetails = createDepartureDetails(departureAnswers.departureDetails)
    )
  }
}
