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

import controllers.actions.{AuthAction, JourneyRefiner}
import forms.ArrivalReference
import forms.ArrivalReference._
import javax.inject.{Inject, Singleton}
import models.ReturnToStartException
import models.cache.{ArrivalAnswers, Cache, JourneyType}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.arrival_reference

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ArrivalReferenceController @Inject()(
  authenticate: AuthAction,
  getJourney: JourneyRefiner,
  cache: CacheRepository,
  mcc: MessagesControllerComponents,
  arrivalReferencePage: arrival_reference
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen getJourney(JourneyType.ARRIVE)) { implicit request =>
    val answers = request.answersAs[ArrivalAnswers]
    val consignmentReference = answers.consignmentReferences.map(_.referenceValue).getOrElse(throw ReturnToStartException)
    Ok(arrivalReferencePage(answers.arrivalReference.fold(form)(form.fill), consignmentReference))
  }

  def submit(): Action[AnyContent] = (authenticate andThen getJourney(JourneyType.ARRIVE)).async { implicit request =>
    val answers = request.answersAs[ArrivalAnswers]
    def consignmentReference = answers.consignmentReferences.map(_.referenceValue).getOrElse(throw ReturnToStartException)
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[ArrivalReference]) => Future.successful(BadRequest(arrivalReferencePage(formWithErrors, consignmentReference))),
        validForm => {
          val updatedAnswers = answers.copy(arrivalReference = Some(validForm))
          cache.upsert(Cache(request.eori, updatedAnswers)).map { _ =>
            Redirect(controllers.routes.MovementDetailsController.displayPage())
          }
        }
      )
  }
}
