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
import forms.Transport
import forms.Transport._
import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.filters.csrf.CSRF.ErrorHandler
import services.CustomsCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.transport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TransportController @Inject()(
  authenticate: AuthAction,
  journeyType: JourneyAction,
  customsCacheService: CustomsCacheService,
  errorHandler: ErrorHandler,
  mcc: MessagesControllerComponents,
  transportPage: transport
)(implicit appConfig: AppConfig, ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    customsCacheService.fetchAndGetEntry[Transport](movementCacheId, formId).map { data =>
      val formForView = data.fold(form)(form.fill(_))

      Ok(transportPage(formForView, request.choice.value))
    }
  }

  def saveTransport(): Action[AnyContent] = (authenticate andThen journeyType).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[Transport]) =>
          Future.successful(BadRequest(transportPage(formWithErrors, request.choice.value))),
        validForm =>
          customsCacheService.cache[Transport](movementCacheId, formId, validForm).map { _ =>
            Redirect(controllers.routes.SummaryController.displayPage())
        }
      )
  }
}
