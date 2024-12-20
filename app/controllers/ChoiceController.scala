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

import controllers.actions.AuthAction
import controllers.consolidations.routes.{DisassociateUcrController, MucrOptionsController, ShutMucrController}
import controllers.ileQuery.routes.FindConsignmentController
import controllers.routes.{ConsignmentReferencesController, SubmissionsController}
import forms.Choice
import forms.Choice._
import models.UcrBlock
import models.cache._
import models.requests.{AuthenticatedRequest, SessionHelper}
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.choice

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChoiceController @Inject() (authenticate: AuthAction, cacheRepository: CacheRepository, mcc: MessagesControllerComponents, choicePage: choice)(
  implicit ec: ExecutionContext
) extends FrontendController(mcc) with I18nSupport with WithUnsafeDefaultFormBinding {

  val displayChoices: Action[AnyContent] = authenticate.async { implicit request =>
    val maybeAnswerCacheId = SessionHelper.getValue(SessionHelper.ANSWER_CACHE_ID)

    val futureResult = maybeAnswerCacheId.map { cacheId =>
      cacheRepository.findByEoriAndAnswerCacheId(request.eori, cacheId).map(_.flatMap(_.answers)).map {
        case Some(answers) => Ok(choicePage(form.fill(Choice(answers.`type`))))
        case None          => Ok(choicePage(form))
      }
    }.getOrElse(Future.successful(Ok(choicePage(form))))

    futureResult.map(_.withSession(SessionHelper.clearAllReceiptPageSessionKeys()))
  }

  def submitChoice(choice: String): Action[AnyContent] = authenticate.async { implicit request =>
    try
      nextPage(Choice(choice))
    catch {
      case e: IllegalArgumentException => Future.successful(NotFound)
    }
  }

  def nextPage(choice: Choice)(implicit request: AuthenticatedRequest[AnyContent]): Future[Result] =
    (choice: @unchecked) match {
      case FindConsignment => resetCache(request.eori, FindConsignmentController.displayPage)
      case Arrival         => saveCache(request.eori, ArrivalAnswers.fromUcr, ConsignmentReferencesController.displayPage)
      case Departure       => saveCache(request.eori, DepartureAnswers.fromUcr, ConsignmentReferencesController.displayPage)
      case AssociateUCR    => saveCache(request.eori, AssociateUcrAnswers.fromUcr, MucrOptionsController.displayPage)
      case DisassociateUCR => saveCache(request.eori, DisassociateUcrAnswers.fromUcr, DisassociateUcrController.displayPage)
      case ShutMUCR        => saveCache(request.eori, ShutMucrAnswers.fromUcr, ShutMucrController.displayPage)
      case Submissions     => resetCache(request.eori, SubmissionsController.displayPage)
    }

  def resetCache(eori: String, call: Call): Future[Result] =
    cacheRepository.upsert(Cache(eori)).map(_ => Redirect(call))

  def saveCache(eori: String, answer: Option[UcrBlock] => Answers, call: Call)(implicit request: AuthenticatedRequest[AnyContent]): Future[Result] =
    cacheRepository
      .upsert(Cache(eori, answer.apply(None)))
      .map(answerCache => Redirect(call).addingToSession(SessionHelper.ANSWER_CACHE_ID -> answerCache.uuid))
}
