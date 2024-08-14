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

package controllers

import controllers.actions.{AuthAction, JourneyRefiner}
import controllers.navigation.Navigator
import controllers.routes.TransportController
import controllers.summary.routes.ArriveOrDepartSummaryController
import forms.Location
import forms.Location._
import models.ReturnToStartException
import models.cache._
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.HtmlFormat.Appendable
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.location

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LocationController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyRefiner,
  cache: CacheRepository,
  mcc: MessagesControllerComponents,
  locationPage: location,
  navigator: Navigator
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with WithUnsafeDefaultFormBinding {

  val displayPage: Action[AnyContent] = (authenticate andThen journeyType(JourneyType.ARRIVE, JourneyType.DEPART)) { implicit request =>
    val location = request.answersAs[MovementAnswers].location
    Ok(buildPage(location.fold(form())(form().fill(_))))
  }

  val saveLocation: Action[AnyContent] = (authenticate andThen journeyType(JourneyType.ARRIVE, JourneyType.DEPART)).async { implicit request =>
    if (request.answersAs[MovementAnswers].consignmentReferences.map(_.referenceValue).isEmpty) throw ReturnToStartException

    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[Location]) => Future.successful(BadRequest(buildPage(formWithErrors))),
        validForm =>
          (request.answers: @unchecked) match {
            case arrivalAnswers: ArrivalAnswers =>
              cache.upsert(request.cache.update(arrivalAnswers.copy(location = Some(validForm), readyToSubmit = Some(true)))).map { _ =>
                Redirect(ArriveOrDepartSummaryController.displayPage)
              }

            case departureAnswers: DepartureAnswers =>
              cache.upsert(request.cache.update(departureAnswers.copy(location = Some(validForm)))).map { _ =>
                navigator.continueTo(TransportController.displayPage)
              }
          }
      )
  }

  private def buildPage(form: Form[Location])(implicit request: JourneyRequest[_]): Appendable = {
    val answers = request.answersAs[MovementAnswers]
    locationPage(form, answers.consignmentReferences.map(_.referenceValue).getOrElse(throw ReturnToStartException), answers.specificDateTimeChoice)
  }

}
