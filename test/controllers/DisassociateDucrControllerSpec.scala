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

import base.MockFactory.buildSubmissionServiceMock
import base.testdata.CommonTestData.correctUcr
import base.{MockAuthConnector, URIHelper}
import forms.DisassociateDucr
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.filters.csrf.{CSRFConfig, CSRFConfigProvider, CSRFFilter}
import services.SubmissionService
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.SessionKeys
import utils.FakeRequestCSRFSupport._

import scala.concurrent.Future

class DisassociateDucrControllerSpec
    extends WordSpec with GuiceOneAppPerSuite with MockAuthConnector with ScalaFutures with MustMatchers
    with URIHelper {

  import DisassociateDucrControllerSpec._

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  private val uri = uriWithContextPath("/disassociate-ducr")

  private val submissionServiceMock = buildSubmissionServiceMock
  override lazy val app: Application =
    GuiceApplicationBuilder()
      .overrides(bind[AuthConnector].to(authConnectorMock), bind[SubmissionService].to(submissionServiceMock))
      .build()

  private val cfg: CSRFConfig = app.injector.instanceOf[CSRFConfigProvider].get
  private val token: String = app.injector.instanceOf[CSRFFilter].tokenProvider.generateToken

  private val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  private implicit val messages: Messages = messagesApi.preferred(FakeRequest())

  private trait Test {
    reset(authConnectorMock, submissionServiceMock)
    when(submissionServiceMock.submitDucrDisassociation(any())(any(), any())).thenReturn(Future.successful(ACCEPTED))
    authorizedUser()
  }

  "Disassociate Ducr Controller on GET" should {

    "return 200 for get request" in new Test {

      status(routeGet()) must be(OK)
    }

    def routeGet(headers: Map[String, String] = Map.empty): Future[Result] =
      route(app, FakeRequest(GET, uri).withHeaders(headers.toSeq: _*).withCSRFToken).get
  }

  "Disassociate Ducr Controller on POST" when {

    "provided with correct data" should {

      "return SeeOther code" in new Test {

        status(routePost()) must be(SEE_OTHER)
      }

      "call SubmissionService" in new Test {

        routePost().futureValue

        verify(submissionServiceMock).submitDucrDisassociation(meq(DisassociateDucr(correctUcr)))(
          any(),
          any()
        )
      }

      "redirect to confirmation page" in new Test {

        redirectLocation(routePost()) must be(Some(routes.DisassociateDucrConfirmationController.displayPage().url))
        verify(submissionServiceMock).submitDucrDisassociation(meq(DisassociateDucr(correctUcr)))(
          any(),
          any()
        )
      }
    }

    "provided with incorrect data" should {

      "return BadRequest code" in new Test {

        status(routePost(body = incorrectForm)) must be(BAD_REQUEST)
      }

      "not call SubmissionService" in new Test {

        routePost(body = incorrectForm).futureValue

        verifyZeroInteractions(submissionServiceMock)
      }
    }

    "SubmissionService returns status other than Accepted" should {
      "return InternalServerError code" in new Test {
        when(submissionServiceMock.submitDucrDisassociation(any())(any(), any()))
          .thenReturn(Future.successful(BAD_REQUEST))

        status(routePost()) must be(INTERNAL_SERVER_ERROR)
      }
    }

    def routePost(headers: Map[String, String] = Map.empty, body: JsValue = correctForm): Future[Result] =
      route(
        app,
        FakeRequest(POST, uri)
          .withHeaders((Map(cfg.headerName -> token) ++ headers).toSeq: _*)
          .withSession(Map(SessionKeys.sessionId -> s"session-${UUID.randomUUID()}").toSeq: _*)
          .withJsonBody(body)
          .withCSRFToken
      ).get
  }
}

object DisassociateDucrControllerSpec {
  val correctForm = JsObject(Map("ducr" -> JsString(correctUcr)))
  val incorrectForm: JsValue = JsObject(Map("ducr" -> JsString("abc")))
}
