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

import controllers.actions.{ArriveDepartAllowList, AuthAction}
import controllers.consolidations.routes.ManageMucrController
import controllers.routes.{ChoiceController, SpecificDateTimeController}
import controllers.summary.routes.{DisassociateUcrSummaryController, ShutMucrSummaryController}
import forms.Choice
import forms.Choice._
import models.UcrBlock
import models.cache._
import models.requests.AuthenticatedRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.CacheRepository
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.choice_on_consignment

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChoiceOnConsignmentController @Inject() (
  authenticate: AuthAction,
  cacheRepository: CacheRepository,
  mcc: MessagesControllerComponents,
  arriveDepartAllowList: ArriveDepartAllowList,
  choicePage: choice_on_consignment
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with WithUnsafeDefaultFormBinding {

  val displayChoices: Action[AnyContent] = authenticate.async { implicit request =>
    def okPage(cacheAndUcr: CacheAndUcr): Future[Result] = {
      val frm = cacheAndUcr.cache.answers.fold(form)(answers => form.fill(Choice(answers.`type`)))
      Future.successful(Ok(choicePage(frm, cacheAndUcr.ucrBlock)))
    }

    processWithCacheAndUcr(request.eori, okPage)
  }

  val submitChoice: Action[AnyContent] = authenticate.async { implicit request =>
    def badRequestPage(formWithErrors: Form[Choice]): CacheAndUcr => Future[Result] =
      (cacheAndUcr: CacheAndUcr) => Future.successful(BadRequest(choicePage(formWithErrors, cacheAndUcr.ucrBlock)))

    form
      .bindFromRequest()
      .fold(formWithErrors => processWithCacheAndUcr(request.eori, badRequestPage(formWithErrors)), process)
  }

  lazy val choicesNoArriveAndDepart = List(AssociateUCR, DisassociateUCR, ShutMUCR)

  private def process(choice: Choice)(implicit request: AuthenticatedRequest[_]): Future[Result] = {
    val validChoices = if (arriveDepartAllowList.contains(request.eori)) consignmentChoices else choicesNoArriveAndDepart
    if (validChoices.contains(choice)) {
      val answerAndCall: (Option[UcrBlock] => Answers, Call) = choice match {
        case Arrival         => (ArrivalAnswers.fromUcr, SpecificDateTimeController.displayPage)
        case Departure       => (DepartureAnswers.fromUcr, SpecificDateTimeController.displayPage)
        case DisassociateUCR => (DisassociateUcrAnswers.fromUcr, DisassociateUcrSummaryController.displayPage)
        case ShutMUCR        => (ShutMucrAnswers.fromUcr, ShutMucrSummaryController.displayPage)
        case _               => (AssociateUcrAnswers.fromUcr, ManageMucrController.displayPage)
      }

      val updateCache = (cacheAndUcr: CacheAndUcr) => {
        val cache = cacheAndUcr.cache.copy(answers = Some(answerAndCall._1.apply(Some(cacheAndUcr.ucrBlock))))
        cacheRepository.upsert(cache).map(_ => Redirect(answerAndCall._2))
      }

      processWithCacheAndUcr(request.eori, updateCache)
    } else Future.successful(Redirect(controllers.routes.ChoiceOnConsignmentController.displayChoices))
  }

  private def processWithCacheAndUcr(eori: String, f: CacheAndUcr => Future[Result]): Future[Result] =
    cacheRepository.findByEori(eori).flatMap { maybeCache =>
      (for {
        cache <- maybeCache
        ucrBlock <- cache.ucrBlock
      } yield CacheAndUcr(cache, ucrBlock)) match {
        case Some(cacheAndUcr) => f(cacheAndUcr)
        case _                 => Future.successful(Redirect(ChoiceController.displayChoices))
      }
    }
}
