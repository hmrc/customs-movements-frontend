/*
 * Copyright 2020 HM Revenue & Customs
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

package unit.controllers

import controllers.SummaryController
import controllers.storage.FlashKeys
import forms.ConsignmentReferences
import models.cache.{ArrivalAnswers, DepartureAnswers, JourneyType, MovementAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.libs.json.{JsObject, JsString}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.SubmissionService
import unit.repository.MockCache
import views.html.summary.{arrival_summary_page, departure_summary_page}

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class SummaryControllerSpec extends ControllerLayerSpec with MockCache {

  private val service = mock[SubmissionService]
  private val mockArrivalSummaryPage = mock[arrival_summary_page]
  private val mockDepartureSummaryPage = mock[departure_summary_page]

  private def controller(answers: MovementAnswers) =
    new SummaryController(
      SuccessfulAuth(),
      ValidJourney(answers),
      cache,
      service,
      stubMessagesControllerComponents(),
      mockArrivalSummaryPage,
      mockDepartureSummaryPage
    )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(mockArrivalSummaryPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockDepartureSummaryPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockArrivalSummaryPage, mockDepartureSummaryPage)
    super.afterEach()
  }

  private val emptyForm = JsObject(Map("" -> JsString("")))

  "Movement Summary Controller" should {

    "return 200 (OK)" when {

      "cache contains data and user is during arrival journey" in {
        val result = controller(ArrivalAnswers()).displayPage()(getRequest())

        status(result) mustBe OK
      }

      "cache contains data and user is during departure journey" in {
        val result = controller(DepartureAnswers()).displayPage()(getRequest())

        status(result) mustBe OK
      }

      "submission service return ACCEPTED during submission" in {
        when(service.submit(any(), any[MovementAnswers])(any())).thenReturn(Future.successful(ConsignmentReferences("ref", "value")))

        val result = controller(ArrivalAnswers()).submitMovementRequest()(postRequest(emptyForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.MovementConfirmationController.display().url)
        flash(result).get(FlashKeys.MOVEMENT_TYPE) mustBe Some(JourneyType.ARRIVE.toString)
        flash(result).get(FlashKeys.UCR_KIND) mustBe Some("ref")
        flash(result).get(FlashKeys.UCR) mustBe Some("value")
      }
    }
  }
}
