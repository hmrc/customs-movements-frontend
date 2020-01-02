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

package controllers.consolidations

import controllers.actions.{AuthAction, JourneyRefiner}
import controllers.storage.FlashKeys
import javax.inject.{Inject, Singleton}
import models.ReturnToStartException
import models.cache.AssociateUcrAnswers
import play.api.i18n.I18nSupport
import play.api.mvc._
import repositories.CacheRepository
import services.SubmissionService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.associate_ucr_summary

import scala.concurrent.ExecutionContext

@Singleton
class AssociateUcrSummaryController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyRefiner,
  mcc: MessagesControllerComponents,
  cache: CacheRepository,
  submissionService: SubmissionService,
  associateUcrSummaryPage: associate_ucr_summary
)(implicit executionContext: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    val answers = request.answersAs[AssociateUcrAnswers]
    val mucrOptions = answers.mucrOptions.getOrElse(throw ReturnToStartException)
    val ucr = answers.associateUcr.getOrElse(throw ReturnToStartException)
    Ok(associateUcrSummaryPage(ucr, mucrOptions.mucr))
  }

  def submit(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    val answers = request.answersAs[AssociateUcrAnswers]
    val associateUcr = answers.associateUcr.getOrElse(throw ReturnToStartException)

    submissionService.submit(request.eori, answers).map { _ =>
      Redirect(controllers.consolidations.routes.AssociateUcrConfirmationController.displayPage())
        .flashing(FlashKeys.CONSOLIDATION_KIND -> associateUcr.kind.formValue, FlashKeys.UCR -> associateUcr.ucr)
    }
  }
}
