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

import controllers.{routes, ConsignmentReferencesController}
import forms.ConsignmentReferences
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.consignment_references

import scala.concurrent.ExecutionContext.global

class ConsignmentReferencesControllerSpec extends ControllerSpec with OptionValues {

  private val mockConsignmentReferencePage = mock[consignment_references]

  private val controller = new ConsignmentReferencesController(
    mockAuthAction,
    mockJourneyAction,
    mockCustomsCacheService,
    stubMessagesControllerComponents(),
    mockConsignmentReferencePage
  )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(mockConsignmentReferencePage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockConsignmentReferencePage)

    super.afterEach()
  }

  private def theResponseForm: Form[ConsignmentReferences] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[ConsignmentReferences]])
    verify(mockConsignmentReferencePage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  "Consignment Reference Controller" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in {

        givenAUserOnTheArrivalJourney()
        withCaching(ConsignmentReferences.formId, None)

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        theResponseForm.value mustBe empty
      }

      "display page method is invoked and cache contains data" in {

        givenAUserOnTheArrivalJourney()
        val cachedData = ConsignmentReferences("D", "123456")
        withCaching(ConsignmentReferences.formId, Some(cachedData))

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        theResponseForm.value.value mustBe cachedData
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect" in {

        givenAUserOnTheArrivalJourney()

        val incorrectForm: JsValue = JsObject(
          Map(
            "eori" -> JsString("GB717572504502811"),
            "reference" -> JsString("reference"),
            "referenceValue" -> JsString("")
          )
        )

        val result = controller.saveConsignmentReferences()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 303 (SEE_OTHER) and redirect to goods date page" when {

      "form is correct during arrival journey" in {

        givenAUserOnTheArrivalJourney()
        withCaching(ConsignmentReferences.formId)

        val correctForm: JsValue =
          JsObject(
            Map(
              "eori" -> JsString("GB717572504502811"),
              "reference" -> JsString("D"),
              "referenceValue" -> JsString("5GB123456789000-123ABC456DEFIIIII")
            )
          )

        val result = controller.saveConsignmentReferences()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.ArrivalReferenceController.displayPage().url
      }
    }

    "return 303 (SEE_OTHER) and redirect to location page" when {

      "form is correct during departure journey" in {

        givenAUserOnTheDepartureJourney()
        withCaching(ConsignmentReferences.formId)

        val correctForm: JsValue =
          JsObject(
            Map(
              "eori" -> JsString("GB717572504502811"),
              "reference" -> JsString("D"),
              "referenceValue" -> JsString("5GB123456789000-123ABC456DEFIIIII")
            )
          )

        val result = controller.saveConsignmentReferences()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.LocationController.displayPage().url
      }
    }
  }
}
