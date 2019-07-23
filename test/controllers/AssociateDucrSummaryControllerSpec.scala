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

import base.MockFactory._
import base.{MockAuthConnector, URIHelper}
import controllers.exception.IncompleteApplication
import controllers.storage.FlashKeys
import forms.Choice.AllowedChoiceValues
import forms.{AssociateDucr, Choice, MucrOptions}
import org.mockito.ArgumentMatchers.{any, anyString, eq => meq}
import org.mockito.Mockito._
import org.mockito.{InOrder, Mockito}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.filters.csrf.{CSRFConfig, CSRFConfigProvider, CSRFFilter}
import services.{CustomsCacheService, SubmissionService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, SessionKeys}
import utils.FakeRequestCSRFSupport._

import scala.concurrent.Future

class AssociateDucrSummaryControllerSpec
    extends WordSpec with GuiceOneAppPerSuite with MockAuthConnector with ScalaFutures with MustMatchers
    with URIHelper {

  private val uri = uriWithContextPath("/associate-ducr-summary")

  private val submissionServiceMock = buildSubmissionServiceMock
  private val customsCacheServiceMock = buildCustomsCacheServiceMock
  override lazy val app: Application =
    GuiceApplicationBuilder()
      .overrides(
        bind[AuthConnector].to(authConnectorMock),
        bind[SubmissionService].to(submissionServiceMock),
        bind[CustomsCacheService].to(customsCacheServiceMock)
      )
      .build()

  private val cfg: CSRFConfig = app.injector.instanceOf[CSRFConfigProvider].get
  private val token: String = app.injector.instanceOf[CSRFFilter].tokenProvider.generateToken

  private val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  private implicit val messages: Messages = messagesApi.preferred(FakeRequest())

  private trait Test {
    implicit val headerCarrierMock = mock[HeaderCarrier]
    reset(authConnectorMock, submissionServiceMock, customsCacheServiceMock)
    when(customsCacheServiceMock.fetchAndGetEntry[Choice](any(), meq(Choice.choiceId))(any(), any(), any()))
      .thenReturn(Future.successful(Some(Choice(AllowedChoiceValues.AssociateDUCR))))
    when(customsCacheServiceMock.fetchAndGetEntry[MucrOptions](any(), meq(MucrOptions.formId))(any(), any(), any()))
      .thenReturn(Future.successful(Some(MucrOptions("MUCR"))))
    when(customsCacheServiceMock.fetchAndGetEntry[AssociateDucr](any(), meq(AssociateDucr.formId))(any(), any(), any()))
      .thenReturn(Future.successful(Some(AssociateDucr("DUCR"))))
    authorizedUser()
  }

  "Associate DUCR Summary  onGET" should {

    "throw incomplete application when MUCROptions cache empty" in new Test {

      when(customsCacheServiceMock.fetchAndGetEntry[MucrOptions](any(), meq(MucrOptions.formId))(any(), any(), any()))
        .thenReturn(Future.successful(None))

      assertThrows[IncompleteApplication] { await(routeGet()) }
    }

    "throw incomplete application when AssociateDUCR cache empty" in new Test {

      when(customsCacheServiceMock.fetchAndGetEntry[MucrOptions](any(), meq(AssociateDucr.formId))(any(), any(), any()))
        .thenReturn(Future.successful(None))

      assertThrows[IncompleteApplication] { await(routeGet()) }
    }

    "return Ok for GET request" in new Test {

      status(routeGet()) must be(OK)
    }

    def routeGet(headers: Map[String, String] = Map.empty): Future[Result] =
      route(app, FakeRequest(GET, uri).withHeaders(headers.toSeq: _*).withCSRFToken).get
  }

  "Associate DUCR Summary on POST" when {

    "MUCROptions cache is empty" should {
      "throw incomplete application" in new Test {

        when(customsCacheServiceMock.fetchAndGetEntry[MucrOptions](any(), meq(MucrOptions.formId))(any(), any(), any()))
          .thenReturn(Future.successful(None))

        assertThrows[IncompleteApplication] { await(routePost()) }
      }
    }

    "AssociateDUCR cache is empty" should {
      "throw incomplete application" in new Test {

        when(
          customsCacheServiceMock.fetchAndGetEntry[MucrOptions](any(), meq(AssociateDucr.formId))(any(), any(), any())
        ).thenReturn(Future.successful(None))

        assertThrows[IncompleteApplication] { await(routePost()) }
      }
    }

    "everything works correctly" should {

      "return SeeOther code" in new HappyPathTest {

        status(routePost()) must be(SEE_OTHER)
      }

      "call SubmissionService and CustomsCacheService in correct order" in new HappyPathTest {

        routePost().futureValue

        val inOrder: InOrder = Mockito.inOrder(submissionServiceMock, customsCacheServiceMock)
        inOrder
          .verify(customsCacheServiceMock)
          .fetchAndGetEntry[MucrOptions](any(), meq(MucrOptions.formId))(any(), any(), any())
        inOrder
          .verify(customsCacheServiceMock)
          .fetchAndGetEntry[AssociateDucr](any(), meq(AssociateDucr.formId))(any(), any(), any())
        inOrder
          .verify(submissionServiceMock)
          .submitDucrAssociation(meq(MucrOptions("MUCR")), meq(AssociateDucr("DUCR")))(any(), any())
        inOrder.verify(customsCacheServiceMock).remove(anyString)(any(), any())
      }

      "Redirect to next page" in new HappyPathTest {

        redirectLocation(routePost()) mustBe Some(routes.AssociateDucrConfirmationController.displayPage().url)
      }

      "add MUCR to Flash" in new HappyPathTest {

        flash(routePost()).get(FlashKeys.MUCR) mustBe Some("MUCR")
      }

      trait HappyPathTest extends Test {
        when(submissionServiceMock.submitDucrAssociation(any(), any())(any(), any()))
          .thenReturn(Future.successful(ACCEPTED))
        when(customsCacheServiceMock.remove(anyString())(any(), any()))
          .thenReturn(Future.successful(mock[HttpResponse]))
      }
    }

    "SubmissionService returns status other than Accepted" should {
      "return InternalServerError code" in new Test {
        when(submissionServiceMock.submitDucrAssociation(any(), any())(any(), any()))
          .thenReturn(Future.successful(BAD_REQUEST))
        when(customsCacheServiceMock.remove(anyString())(any(), any()))
          .thenReturn(Future.successful(mock[HttpResponse]))

        status(routePost()) must be(INTERNAL_SERVER_ERROR)
      }
    }

    def routePost(headers: Map[String, String] = Map.empty, body: JsValue = Json.obj()): Future[Result] =
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
