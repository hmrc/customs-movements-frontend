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

package handlers

import config.AppConfig
import controllers.exception.{IncompleteApplication, InvalidFeatureStateException}
import controllers.routes.{RootController, UnauthorisedController}
import models.ReturnToStartException
import play.api.http.HeaderNames.CACHE_CONTROL
import play.api.i18n.MessagesApi
import play.api.mvc.Results.{InternalServerError, NotFound}
import play.api.mvc.{Request, RequestHeader, Result, Results}
import play.twirl.api.Html
import uk.gov.hmrc.auth.core.{InsufficientEnrolments, NoActiveSession}
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler
import views.html.error_template

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ErrorHandler @Inject() (appConfig: AppConfig, override val messagesApi: MessagesApi, errorTemplate: error_template)(
  implicit executionContext: ExecutionContext
) extends FrontendErrorHandler {

  implicit val ec: ExecutionContext = executionContext

  override def standardErrorTemplate(titleKey: String, headingKey: String, messageKey: String)(
    implicit requestHeader: RequestHeader
  ): Future[Html] = {
    implicit val request: Request[_] = Request(requestHeader, "")
    Future.successful(defaultErrorTemplate(titleKey, headingKey, messageKey))
  }

  override def resolveError(rh: RequestHeader, ex: Throwable): Future[Result] = {
    val result = ex match {
      case _: NoActiveSession =>
        Results.Redirect(appConfig.loginUrl, Map("continue" -> Seq(appConfig.loginContinueUrl)))

      case _: InsufficientEnrolments => Results.SeeOther(UnauthorisedController.onPageLoad.url)

      case _: IncompleteApplication | ReturnToStartException => Results.Redirect(RootController.displayPage)

      case _: InvalidFeatureStateException => notFound(Request(rh, ""))

      case _ => internalServerError(Request(rh, ""))
    }
    Future.successful(result)
  }

  def defaultErrorTemplate(
    titleKey: String = "global.error.title",
    headingKey: String = "global.error.heading",
    messageKey: String = "global.error.message"
  )(implicit request: Request[_]): Html = errorTemplate(titleKey, headingKey, messageKey)

  def internalServerError(implicit request: Request[_]): Result =
    InternalServerError(defaultErrorTemplate()).withHeaders(CACHE_CONTROL -> "no-cache")

  def notFound(implicit request: Request[_]): Result =
    NotFound(defaultErrorTemplate("global.error.pageNotFound.title", "global.error.pageNotFound.heading", "global.error.pageNotFound.message"))
      .withHeaders(CACHE_CONTROL -> "no-cache")
}
