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

package handlers

import java.net.URLEncoder

import base.MovementBaseSpec
import config.AppConfig
import controllers.exception.IncompleteApplication
import models.ReturnToStartException
import play.api.http.{HeaderNames, Status}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.{InsufficientEnrolments, NoActiveSession}

import scala.concurrent.Future

class ErrorHandlerSpec extends MovementBaseSpec {

  val appConfig = app.injector.instanceOf[AppConfig]
  val errorPage = app.injector.instanceOf[views.html.error_template]

  val errorHandler = new ErrorHandler(appConfig, messagesApi, errorPage)
  val req = FakeRequest("GET", "/foo")

  "ErrorHandlerSpec" should {

    "standardErrorTemplate" in {

      val result = errorHandler
        .standardErrorTemplate("Page Title", "Heading", "Message")(FakeRequest())
        .body

      result must include("Page Title")
      result must include("Heading")
      result must include("Message")
    }

    "return Bad Request page" in {

      val result = contentAsString(Future.successful(errorHandler.getBadRequestPage()(FakeRequest())))

      result must include(messages("global.error.title"))
      result must include(messages("global.error.heading"))
      result must include(messages("global.error.message"))
    }

    "return Internal Server Error page" in {

      val result = contentAsString(Future.successful(errorHandler.getInternalServerErrorPage()(FakeRequest())))

      result must include(messages("global.error.title"))
      result must include(messages("global.error.heading"))
      result must include(messages("global.error.message"))
    }
  }
  "resolve error" should {

    def urlEncode(value: String): String = URLEncoder.encode(value, "UTF-8")

    "handle incomplete application exception" in {
      val res = errorHandler.resolveError(req, IncompleteApplication)
      res.header.status must be(Status.SEE_OTHER)
      res.header.headers.get(HeaderNames.LOCATION) must be(Some(controllers.routes.StartController.displayStartPage().url))
    }

    "handle return to start exception" in {
      val res = errorHandler.resolveError(req, ReturnToStartException)
      res.header.status must be(Status.SEE_OTHER)
      res.header.headers.get(HeaderNames.LOCATION) must be(Some(controllers.routes.StartController.displayStartPage().url))
    }

    "handle no active session authorisation exception" in {
      val res = errorHandler.resolveError(req, new NoActiveSession("A user is not logged in") {})
      res.header.status must be(Status.SEE_OTHER)
      res.header.headers.get(HeaderNames.LOCATION) must be(
        Some(s"http://localhost:9949/auth-login-stub/gg-sign-in?continue=${urlEncode("http://localhost:6796/customs-movements-frontend/start")}")
      )
    }

    "handle insufficient enrolments authorisation exception" in {
      val res =
        errorHandler.resolveError(req, InsufficientEnrolments("HMRC-CUS-ORG"))
      res.header.status must be(Status.SEE_OTHER)
      res.header.headers.get(HeaderNames.LOCATION) must be(Some(controllers.routes.UnauthorisedController.onPageLoad().url))
    }

  }

}
