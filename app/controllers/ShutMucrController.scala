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
import controllers.actions.AuthAction
import controllers.storage.FlashKeys
import forms.ShutMucr.form
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Flash, MessagesControllerComponents}
import services.SubmissionService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.shut_mucr

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ShutMucrController @Inject()(
  authenticate: AuthAction,
  submissionService: SubmissionService,
  mcc: MessagesControllerComponents,
  shutMucrPage: shut_mucr
)(implicit appConfig: AppConfig, ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = authenticate { implicit request =>
    Ok(shutMucrPage(form()))
  }

  def submitForm(): Action[AnyContent] = authenticate.async { implicit request =>
    form()
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(shutMucrPage(formWithErrors))),
        shutMucr =>
          submissionService.submitShutMucrRequest(formData = shutMucr).map { _ =>
            Redirect(controllers.routes.ShutMucrConfirmationController.displayPage())
              .flashing(Flash(Map(FlashKeys.MUCR -> shutMucr.mucr)))
        }
      )
  }
}
