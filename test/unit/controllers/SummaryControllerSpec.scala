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

package unit.controllers

import base.MockSubmissionService
import controllers.SummaryController
import forms.Choice
import forms.Choice.ShutMUCR
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.cache.client.CacheMap
import unit.base.ControllerSpec
import views.html.movement_confirmation_page
import views.html.summary.{arrival_summary_page, departure_summary_page}

import scala.concurrent.ExecutionContext.global

class SummaryControllerSpec extends ControllerSpec with MockSubmissionService {

  private val mockArrivalSummaryPage = mock[arrival_summary_page]
  private val mockDepartureSummaryPage = mock[departure_summary_page]
  private val mockMovementConfirmationPage = mock[movement_confirmation_page]

  private val controller = new SummaryController(
    mockAuthAction,
    mockJourneyAction,
    mockErrorHandler,
    mockCustomsCacheService,
    mockSubmissionService,
    stubMessagesControllerComponents(),
    mockArrivalSummaryPage,
    mockDepartureSummaryPage,
    mockMovementConfirmationPage
  )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    setupErrorHandler()
    when(mockArrivalSummaryPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockDepartureSummaryPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockMovementConfirmationPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockArrivalSummaryPage, mockDepartureSummaryPage, mockMovementConfirmationPage)

    super.afterEach()
  }

  private val emptyForm = JsObject(Map("" -> JsString("")))

  "Movement Summary Controller" should {

    "return 200 (OK)" when {

      "cache contains data and user is during arrival journey" in {

        givenAUserOnTheArrivalJourney()
        withCacheMap(Some(CacheMap("id", Map.empty[String, JsValue])))

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
      }

      "cache contains data and user is during departure journey" in {

        givenAUserOnTheDepartureJourney()
        withCacheMap(Some(CacheMap("id", Map.empty[String, JsValue])))

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
      }

      "submission service return ACCEPTED during submission" in {

        givenAUserOnTheArrivalJourney()
        mockSubmission()
        mockCustomsCacheServiceClearedSuccessfully

        val result = controller.submitMovementRequest()(postRequest(emptyForm))

        status(result) must be(OK)
        verify(mockMovementConfirmationPage).apply(any())(any(), any())
      }
    }

    "return 500 (INTERNAL_SERVER_ERROR)" when {

      "there is no data in cache" in {

        givenAUserOnTheArrivalJourney()
        withCacheMap(None)

        val result = controller.displayPage()(getRequest())

        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      "user is on different type of journey" in {

        withCaching(Choice.choiceId, Some(ShutMUCR))
        withCacheMap(Some(CacheMap("id", Map.empty[String, JsValue])))

        val result = controller.displayPage()(getRequest())

        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      "submission service returned different status than ACCEPTED" in {

        givenAUserOnTheArrivalJourney()
        mockSubmission(BAD_REQUEST)

        val result = controller.submitMovementRequest()(postRequest(emptyForm))

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
