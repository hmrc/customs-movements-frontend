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

package controllers

import config.AppConfig
import controllers.actions.{AuthAction, JourneyAction}
import controllers.util.CacheIdGenerator.movementCacheId
import forms.DisassociateDucr
import forms.DisassociateDucr._
import handlers.ErrorHandler
import javax.inject.{Inject, Singleton}
import models.requests.JourneyRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SubmissionService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.disassociate_ducr

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DisassociateDucrController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  submissionService: SubmissionService,
  errorHandler: ErrorHandler,
  mcc: MessagesControllerComponents,
  disassociateDucrPage: disassociate_ducr
)(implicit appConfig: AppConfig, ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType) { implicit request =>
    Ok(disassociateDucrPage(form))
  }

  def submit(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(disassociateDucrPage(formWithErrors))),
        formData =>
          submit(formData).map { _ =>
            Redirect(controllers.routes.DisassociateDucrConfirmationController.displayPage())
              .flashing(FlashKeys.DUCR -> formData.ducr)
        }
      )
  }

  private def submit(formData: DisassociateDucr)(implicit r: JourneyRequest[_]): Future[Unit] =
    for {
      _ <- submissionService.submitDucrDisassociation(movementCacheId(), formData.ducr)
    } yield Unit
}
