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

import controllers.{routes, MovementDetailsController}
import forms.common.{Date, Time}
import forms.{ArrivalDetails, DepartureDetails, MovementDetails}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.{JsNumber, JsObject, JsString, JsValue}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.{arrival_details, departure_details}

import scala.concurrent.ExecutionContext.global

class MovementDetailsControllerSpec extends ControllerSpec with OptionValues {

  private val mockArrivalDetailsPage = mock[arrival_details]
  private val mockDepartureDetailsPage = mock[departure_details]

  private val controller = new MovementDetailsController(
    mockAuthAction,
    mockJourneyAction,
    mockCustomsCacheService,
    stubMessagesControllerComponents(),
    mockArrivalDetailsPage,
    mockDepartureDetailsPage
  )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(mockArrivalDetailsPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockDepartureDetailsPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockArrivalDetailsPage, mockDepartureDetailsPage)

    super.afterEach()
  }

  private def arrivalResponseForm: Form[ArrivalDetails] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[ArrivalDetails]])
    verify(mockArrivalDetailsPage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  private def departureResponseForm: Form[DepartureDetails] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[DepartureDetails]])
    verify(mockDepartureDetailsPage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  "Movement Details Controller for arrival journey" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in {

        givenAUserOnTheArrivalJourney()
        withCaching(MovementDetails.formId, None)

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        arrivalResponseForm.value mustBe empty
      }

      "display page method is invoked and cache contains data" in {

        givenAUserOnTheArrivalJourney()
        val cachedData = ArrivalDetails(Date(Some(10), Some(2), Some(2019)), Time(Some("10"), Some("10")))
        withCaching(MovementDetails.formId, Some(cachedData))

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        arrivalResponseForm.value.value mustBe cachedData
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect" in {

        givenAUserOnTheArrivalJourney()

        val incorrectForm: JsValue = JsObject(Map("dateOfArrival" -> JsString("")))

        val result = controller.saveMovementDetails()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 303 (SEE_OTHER) and redirect to location page" when {

      "form is correct" in {

        givenAUserOnTheArrivalJourney()
        withCaching(MovementDetails.formId)

        val correctDate: JsValue = JsObject(Map("day" -> JsNumber(10), "month" -> JsNumber(10), "year" -> JsNumber(2019)))
        val correctTime: JsValue = JsObject(Map("hour" -> JsString("10"), "minute" -> JsString("10")))

        val correctForm: JsValue = JsObject(Map("dateOfArrival" -> correctDate, "timeOfArrival" -> correctTime))

        val result = controller.saveMovementDetails()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.LocationController.displayPage().url
      }
    }
  }

  "Movement Details Controller for departure" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in {

        givenAUserOnTheDepartureJourney()
        withCaching(MovementDetails.formId, None)

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        departureResponseForm.value mustBe empty
      }

      "display page method is invoked and cache contains data" in {

        givenAUserOnTheDepartureJourney()
        val cachedData = DepartureDetails(Date(Some(10), Some(2), Some(2019)))
        withCaching(MovementDetails.formId, Some(cachedData))

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        departureResponseForm.value.value mustBe cachedData
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect" in {

        givenAUserOnTheDepartureJourney()

        val incorrectForm: JsValue = JsObject(Map("dateOfDeparture" -> JsString("")))

        val result = controller.saveMovementDetails()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 303 (SEE_OTHER) and redirect to transport page" when {

      "form is correct" in {

        givenAUserOnTheDepartureJourney()
        withCaching(MovementDetails.formId)

        val correctDate: JsValue = JsObject(Map("day" -> JsNumber(10), "month" -> JsNumber(10), "year" -> JsNumber(2019)))

        val correctForm: JsValue = JsObject(Map("dateOfDeparture" -> correctDate))

        val result = controller.saveMovementDetails()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.TransportController.displayPage().url
      }
    }
  }
}
