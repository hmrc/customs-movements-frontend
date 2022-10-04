/*
 * Copyright 2022 HM Revenue & Customs
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
import controllers.routes.SpecificDateTimeController
import forms.ConsignmentReferences
import forms.ConsignmentReferences._
import models.cache.JourneyType.{ARRIVE, DEPART}
import models.cache._
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.consignment_references

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConsignmentReferencesController @Inject() (
  authenticate: AuthAction,
  getJourney: JourneyRefiner,
  cacheRepository: CacheRepository,
  mcc: MessagesControllerComponents,
  consignmentReferencesPage: consignment_references,
  navigator: Navigator
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with WithDefaultFormBinding {

  def displayPage(): Action[AnyContent] = (authenticate andThen getJourney(ARRIVE, DEPART)) { implicit request =>
    val references = request.answersAs[MovementAnswers].consignmentReferences
    Ok(consignmentReferencesPage(references.fold(form(request.answers.`type`))(form(request.answers.`type`).fill(_))))
  }

  def saveConsignmentReferences(): Action[AnyContent] = (authenticate andThen getJourney(ARRIVE, DEPART)).async { implicit request =>
    form(request.answers.`type`)
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[ConsignmentReferences]) => Future.successful(BadRequest(consignmentReferencesPage(formWithErrors))),
        validForm =>
          request.answers match {
            case arrivalAnswers: ArrivalAnswers     => saveAndContinue(arrivalAnswers.copy(consignmentReferences = Some(validForm)))
            case departureAnswers: DepartureAnswers => saveAndContinue(departureAnswers.copy(consignmentReferences = Some(validForm)))
          }
      )
  }

  private def saveAndContinue(answers: Answers)(implicit request: JourneyRequest[AnyContent]): Future[Result] =
    cacheRepository.upsert(request.cache.update(answers)).map { _ =>
      navigator.continueTo(SpecificDateTimeController.displayPage())
    }
}
