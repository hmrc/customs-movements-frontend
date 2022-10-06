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

package handlers

import config.AppConfig
import controllers.ControllerLayerSpec
import controllers.exception.IncompleteApplication
import controllers.routes.RootController
import models.ReturnToStartException
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito._
import play.api.http.{HeaderNames, Status}
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.auth.core.{InsufficientEnrolments, NoActiveSession}

import scala.concurrent.Future.successful

class ErrorHandlerSpec extends ControllerLayerSpec {

  private val appConfig = mock[AppConfig]
  private val errorPage = mock[views.html.error_template]
  private val errorPageHTML = HtmlFormat.empty
  private val messagesApi = mock[MessagesApi]
  private val messages = new FakeMessages()
  private val req = FakeRequest("GET", "/foo")
  private val errorHandler = new ErrorHandler(appConfig, messagesApi, errorPage)

  override def beforeEach(): Unit = {
    given(errorPage.apply(any(), any(), any())(any(), any())).willReturn(errorPageHTML)
    given(messagesApi.preferred(any[RequestHeader]())).willReturn(messages)
  }

  override def afterEach(): Unit =
    reset(errorPage, messagesApi, appConfig)

  "ErrorHandlerSpec" should {

    "standardErrorTemplate" in {
      errorHandler.standardErrorTemplate("Page Title", "Heading", "Message")(FakeRequest()) mustBe errorPageHTML

      verify(errorPage).apply(refEq("Page Title"), refEq("Heading"), refEq("Message"))(any(), any())
    }

    "return Bad Request page" in {
      val result = successful(errorHandler.getBadRequestPage()(FakeRequest()))

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe errorPageHTML.toString()

      verify(errorPage).apply(
        refEq(messages("global.error.title")),
        refEq(messages("global.error.heading")),
        refEq(messages("global.error.message"))
      )(any(), any())
    }
  }

  "resolve error" should {

    "handle incomplete application exception" in {
      val res = errorHandler.resolveError(req, IncompleteApplication)
      res.header.status must be(Status.SEE_OTHER)
      res.header.headers.get(HeaderNames.LOCATION) must be(Some(RootController.displayPage.url))
    }

    "handle return to start exception" in {
      val res = errorHandler.resolveError(req, ReturnToStartException)
      res.header.status must be(Status.SEE_OTHER)
      res.header.headers.get(HeaderNames.LOCATION) must be(Some(RootController.displayPage.url))
    }

    "handle no active session authorisation exception" in {
      given(appConfig.loginUrl).willReturn("login-url")
      given(appConfig.loginContinueUrl).willReturn("login-continue-url")

      val res = errorHandler.resolveError(req, new NoActiveSession("A user is not logged in") {})

      res.header.status must be(Status.SEE_OTHER)
      res.header.headers.get(HeaderNames.LOCATION) must be(Some("login-url?continue=login-continue-url"))
    }

    "handle insufficient enrolments authorisation exception" in {
      val res =
        errorHandler.resolveError(req, InsufficientEnrolments("HMRC-CUS-ORG"))
      res.header.status must be(Status.SEE_OTHER)
      res.header.headers.get(HeaderNames.LOCATION) must be(Some(controllers.routes.UnauthorisedController.onPageLoad.url))
    }
  }

  class FakeMessages extends Messages {
    override def asJava: play.i18n.Messages = new play.i18n.MessagesImpl(lang.asJava, messagesApi.asJava)
    def lang: Lang = mock[Lang]
    def apply(key: String, args: Any*): String = s"messages($key, ${args.mkString(",")})"
    def apply(keys: Seq[String], args: Any*): String = keys.map(apply(_, args)).mkString(",")
    def translate(key: String, args: Seq[Any]): Option[String] = None
    def isDefinedAt(key: String): Boolean = true
  }
}
