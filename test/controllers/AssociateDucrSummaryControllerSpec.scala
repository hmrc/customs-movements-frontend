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

import base.MockFactory._
import base.{MockAuthConnector, URIHelper}
import controllers.exception.IncompleteApplication
import controllers.storage.FlashKeys
import controllers.util.RoutingHelper
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
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{CustomsCacheService, SubmissionService}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

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

  private val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  private implicit val messages: Messages = messagesApi.preferred(FakeRequest())

  private val routingHelper = RoutingHelper(app, uri)

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

      assertThrows[IncompleteApplication] { await(routingHelper.routeGet()) }
    }

    "throw incomplete application when AssociateDUCR cache empty" in new Test {

      when(customsCacheServiceMock.fetchAndGetEntry[MucrOptions](any(), meq(AssociateDucr.formId))(any(), any(), any()))
        .thenReturn(Future.successful(None))

      assertThrows[IncompleteApplication] { await(routingHelper.routeGet()) }
    }

    "return Ok for GET request" in new Test {

      status(routingHelper.routeGet()) must be(OK)
    }
  }

  "Associate DUCR Summary on POST" when {

    "MUCROptions cache is empty" should {
      "throw incomplete application" in new Test {

        when(customsCacheServiceMock.fetchAndGetEntry[MucrOptions](any(), meq(MucrOptions.formId))(any(), any(), any()))
          .thenReturn(Future.successful(None))

        assertThrows[IncompleteApplication] { await(routingHelper.routePost(body = Json.obj())) }
      }
    }

    "AssociateDUCR cache is empty" should {
      "throw incomplete application" in new Test {

        when(
          customsCacheServiceMock.fetchAndGetEntry[MucrOptions](any(), meq(AssociateDucr.formId))(any(), any(), any())
        ).thenReturn(Future.successful(None))

        assertThrows[IncompleteApplication] { await(routingHelper.routePost(body = Json.obj())) }
      }
    }

    "everything works correctly" should {

      "return SeeOther code" in new HappyPathTest {

        status(routingHelper.routePost(body = Json.obj())) must be(SEE_OTHER)
      }

      "call SubmissionService and CustomsCacheService in correct order" in new HappyPathTest {

        routingHelper.routePost(body = Json.obj()).futureValue

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

        redirectLocation(routingHelper.routePost(body = Json.obj())) mustBe Some(
          routes.AssociateDucrConfirmationController.displayPage().url
        )
      }

      "add MUCR to Flash" in new HappyPathTest {

        flash(routingHelper.routePost(body = Json.obj())).get(FlashKeys.MUCR) mustBe Some("MUCR")
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

        status(routingHelper.routePost(body = Json.obj())) must be(INTERNAL_SERVER_ERROR)
      }
    }
  }

}
