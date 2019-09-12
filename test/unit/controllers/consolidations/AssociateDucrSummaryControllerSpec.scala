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

package unit.controllers.consolidations

import base.MockSubmissionService
import controllers.consolidations.{routes, AssociateDucrSummaryController}
import controllers.exception.IncompleteApplication
import controllers.storage.FlashKeys
import forms.Choice.AllowedChoiceValues
import forms.{AssociateDucr, Choice, MucrOptions}
import org.mockito.ArgumentMatchers.{any, anyString, eq => meq}
import org.mockito.Mockito._
import org.mockito.{InOrder, Mockito}
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.HttpResponse
import unit.base.ControllerSpec
import views.html.associate_ducr_summary

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class AssociateDucrSummaryControllerSpec extends ControllerSpec with MockSubmissionService with ScalaFutures {

  private val mockAssociateDucrSummaryPage = mock[associate_ducr_summary]

  private val controller = new AssociateDucrSummaryController(
    mockAuthAction,
    mockJourneyAction,
    mockErrorHandler,
    stubMessagesControllerComponents(),
    mockCustomsCacheService,
    mockSubmissionService,
    mockAssociateDucrSummaryPage
  )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    setupErrorHandler()
    when(mockAssociateDucrSummaryPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)

    when(mockCustomsCacheService.fetchAndGetEntry[Choice](any(), meq(Choice.choiceId))(any(), any(), any()))
      .thenReturn(Future.successful(Some(Choice(AllowedChoiceValues.AssociateDUCR))))

    when(mockCustomsCacheService.fetchAndGetEntry[MucrOptions](any(), meq(MucrOptions.formId))(any(), any(), any()))
      .thenReturn(Future.successful(Some(MucrOptions("MUCR"))))

    when(mockCustomsCacheService.fetchAndGetEntry[AssociateDucr](any(), meq(AssociateDucr.formId))(any(), any(), any()))
      .thenReturn(Future.successful(Some(AssociateDucr("DUCR"))))
  }

  override protected def afterEach(): Unit = {
    reset(mockAssociateDucrSummaryPage)

    super.afterEach()
  }

  "Associate DUCR Summary on GET" should {

    "throw incomplete application when MUCROptions cache empty" in {

      when(mockCustomsCacheService.fetchAndGetEntry[MucrOptions](any(), meq(MucrOptions.formId))(any(), any(), any()))
        .thenReturn(Future.successful(None))

      assertThrows[IncompleteApplication] { await(controller.displayPage()(getRequest())) }
    }

    "throw incomplete application when AssociateDUCR cache empty" in {

      when(mockCustomsCacheService.fetchAndGetEntry[MucrOptions](any(), meq(AssociateDucr.formId))(any(), any(), any()))
        .thenReturn(Future.successful(None))

      assertThrows[IncompleteApplication] { await(controller.displayPage()(getRequest())) }
    }

    "return Ok for GET request" in {

      val result = controller.displayPage()(getRequest())

      status(result) mustBe OK
    }
  }

  "Associate DUCR Summary on POST" when {

    "MUCROptions cache is empty" should {
      "throw incomplete application" in {

        when(mockCustomsCacheService.fetchAndGetEntry[MucrOptions](any(), meq(MucrOptions.formId))(any(), any(), any()))
          .thenReturn(Future.successful(None))

        assertThrows[IncompleteApplication] { await(controller.submit()(postRequest(Json.obj()))) }
      }
    }

    "AssociateDUCR cache is empty" should {
      "throw incomplete application" in {

        when(
          mockCustomsCacheService.fetchAndGetEntry[MucrOptions](any(), meq(AssociateDucr.formId))(any(), any(), any())
        ).thenReturn(Future.successful(None))

        assertThrows[IncompleteApplication] { await(controller.submit()(postRequest(Json.obj()))) }
      }
    }

    "everything works correctly" should {

      "return SeeOther code" in {

        when(mockSubmissionService.submitDucrAssociation(any(), any())(any(), any()))
          .thenReturn(Future.successful(ACCEPTED))
        when(mockCustomsCacheService.remove(anyString())(any(), any()))
          .thenReturn(Future.successful(mock[HttpResponse]))

        val result = controller.submit()(postRequest(Json.obj()))

        status(result) mustBe SEE_OTHER
      }

      "call SubmissionService and CustomsCacheService in correct order" in {

        when(mockSubmissionService.submitDucrAssociation(any(), any())(any(), any()))
          .thenReturn(Future.successful(ACCEPTED))
        when(mockCustomsCacheService.remove(anyString())(any(), any()))
          .thenReturn(Future.successful(mock[HttpResponse]))

        controller.submit()(postRequest(Json.obj())).futureValue

        val inOrder: InOrder = Mockito.inOrder(mockSubmissionService, mockCustomsCacheService)
        inOrder
          .verify(mockCustomsCacheService)
          .fetchAndGetEntry[MucrOptions](any(), meq(MucrOptions.formId))(any(), any(), any())
        inOrder
          .verify(mockCustomsCacheService)
          .fetchAndGetEntry[AssociateDucr](any(), meq(AssociateDucr.formId))(any(), any(), any())
        inOrder
          .verify(mockSubmissionService)
          .submitDucrAssociation(meq(MucrOptions("MUCR")), meq(AssociateDucr("DUCR")))(any(), any())
        inOrder.verify(mockCustomsCacheService).remove(anyString)(any(), any())
      }

      "Redirect to next page" in {

        when(mockSubmissionService.submitDucrAssociation(any(), any())(any(), any()))
          .thenReturn(Future.successful(ACCEPTED))
        when(mockCustomsCacheService.remove(anyString())(any(), any()))
          .thenReturn(Future.successful(mock[HttpResponse]))

        val result = controller.submit()(postRequest(Json.obj()))

        redirectLocation(result) mustBe Some(routes.AssociateDucrConfirmationController.displayPage().url)
      }

      "add MUCR to Flash" in {

        when(mockSubmissionService.submitDucrAssociation(any(), any())(any(), any()))
          .thenReturn(Future.successful(ACCEPTED))
        when(mockCustomsCacheService.remove(anyString())(any(), any()))
          .thenReturn(Future.successful(mock[HttpResponse]))

        val result = controller.submit()(postRequest(Json.obj()))

        flash(result).get(FlashKeys.MUCR) mustBe Some("MUCR")
      }

    }

    "SubmissionService returns status other than Accepted" should {

      "return InternalServerError code" in {

        when(mockSubmissionService.submitDucrAssociation(any(), any())(any(), any()))
          .thenReturn(Future.successful(BAD_REQUEST))
        when(mockCustomsCacheService.remove(anyString())(any(), any()))
          .thenReturn(Future.successful(mock[HttpResponse]))

        val result = controller.submit()(postRequest(Json.obj()))

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
