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

import config.AppConfig
import controllers.actions.AuthAction
import forms.Choice
import forms.Choice._
import javax.inject.{Inject, Singleton}
import models.cache._
import models.requests.AuthenticatedRequest
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.choice_page

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChoiceController @Inject()(
  authenticate: AuthAction,
  cache: CacheRepository,
  mcc: MessagesControllerComponents,
  choicePage: choice_page,
  appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayChoiceForm(): Action[AnyContent] = authenticate.async { implicit request =>
    {
      System.out.println(s"ileQuery: ${appConfig.ileQueryFeatureStatus}")
      cache
        .findByEori(request.eori)
        .map(_.map(cache => Choice(cache.answers.`type`)))
        .map {
          case Some(choice) => Ok(choicePage(Choice.form().fill(choice)))
          case None         => Ok(choicePage(Choice.form()))
        }
    }
  }

  def startSpecificJourney(choice: String): Action[AnyContent] = authenticate.async { implicit request =>
    proceed(Choice(choice))
  }

  def submitChoice(): Action[AnyContent] = authenticate.async { implicit request =>
    form()
      .bindFromRequest()
      .fold(formWithErrors => Future.successful(BadRequest(choicePage(formWithErrors))), proceed)
  }

  private def proceed(choice: Choice)(implicit request: AuthenticatedRequest[AnyContent]): Future[Result] = choice match {
    case Arrival         => saveAndRedirect(ArrivalAnswers(), controllers.routes.ConsignmentReferencesController.displayPage())
    case Departure       => saveAndRedirect(DepartureAnswers(), controllers.routes.ConsignmentReferencesController.displayPage())
    case AssociateUCR    => saveAndRedirect(AssociateUcrAnswers(), controllers.consolidations.routes.MucrOptionsController.displayPage())
    case DisassociateUCR => saveAndRedirect(DisassociateUcrAnswers(), controllers.consolidations.routes.DisassociateUcrController.displayPage())
    case ShutMUCR        => saveAndRedirect(ShutMucrAnswers(), controllers.consolidations.routes.ShutMucrController.displayPage())
    case Submissions     => Future.successful(Redirect(controllers.routes.SubmissionsController.displayPage()))
  }

  private def saveAndRedirect(answers: Answers, call: Call)(implicit request: AuthenticatedRequest[AnyContent]): Future[Result] =
    cache.upsert(Cache(request.eori, answers)).map(_ => Redirect(call))
}
