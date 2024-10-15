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
import models.ReturnToStartException
import models.cache.DisassociateUcrAnswers
import models.cache.JourneyType.DISSOCIATE_UCR
import models.requests.SessionHelper._
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.SubmissionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.summary.disassociate_ucr_summary

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class DisassociateUcrSummaryController @Inject() (
  authenticate: AuthAction,
  journeyRefiner: JourneyRefiner,
  mcc: MessagesControllerComponents,
  submissionService: SubmissionService,
  page: disassociate_ucr_summary
)(implicit executionContext: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  private val authAndValidJourney = authenticate andThen journeyRefiner(DISSOCIATE_UCR)

  val displayPage: Action[AnyContent] = authAndValidJourney { implicit request =>
    request.answersAs[DisassociateUcrAnswers].ucr match {
      case Some(ucr) => Ok(page(ucr))
      case _         => throw ReturnToStartException
    }
  }

  val submit: Action[AnyContent] = authAndValidJourney.async { implicit request =>
    val answers = request.answersAs[DisassociateUcrAnswers]
    val ucrType = answers.consignmentReferences.map(_.reference)
    val ucr = answers.consignmentReferences.map(_.referenceValue)

    submissionService.submit(request.eori, answers, request.cache.uuid).map { conversationId =>
      val sessionValues = List[Option[(String, String)]](
        Some(CONVERSATION_ID -> conversationId),
        Some(JOURNEY_TYPE -> answers.`type`.toString),
        ucr.map(ucr => UCR -> ucr),
        ucrType.map(ucrType => UCR_TYPE -> ucrType)
      ).flatten

      Redirect(MovementConfirmationController.displayPage).addingToSession(sessionValues: _*)
    }
  }
}
