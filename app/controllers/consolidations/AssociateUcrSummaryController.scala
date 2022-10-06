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

package controllers.consolidations

import controllers.actions.{AuthAction, JourneyRefiner}
import controllers.consolidations.routes.AssociateUcrConfirmationController
import controllers.storage.FlashKeys
import forms.UcrType
import models.ReturnToStartException
import models.cache.AssociateUcrAnswers
import models.cache.JourneyType.ASSOCIATE_UCR
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.SubmissionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.associateucr.{associate_ucr_summary, associate_ucr_summary_no_change}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class AssociateUcrSummaryController @Inject() (
  authenticate: AuthAction,
  journeyType: JourneyRefiner,
  mcc: MessagesControllerComponents,
  submissionService: SubmissionService,
  associateUcrSummaryPage: associate_ucr_summary,
  associateUcrSummaryNoChangePage: associate_ucr_summary_no_change
)(implicit executionContext: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType(ASSOCIATE_UCR)) { implicit request =>
    val answers = request.answersAs[AssociateUcrAnswers]
    val mucrOptions = answers.mucrOptions.getOrElse(throw ReturnToStartException)
    val associateUcr = answers.associateUcr.getOrElse(throw ReturnToStartException)

    if (!request.cache.ucrBlockFromIleQuery) Ok(associateUcrSummaryPage(associateUcr, mucrOptions.mucr))
    else if (answers.isAssociateAnotherMucr)
      Ok(associateUcrSummaryNoChangePage(mucrOptions.mucr, associateUcr.ucr, associateUcr.kind, answers.manageMucrChoice))
    else Ok(associateUcrSummaryNoChangePage(associateUcr.ucr, mucrOptions.mucr, UcrType.Mucr, answers.manageMucrChoice))
  }

  def submit(): Action[AnyContent] = (authenticate andThen journeyType(ASSOCIATE_UCR)).async { implicit request =>
    submissionService.submit(request.eori, request.answersAs[AssociateUcrAnswers]).map { _ =>
      Redirect(AssociateUcrConfirmationController.displayPage())
        .flashing(FlashKeys.MOVEMENT_TYPE -> request.answers.`type`.toString)
    }
  }
}
