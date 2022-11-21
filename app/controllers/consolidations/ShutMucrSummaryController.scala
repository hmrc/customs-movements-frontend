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
import controllers.consolidations.routes.ShutMucrConfirmationController
import controllers.storage.FlashKeys
import forms.ShutMucr
import models.ReturnToStartException
import models.cache.{JourneyType, ShutMucrAnswers}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SubmissionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.shutmucr.shut_mucr_summary

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ShutMucrSummaryController @Inject() (
  authenticate: AuthAction,
  getJourney: JourneyRefiner,
  mcc: MessagesControllerComponents,
  submissionService: SubmissionService,
  page: shut_mucr_summary
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage: Action[AnyContent] = (authenticate andThen getJourney(JourneyType.SHUT_MUCR)) { implicit request =>
    val mucr: ShutMucr = request.answersAs[ShutMucrAnswers].shutMucr.getOrElse(throw ReturnToStartException)
    Ok(page(mucr))
  }

  def submit: Action[AnyContent] = (authenticate andThen getJourney(JourneyType.SHUT_MUCR)).async { implicit request =>
    val answers = request.answersAs[ShutMucrAnswers]
    val ucrType = answers.consignmentReferences.map(_.reference)
    val ucr = answers.consignmentReferences.map(_.referenceValue)
    val flash = Seq(
      Some(FlashKeys.MOVEMENT_TYPE -> answers.`type`.toString),
      ucr.map(ucr => FlashKeys.UCR -> ucr),
      ucrType.map(ucrType => FlashKeys.UCR_TYPE -> ucrType)
    ).flatten

    submissionService.submit(request.eori, answers).map { _ =>
      Redirect(ShutMucrConfirmationController.displayPage)
        .flashing(flash: _*)
    }
  }
}
