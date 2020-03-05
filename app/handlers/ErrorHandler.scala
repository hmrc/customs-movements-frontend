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

package handlers

import config.AppConfig
import controllers.exception.{IncompleteApplication, InvalidFeatureStateException}
import controllers.routes
import javax.inject.{Inject, Singleton}
import models.ReturnToStartException
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.Results.{BadRequest, NotFound}
import play.api.mvc.{Request, RequestHeader, Result, Results}
import play.api.{Configuration, Environment}
import play.twirl.api.Html
import uk.gov.hmrc.auth.core.{InsufficientEnrolments, NoActiveSession}
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects
import uk.gov.hmrc.play.bootstrap.http.FrontendErrorHandler
import views.html.error_template

@Singleton
class ErrorHandler @Inject()(appConfig: AppConfig, override val messagesApi: MessagesApi, errorTemplate: error_template)
    extends FrontendErrorHandler with I18nSupport with AuthRedirects {
  override def config: Configuration = appConfig.runModeConfiguration

  override def env: Environment = appConfig.environment

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: Request[_]): Html =
    errorTemplate(pageTitle, heading, message)

  override def resolveError(rh: RequestHeader, ex: Throwable): Result =
    ex match {
      case _: NoActiveSession =>
        Results.Redirect(appConfig.loginUrl, Map("continue" -> Seq(appConfig.loginContinueUrl)))
      case _: InsufficientEnrolments =>
        Results.SeeOther(routes.UnauthorisedController.onPageLoad().url)
      case _: IncompleteApplication | ReturnToStartException =>
        Results.Redirect(routes.StartController.displayStartPage())
      case _: InvalidFeatureStateException => NotFound(notFoundTemplate(Request(rh, "")))
      case _                               => super.resolveError(rh, ex)
    }

  def getBadRequestPage()(implicit request: Request[_]): Result =
    BadRequest(
      standardErrorTemplate(
        pageTitle = Messages("global.error.title"),
        heading = Messages("global.error.heading"),
        message = Messages("global.error.message")
      )
    )

  def standardErrorTemplate()(implicit request: Request[_]): Html =
    errorTemplate("global.error.title", "global.error.heading", "global.error.message")
}
