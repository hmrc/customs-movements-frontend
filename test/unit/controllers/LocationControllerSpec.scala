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

import controllers.{routes, LocationController}
import forms.{Choice, Location}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.location

import scala.concurrent.ExecutionContext.global

class LocationControllerSpec extends ControllerSpec with OptionValues {

  private val mockLocationPage = mock[location]

  private val controller = new LocationController(
    mockAuthAction,
    mockJourneyAction,
    mockCustomsCacheService,
    stubMessagesControllerComponents(),
    mockLocationPage
  )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(mockLocationPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockLocationPage)

    super.afterEach()
  }

  private def theResponseForm: Form[Location] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[Location]])
    verify(mockLocationPage).apply(captor.capture(), any())(any(), any())
    captor.getValue
  }

  "Location Controller" should {

    "return 200 for get request" when {

      "cache is empty" in {

        givenAUserOnTheArrivalJourney()
        withCaching(Location.formId, None)

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        theResponseForm.value mustBe empty
      }

      "cache contains data" in {

        givenAUserOnTheArrivalJourney()
        val cachedData = Location("A", "Y", "locationCode", "PL")
        withCaching(Location.formId, Some(cachedData))

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        theResponseForm.value.value mustBe cachedData
      }

      "return Error" when {

        "no JourneyType found" in {

          withCaching(Choice.choiceId, None)
          withCaching(Location.formId, None)

          val result = controller.displayPage()(getRequest())

          status(result) mustBe CONFLICT
        }
      }
    }

    "return BadRequest for incorrect form" in {

      givenAUserOnTheArrivalJourney()
      withCaching(Location.formId)

      val incorrectForm: JsValue = Json.toJson(Location("incorrectValue", "Y", "locationCode", "PL"))

      val result = controller.saveLocation()(postRequest(incorrectForm))

      status(result) mustBe BAD_REQUEST
    }

    "redirect to transport page for correct form during arrival" in {

      givenAUserOnTheArrivalJourney()
      withCaching(Location.formId)

      val correctForm: JsValue = Json.toJson(Location("A", "Y", "locationCode", "PL"))

      val result = controller.saveLocation()(postRequest(correctForm))

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe routes.TransportController.displayPage().url
    }

    "redirect to goods departed page for correct form during departure" in {

      givenAUserOnTheDepartureJourney()
      withCaching(Location.formId)

      val correctForm: JsValue = Json.toJson(Location("A", "Y", "locationCode", "PL"))

      val result = controller.saveLocation()(postRequest(correctForm))

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe routes.GoodsDepartedController.displayPage().url
    }
  }
}
