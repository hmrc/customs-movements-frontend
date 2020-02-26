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
import models.cache.{DisassociateUcrAnswers, JourneyType}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.SubmissionService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.disassociateucr.disassociate_ucr_summary

import scala.concurrent.ExecutionContext

@Singleton
class DisassociateUcrSummaryController @Inject()(
  authenticate: AuthAction,
  getJourney: JourneyRefiner,
  mcc: MessagesControllerComponents,
  submissionService: SubmissionService,
  page: disassociate_ucr_summary
)(implicit executionContext: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen getJourney(JourneyType.DISSOCIATE_UCR)) { implicit request =>
    request.answersAs[DisassociateUcrAnswers].ucr match {
      case Some(ucr) => Ok(page(ucr))
      case _         => throw ReturnToStartException
    }
  }

  def submit(): Action[AnyContent] = (authenticate andThen getJourney(JourneyType.DISSOCIATE_UCR)).async { implicit request =>
    val answers = request.answersAs[DisassociateUcrAnswers]

    submissionService.submit(request.eori, answers).map { _ =>
      Redirect(routes.DisassociateUcrConfirmationController.displayPage())
        .flashing(FlashKeys.MOVEMENT_TYPE -> request.answers.`type`.toString)
    }
  }
}
