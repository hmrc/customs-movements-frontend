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

import config.IleQueryConfig
import controllers.actions.AuthAction
import controllers.consolidations.routes.ShutMucrController
import controllers.ileQuery.routes.FindConsignmentController
import controllers.routes.{DucrPartChiefController, SubmissionsController}
import forms.Choice
import forms.Choice._
import models.UcrBlock
import models.cache._
import models.requests.AuthenticatedRequest
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.choice

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChoiceController @Inject() (
  authenticate: AuthAction,
  cacheRepository: CacheRepository,
  mcc: MessagesControllerComponents,
  ileQueryConfig: IleQueryConfig,
  choicePage: choice
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with WithDefaultFormBinding {

  val displayChoices: Action[AnyContent] = authenticate.async { implicit request =>
    cacheRepository.findByEori(request.eori).map(_.flatMap(_.answers)).map {
      case Some(answers) => Ok(choicePage(form.fill(Choice(answers.`type`))))
      case None          => Ok(choicePage(form))
    }
  }

  val submitChoice: Action[AnyContent] = authenticate.async { implicit request =>
    form.bindFromRequest
      .fold(formWithErrors => Future.successful(BadRequest(choicePage(formWithErrors))), nextPage)
  }

  private def nextPage(choice: Choice)(implicit request: AuthenticatedRequest[AnyContent]): Future[Result] =
    choice match {
      case Arrival         => saveCache(request.eori, ArrivalAnswers.fromUcr, DucrPartChiefController.displayPage)
      case Departure       => saveCache(request.eori, DepartureAnswers.fromUcr, DucrPartChiefController.displayPage)
      case AssociateUCR    => saveCache(request.eori, AssociateUcrAnswers.fromUcr, DucrPartChiefController.displayPage)
      case DisassociateUCR => saveCache(request.eori, DisassociateUcrAnswers.fromUcr, DucrPartChiefController.displayPage)
      case ShutMUCR        => saveCache(request.eori, ShutMucrAnswers.fromUcr, ShutMucrController.displayPage)
      case Submissions     => resetCache(request.eori, SubmissionsController.displayPage)
      case FindConsignment if ileQueryConfig.isIleQueryEnabled => resetCache(request.eori, FindConsignmentController.displayPage)
    }

  def resetCache(eori: String, call: Call): Future[Result] =
    cacheRepository.upsert(Cache(eori)).map(_ => Redirect(call))

  def saveCache(eori: String, answer: Option[UcrBlock] => Answers, call: Call): Future[Result] =
    cacheRepository.upsert(Cache(eori, answer.apply(None))).map(_ => Redirect(call))
}
