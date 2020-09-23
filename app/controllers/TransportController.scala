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
import forms.Transport
import forms.Transport._
import javax.inject.{Inject, Singleton}
import models.ReturnToStartException
import models.cache.{DepartureAnswers, JourneyType}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.transport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TransportController @Inject()(
  authenticate: AuthAction,
  getJourney: JourneyRefiner,
  cache: CacheRepository,
  mcc: MessagesControllerComponents,
  transportPage: transport
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen getJourney(JourneyType.DEPART)) { implicit request =>
    val answers = request.answersAs[DepartureAnswers]
    val consignmentReference = answers.consignmentReferences.map(_.referenceValue).getOrElse(throw ReturnToStartException)
    Ok(transportPage(answers.transport.fold(form)(form.fill), consignmentReference))
  }

  def saveTransport(): Action[AnyContent] = (authenticate andThen getJourney(JourneyType.DEPART)).async { implicit request =>
    val answers = request.answersAs[DepartureAnswers]
    val consignmentReference = answers.consignmentReferences.map(_.referenceValue).getOrElse(throw ReturnToStartException)
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[Transport]) => Future.successful(BadRequest(transportPage(formWithErrors, consignmentReference))),
        validForm => {
          val movementAnswers = answers.copy(transport = Some(validForm))
          cache.upsert(request.cache.update(movementAnswers)).map { _ =>
            Redirect(controllers.routes.SummaryController.displayPage())
          }
        }
      )
  }
}
