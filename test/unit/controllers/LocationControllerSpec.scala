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

import controllers.{LocationController, routes}
import forms.Location
import models.cache.{ArrivalAnswers, DepartureAnswers, MovementAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.repository.MockCache
import views.html.location

import scala.concurrent.ExecutionContext.global

class LocationControllerSpec extends ControllerLayerSpec with MockCache with OptionValues {

  private val mockLocationPage = mock[location]

  private def controller(answers: MovementAnswers = ArrivalAnswers()) =
    new LocationController(SuccessfulAuth(), ValidJourney(answers), cache, stubMessagesControllerComponents(), mockLocationPage)(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(mockLocationPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockLocationPage)
    super.afterEach()
  }

  private def theResponseForm: Form[Location] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[Location]])
    verify(mockLocationPage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  "Location Controller" should {
    "return 200 (OK)" when {
      "display page method is invoked and cache is empty" in {
        val result = controller().displayPage()(getRequest())

        status(result) mustBe OK
        theResponseForm.value mustBe empty
      }

      "display page method is invoked and cache contains data" in {
        val cachedData = Location("PLAYlocationCode")

        val answers = ArrivalAnswers(location = Some(cachedData))
        val result = controller(answers).displayPage()(getRequest())

        status(result) mustBe OK
        theResponseForm.value.value mustBe cachedData
      }
    }

    "return 400 (BAD_REQUEST)" when {
      "form is incorrect" in {
        val incorrectForm: JsValue = Json.toJson(Location("PLincorrectYlocationCode"))

        val result = controller().saveLocation()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 303 (SEE_OTHER) and redirect to summary page" when {

      "form is correct and user is during arrival journey" in {
        val correctForm: JsValue = Json.toJson(Location("PLAYlocationCode"))

        val result = controller(ArrivalAnswers()).saveLocation()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.SummaryController.displayPage().url
      }
    }

    "return 303 (SEE_OTHER) and redirect to transport page" when {

      "form is correct and user is during departure journey" in {
        val correctForm: JsValue = Json.toJson(Location("PLAYlocationCode"))

        val result = controller(DepartureAnswers()).saveLocation()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.TransportController.displayPage().url
      }
    }
  }
}
