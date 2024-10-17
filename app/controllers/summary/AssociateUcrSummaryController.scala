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

package controllers.summary

import controllers.actions.{AuthAction, JourneyRefiner}
import controllers.summary.routes.MovementConfirmationController
import forms.UcrType
import models.ReturnToStartException
import models.cache.AssociateUcrAnswers
import models.cache.JourneyType.ASSOCIATE_UCR
import models.requests.SessionHelper._
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.SubmissionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.summary.{associate_ucr_summary, associate_ucr_summary_no_change}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class AssociateUcrSummaryController @Inject() (
  authenticate: AuthAction,
  journeyRefiner: JourneyRefiner,
  mcc: MessagesControllerComponents,
  submissionService: SubmissionService,
  associateUcrSummaryPage: associate_ucr_summary,
  associateUcrSummaryNoChangePage: associate_ucr_summary_no_change
)(implicit executionContext: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  private val authAndValidJourney = authenticate andThen journeyRefiner(ASSOCIATE_UCR)

  val displayPage: Action[AnyContent] = authAndValidJourney { implicit request =>
    val answers = request.answersAs[AssociateUcrAnswers]
    val mucrOptions = answers.mucrOptions.getOrElse(throw ReturnToStartException)
    val associateUcr = answers.associateUcr.getOrElse(throw ReturnToStartException)

    if (!request.cache.ucrBlockFromIleQuery) Ok(associateUcrSummaryPage(associateUcr, mucrOptions.mucr))
    else if (answers.isAssociateAnotherMucr)
      Ok(associateUcrSummaryNoChangePage(mucrOptions.mucr, associateUcr.ucr, associateUcr.kind, answers.manageMucrChoice))
    else Ok(associateUcrSummaryNoChangePage(associateUcr.ucr, mucrOptions.mucr, UcrType.Mucr, answers.manageMucrChoice))
  }

  val submit: Action[AnyContent] = authAndValidJourney.async { implicit request =>
    val answers = request.answersAs[AssociateUcrAnswers]
    val ucrType = answers.consignmentReferences.map(_.reference)
    val ucr = answers.consignmentReferences.map(_.referenceValue)
    val mucr = answers.mucrOptions.map(_.mucr)

    submissionService.submit(request.eori, answers, request.cache.uuid).map { conversationId =>
      val sessionValues = List(
        Some(CONVERSATION_ID -> conversationId),
        Some(JOURNEY_TYPE -> answers.`type`.toString),
        mucr.map(mucr => MUCR -> mucr),
        ucr.map(ucr => UCR -> ucr),
        ucrType.map(ucrType => UCR_TYPE -> ucrType)
      ).flatten

      Redirect(MovementConfirmationController.displayPage).addingToSession(sessionValues: _*)
    }
  }
}
