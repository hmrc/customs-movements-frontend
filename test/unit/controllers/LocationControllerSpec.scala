/*
 * Copyright 2023 HM Revenue & Customs
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

import controllers.summary.routes.ArriveOrDepartSummaryController
import forms.{ConsignmentReferences, Location}
import models.ReturnToStartException
import models.cache.{ArrivalAnswers, DepartureAnswers, MovementAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import repository.MockCache
import views.html.location

import scala.concurrent.ExecutionContext.global

class LocationControllerSpec extends ControllerLayerSpec with MockCache with OptionValues {

  private val mockLocationPage = mock[location]
  private val consignmentReferences = ConsignmentReferences("reference", "referenceValue")

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(mockLocationPage.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockLocationPage)
    super.afterEach()
  }

  private def controller(answers: MovementAnswers = ArrivalAnswers()) =
    new LocationController(SuccessfulAuth(), ValidJourney(answers), cacheRepository, stubMessagesControllerComponents(), mockLocationPage, navigator)(
      global
    )

  private def theResponseForm: Form[Location] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[Location]])
    verify(mockLocationPage).apply(captor.capture(), any(), any())(any(), any())
    captor.getValue
  }

  "Location Controller on displayPage" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in {
        val answers = ArrivalAnswers(consignmentReferences = Some(consignmentReferences))

        val result = controller(answers).displayPage(getRequest())

        status(result) mustBe OK
        theResponseForm.value mustBe empty
      }

      "display page method is invoked and cache contains data" in {
        val cachedData = Location("PLAYlocationCode")
        val answers = ArrivalAnswers(location = Some(cachedData), consignmentReferences = Some(consignmentReferences))

        val result = controller(answers).displayPage(getRequest())

        status(result) mustBe OK
        theResponseForm.value.value mustBe cachedData
      }
    }

    "return to start" when {
      "consignment reference is missing" in {
        intercept[RuntimeException] {
          await(controller().displayPage(getRequest()))
        } mustBe ReturnToStartException
      }
    }
  }

  "Location Controller on saveLocation" should {

    "return 303 (SEE_OTHER) and redirect to summary page" when {
      "form is correct and user is during arrival journey" in {
        val correctForm: JsValue = Json.toJson(Location("PLAYlocationCode"))
        val answers = ArrivalAnswers(consignmentReferences = Some(consignmentReferences))

        val result = controller(answers).saveLocation()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe ArriveOrDepartSummaryController.displayPage.url
      }
    }

    "return 303 (SEE_OTHER) and redirect to transport page" when {
      "form is correct and user is during departure journey" in {
        val correctForm: JsValue = Json.toJson(Location("PLAYlocationCode"))
        val answers = DepartureAnswers(consignmentReferences = Some(consignmentReferences))

        val result = controller(answers).saveLocation()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        thePageNavigatedTo.url mustBe routes.TransportController.displayPage.url
      }
    }

    "return 400 (BAD_REQUEST)" when {
      "form is incorrect" in {
        val incorrectForm: JsValue = Json.toJson(Location("PLincorrectYlocationCode"))
        val answers = ArrivalAnswers(consignmentReferences = Some(consignmentReferences))

        val result = controller(answers).saveLocation()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return to start" when {
      "consignment reference is missing" in {
        val correctForm: JsValue = Json.toJson(Location("PLAYlocationCode"))

        intercept[RuntimeException] {
          await(controller(DepartureAnswers()).saveLocation()(postRequest(correctForm)))
        } mustBe ReturnToStartException
      }
    }
  }
}
