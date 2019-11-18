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
import forms.ConsignmentReferences
import forms.ConsignmentReferences._
import javax.inject.{Inject, Singleton}
import models.cache._
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.consignment_references

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConsignmentReferencesController @Inject()(
                                                 authenticate: AuthAction,
                                                 getJourney: JourneyRefiner,
                                                 cache: CacheRepository,
                                                 mcc: MessagesControllerComponents,
                                                 consignmentReferencesPage: consignment_references
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen getJourney(JourneyType.ARRIVE, JourneyType.DEPART)) { implicit request =>
    val references = request.answersAs[MovementAnswers].consignmentReferences
    Ok(consignmentReferencesPage(references.fold(form())(form().fill(_))))
  }

  def saveConsignmentReferences(): Action[AnyContent] = (authenticate andThen getJourney).async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[ConsignmentReferences]) => Future.successful(BadRequest(consignmentReferencesPage(formWithErrors))),
        validForm => {
          request.answers match {
            case arrivalAnswers: ArrivalAnswers =>
              cache.upsert(Cache(request.eori, arrivalAnswers.copy(consignmentReferences = Some(validForm)))).map { _ =>
                Redirect(controllers.routes.ArrivalReferenceController.displayPage())
              }
            case departureAnswers: DepartureAnswers =>
              cache.upsert(Cache(request.eori, departureAnswers.copy(consignmentReferences = Some(validForm)))).map { _ =>
                Redirect(controllers.routes.MovementDetailsController.displayPage())
              }
          }
        }
      )
  }
}
