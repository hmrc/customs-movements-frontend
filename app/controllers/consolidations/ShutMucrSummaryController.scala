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

package controllers.consolidations

import controllers.actions.{AuthAction, JourneyRefiner}
import controllers.storage.FlashKeys
import forms.ShutMucr
import javax.inject.Inject
import models.ReturnToStartException
import models.cache.{JourneyType, ShutMucrAnswers}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.CacheRepository
import services.SubmissionService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.shut_mucr_summary

import scala.concurrent.ExecutionContext

class ShutMucrSummaryController @Inject()(
  authenticate: AuthAction,
  getJourney: JourneyRefiner,
  mcc: MessagesControllerComponents,
  cache: CacheRepository,
  submissionService: SubmissionService,
  page: shut_mucr_summary
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen getJourney(JourneyType.SHUT_MUCR)) { implicit request =>
    val mucr: ShutMucr = request.answersAs[ShutMucrAnswers].shutMucr.getOrElse(throw ReturnToStartException)
    Ok(page(mucr))
  }

  def submit(): Action[AnyContent] = (authenticate andThen getJourney(JourneyType.SHUT_MUCR)).async { implicit request =>
    val answers = request.answersAs[ShutMucrAnswers]
    val mucr = answers.shutMucr.map(_.mucr).getOrElse(throw ReturnToStartException)
    submissionService.submit(request.eori, answers).map { _ =>
      Redirect(routes.ShutMucrConfirmationController.displayPage())
        .flashing(FlashKeys.MUCR -> mucr)
    }
  }
}
