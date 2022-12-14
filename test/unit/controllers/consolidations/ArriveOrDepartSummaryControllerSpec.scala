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

package controllers.consolidations

import controllers.ControllerLayerSpec
import controllers.consolidations.routes.MovementConfirmationController
import forms.ConsignmentReferences
import models.cache.{ArrivalAnswers, DepartureAnswers, JourneyType, MovementAnswers}
import models.confirmation.{FlashKeys, SubmissionResult}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.libs.json.{JsObject, JsString}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.SubmissionService
import views.html.summary.{arrival_summary_page, departure_summary_page}

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class ArriveOrDepartSummaryControllerSpec extends ControllerLayerSpec with ScalaFutures with IntegrationPatience {

  private val submissionService = mock[SubmissionService]
  private val arrivalSummaryPage = mock[arrival_summary_page]
  private val departureSummaryPage = mock[departure_summary_page]

  private val conversationId = "conversationId"
  private val dummyUcr = "dummyUcr"
  private val dummyUcrType = "dummyUcrType"
  private val consignmentRefs = ConsignmentReferences(dummyUcrType, dummyUcr)
  private val submissionResult = SubmissionResult(conversationId, consignmentRefs)

  private def controller(answers: MovementAnswers) =
    new ArriveOrDepartSummaryController(
      SuccessfulAuth(),
      ValidJourney(answers),
      submissionService,
      stubMessagesControllerComponents(),
      arrivalSummaryPage,
      departureSummaryPage
    )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(submissionService, arrivalSummaryPage, departureSummaryPage)
    when(arrivalSummaryPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(departureSummaryPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(submissionService, arrivalSummaryPage, departureSummaryPage)

    super.afterEach()
  }

  private val emptyForm = JsObject(Map("" -> JsString("")))

  "Movement Summary Controller on displayPage" should {

    "return 200 (OK)" when {

      "cache contains data and user is during Arrival journey" in {
        val result = controller(ArrivalAnswers()).displayPage(getRequest())
        status(result) mustBe OK
      }

      "cache contains data and user is during Departure journey" in {
        val result = controller(DepartureAnswers()).displayPage(getRequest())
        status(result) mustBe OK
      }
    }
  }

  "Movement Summary Controller on submit" when {

    "everything works correctly and user is on Arrival journey" should {

      "call SubmissionService" in {
        when(submissionService.submit(any(), any[MovementAnswers])(any())).thenReturn(Future.successful(submissionResult))
        val cachedAnswers = ArrivalAnswers(Some(consignmentRefs))

        controller(cachedAnswers).submit()(postRequest(emptyForm)).futureValue

        val expectedEori = SuccessfulAuth().operator.eori
        verify(submissionService).submit(meq(expectedEori), meq(cachedAnswers))(any())
      }

      "return SEE_OTHER (303) that redirects to MovementConfirmationController" in {
        when(submissionService.submit(any(), any[MovementAnswers])(any())).thenReturn(Future.successful(submissionResult))

        val result = controller(ArrivalAnswers(Some(consignmentRefs))).submit()(postRequest(emptyForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(MovementConfirmationController.displayPage.url)
      }

      "return response with Movement Type, Conversation Id, UCR, and UCR Type in flash" in {
        when(submissionService.submit(any(), any[MovementAnswers])(any())).thenReturn(Future.successful(submissionResult))

        val result = controller(ArrivalAnswers(Some(consignmentRefs))).submit()(postRequest(emptyForm))

        flash(result).get(FlashKeys.JOURNEY_TYPE) mustBe Some(JourneyType.ARRIVE.toString)
        flash(result).get(FlashKeys.CONVERSATION_ID) mustBe Some(conversationId)
        flash(result).get(FlashKeys.UCR) mustBe Some(dummyUcr)
        flash(result).get(FlashKeys.UCR_TYPE) mustBe Some(dummyUcrType)
      }
    }

    "everything works correctly and user is on Departure journey" should {

      "call SubmissionService" in {
        when(submissionService.submit(any(), any[MovementAnswers])(any())).thenReturn(Future.successful(submissionResult))
        val cachedAnswers = DepartureAnswers(Some(consignmentRefs))

        controller(cachedAnswers).submit()(postRequest(emptyForm)).futureValue

        val expectedEori = SuccessfulAuth().operator.eori
        verify(submissionService).submit(meq(expectedEori), meq(cachedAnswers))(any())
      }

      "return SEE_OTHER (303) that redirects to MovementConfirmationController" in {
        when(submissionService.submit(any(), any[MovementAnswers])(any())).thenReturn(Future.successful(submissionResult))

        val result = controller(DepartureAnswers(Some(consignmentRefs))).submit()(postRequest(emptyForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(MovementConfirmationController.displayPage.url)
      }

      "return response with Movement Type, Conversation Id, UCR, and UCR Type in flash" in {
        when(submissionService.submit(any(), any[MovementAnswers])(any())).thenReturn(Future.successful(submissionResult))

        val result = controller(DepartureAnswers(Some(consignmentRefs))).submit()(postRequest(emptyForm))

        flash(result).get(FlashKeys.JOURNEY_TYPE) mustBe Some(JourneyType.DEPART.toString)
        flash(result).get(FlashKeys.CONVERSATION_ID) mustBe Some(conversationId)
        flash(result).get(FlashKeys.UCR) mustBe Some(dummyUcr)
        flash(result).get(FlashKeys.UCR_TYPE) mustBe Some(dummyUcrType)
      }
    }
  }
}
