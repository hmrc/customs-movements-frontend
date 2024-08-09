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

import base.Injector
import controllers.ControllerLayerSpec
import controllers.exception.IncompleteApplication
import controllers.routes.{RootController, UnauthorisedController}
import models.AuthKey.enrolment
import models.ReturnToStartException
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.`given`
import org.mockito.MockitoSugar.{mock, reset}
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.http.{HeaderNames, Status}
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.{InsufficientEnrolments, NoActiveSession}
import views.html.error_template

import scala.concurrent.ExecutionContext.global

class ErrorHandlerSpec extends ControllerLayerSpec with Injector {

  private val errorTemplate = instanceOf[error_template]
  private val messagesApi = mock[MessagesApi]
  private val messages = new FakeMessages()
  private val request = FakeRequest("GET", "/foo")
  private val errorHandler = new ErrorHandler(appConfig, messagesApi, errorTemplate)(global)

  override def beforeEach(): Unit =
    given(messagesApi.preferred(any[RequestHeader]())).willReturn(messages)

  override def afterEach(): Unit = reset(messagesApi, appConfig)

  "ErrorHandler.resolveError" should {

    "handle incomplete application exception" in {
      val result = errorHandler.resolveError(request, IncompleteApplication).futureValue
      result.header.status must be(Status.SEE_OTHER)
      result.header.headers.get(HeaderNames.LOCATION) must be(Some(RootController.displayPage.url))
    }

    "handle return to start exception" in {
      val result = errorHandler.resolveError(request, ReturnToStartException).futureValue
      result.header.status must be(Status.SEE_OTHER)
      result.header.headers.get(HeaderNames.LOCATION) must be(Some(RootController.displayPage.url))
    }

    "handle no active session authorisation exception" in {
      given(appConfig.loginUrl).willReturn("login-url")
      given(appConfig.loginContinueUrl).willReturn("login-continue-url")

      val result = errorHandler.resolveError(request, new NoActiveSession("A user is not logged in") {}).futureValue

      result.header.status must be(Status.SEE_OTHER)
      result.header.headers.get(HeaderNames.LOCATION) must be(Some("login-url?continue=login-continue-url"))
    }

    "handle insufficient enrolments authorisation exception" in {
      val result = errorHandler.resolveError(request, InsufficientEnrolments(enrolment)).futureValue
      result.header.status must be(Status.SEE_OTHER)
      result.header.headers.get(HeaderNames.LOCATION) must be(Some(UnauthorisedController.onPageLoad.url))
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
