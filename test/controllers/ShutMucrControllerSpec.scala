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

import java.util.UUID

import base.MockAuthConnector
import controllers.storage.FlashKeys
import forms.ShutMucrSpec._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsValue
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.filters.csrf.{CSRFConfig, CSRFConfigProvider, CSRFFilter}
import services.SubmissionService
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.SessionKeys
import utils.FakeRequestCSRFSupport._

import scala.concurrent.Future

class ShutMucrControllerSpec
    extends WordSpec with GuiceOneAppPerSuite with MockAuthConnector with ScalaFutures with MustMatchers {

  private val shutMucrUri = "/customs-movements/shut-mucr"

  private val submissionServiceMock = mock[SubmissionService]
  override lazy val app: Application =
    GuiceApplicationBuilder()
      .overrides(bind[AuthConnector].to(authConnectorMock), bind[SubmissionService].to(submissionServiceMock))
      .build()

  private val cfg: CSRFConfig = app.injector.instanceOf[CSRFConfigProvider].get
  private val token: String = app.injector.instanceOf[CSRFFilter].tokenProvider.generateToken

  private val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  private implicit val messages: Messages = messagesApi.preferred(FakeRequest())

  private trait Test {
    reset(authConnectorMock)
    when(submissionServiceMock.submitShutMucrRequest(any())(any(), any())).thenReturn(Future.successful(ACCEPTED))
  }

  "ShutMucr Controller on GET" should {

    "return Ok code" in new Test {

      authorizedUser()
      status(routeGet()) must be(OK)
    }

    def routeGet(headers: Map[String, String] = Map.empty): Future[Result] =
      route(app, FakeRequest(GET, shutMucrUri).withHeaders(headers.toSeq: _*).withCSRFToken).get
  }

  "ShutMucr Controller on POST" when {

    "provided with correct data" should {

      "return SeeOther code" in new Test {

        authorizedUser()
        status(routePost()) must be(SEE_OTHER)
      }

      "redirect to ShutMucrConfirmation page" in new Test {

        authorizedUser()
        redirectLocation(routePost()) must be(Some(routes.ShutMucrConfirmationController.displayPage().url))
      }

      "add MUCR to Flash" in new Test {

        authorizedUser()
        val flashValue = flash(routePost())
        flashValue.get(FlashKeys.MUCR) must be(defined)
        flashValue.get(FlashKeys.MUCR).get must equal(correctMucr)
      }
    }

    "provided with incorrect data" should {

      "return BadRequest code" in new Test {

        authorizedUser()
        status(routePost(body = incorrectShutMucrJSON)) must be(BAD_REQUEST)
      }

      "return Shut a MUCR page" in new Test {

        authorizedUser()
        val result = routePost(body = incorrectShutMucrJSON)

        contentAsString(result) must include(messages("shutMucr.title"))
      }
    }

    def routePost(headers: Map[String, String] = Map.empty, body: JsValue = correctShutMucrJSON): Future[Result] =
      route(
        app,
        FakeRequest(POST, shutMucrUri)
          .withHeaders((Map(cfg.headerName -> token) ++ headers).toSeq: _*)
          .withSession(Map(SessionKeys.sessionId -> s"session-${UUID.randomUUID()}").toSeq: _*)
          .withJsonBody(body)
          .withCSRFToken
      ).get
  }

}
