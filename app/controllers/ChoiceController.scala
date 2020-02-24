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

import controllers.actions.AuthAction
import forms.Choice._
import forms.{Choice, ConsignmentReferences}
import javax.inject.{Inject, Singleton}
import models.UcrBlock
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
  cacheRepository: CacheRepository,
  mcc: MessagesControllerComponents,
  choicePage: choice_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayChoiceForm(): Action[AnyContent] = authenticate.async { implicit request =>
    cacheRepository
      .findByEori(request.eori)
      .map {
        case Some(cache) =>
          cache.answers
            .map(answers => Ok(choicePage(Choice.form().fill(Choice(answers.`type`)))))
            .getOrElse(Ok(choicePage(Choice.form())))
        case None => Ok(choicePage(Choice.form())) // TODO redirect to search page
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

  private def proceed(choice: Choice)(implicit request: AuthenticatedRequest[AnyContent]): Future[Result] =
    choice match {
      case Arrival      => saveAndRedirect(ArrivalAnswers.fromUcr, controllers.routes.ConsignmentReferencesController.displayPage())
      case Departure    => saveAndRedirect(DepartureAnswers.fromUcr, controllers.routes.ConsignmentReferencesController.displayPage())
      case AssociateUCR => saveAndRedirect(AssociateUcrAnswers.fromUcr, controllers.consolidations.routes.MucrOptionsController.displayPage())
      case DisassociateUCR =>
        saveAndRedirect(DisassociateUcrAnswers.fromUcr, controllers.consolidations.routes.DisassociateUcrController.displayPage())
      case ShutMUCR    => saveAndRedirect(ShutMucrAnswers.fromUcr, controllers.consolidations.routes.ShutMucrController.displayPage())
      case Submissions => Future.successful(Redirect(controllers.routes.SubmissionsController.displayPage()))
    }

  private def saveAndRedirect(answerProvider: Option[UcrBlock] => Answers, call: Call)(
    implicit request: AuthenticatedRequest[AnyContent]
  ): Future[Result] =
    for {
      updatedCache: Cache <- cacheRepository.findByEori(request.eori).map {
        case Some(cache) => cache.copy(answers = Some(answerProvider.apply(cache.queryUcr)))
        case None        => Cache(request.eori, Some(answerProvider.apply(None)), None)
      }
      result <- cacheRepository.upsert(updatedCache).map(_ => Redirect(call))
    } yield (result)

}
