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

package controllers.summary

import controllers.ControllerLayerSpec
import controllers.summary.routes.MovementConfirmationController
import forms.ShutMucr
import models.ReturnToStartException
import models.cache.{JourneyType, ShutMucrAnswers}
import models.requests.SessionHelper
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.libs.json.JsObject
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.SubmissionService
import testdata.CommonTestData.validMucr
import views.html.summary.shut_mucr_summary

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class ShutMucrSummaryControllerSpec extends ControllerLayerSpec with ScalaFutures with IntegrationPatience {

  private val submissionService = mock[SubmissionService]
  private val page = mock[shut_mucr_summary]

  private def controller(answers: ShutMucrAnswers) =
    new ShutMucrSummaryController(SuccessfulAuth(), ValidJourney(answers), stubMessagesControllerComponents(), submissionService, page)(global)

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(submissionService, page)
    when(page.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  private val shutMucr = ShutMucr(validMucr)

  "ShutMucrSummaryController.displayPage" should {

    "return 200 (OK)" when {

      "cache contains information from shut mucr page" in {
        val result = controller(ShutMucrAnswers(Some(shutMucr))).displayPage(getRequest())

        status(result) mustBe OK
        verify(page).apply(any())(any(), any())
      }
    }

    "throw an exception" when {

      "cache is empty for displayPage method" in {
        intercept[RuntimeException] {
          await(controller(ShutMucrAnswers()).displayPage(getRequest()))
        } mustBe ReturnToStartException
      }
    }
  }

  "ShutMucrSummaryController.submit" when {

    "everything works correctly" should {
      val conversationId = "conversationId"

      "call SubmissionService" in {
        when(submissionService.submit(any(), any[ShutMucrAnswers], any())(any())).thenReturn(Future.successful(conversationId))
        val cachedAnswers = ShutMucrAnswers(shutMucr = Some(shutMucr))

        controller(cachedAnswers).submit(postRequest()).futureValue

        val expectedEori = SuccessfulAuth().operator.eori
        verify(submissionService).submit(meq(expectedEori), meq(cachedAnswers), any())(any())
      }

      "return 303 (SEE_OTHER) that redirects to ShutMucrConfirmationController" in {
        when(submissionService.submit(any(), any[ShutMucrAnswers], any())(any())).thenReturn(Future.successful(conversationId))

        val result = controller(ShutMucrAnswers(Some(shutMucr))).submit(postRequest(JsObject(Seq.empty)))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(MovementConfirmationController.displayPage.url)
      }

      "return response with Movement Type and Conversation Id in flash" in {
        when(submissionService.submit(any(), any[ShutMucrAnswers], any())(any())).thenReturn(Future.successful(conversationId))

        val result = controller(ShutMucrAnswers(Some(shutMucr))).submit(postRequest(JsObject(Seq.empty)))

        session(result).get(SessionHelper.JOURNEY_TYPE) mustBe Some(JourneyType.SHUT_MUCR.toString)
        session(result).get(SessionHelper.CONVERSATION_ID) mustBe Some(conversationId)
      }
    }
  }
}
