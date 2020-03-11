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

package controllers

import config.AppConfig
import controllers.actions.NonIleQueryAction
import controllers.exception.InvalidFeatureStateException
import forms.ConsignmentReferences
import models.cache.{ArrivalAnswers, DepartureAnswers, MovementAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import repository.MockCache
import views.html.consignment_references

import scala.concurrent.ExecutionContext.Implicits.global

class ConsignmentReferencesControllerSpec extends ControllerLayerSpec with MockCache with OptionValues {

  private val mockConsignmentReferencePage = mock[consignment_references]

  private def controller(answers: MovementAnswers, nonIleQueryAction: NonIleQueryAction) =
    new ConsignmentReferencesController(
      SuccessfulAuth(),
      ValidJourney(answers),
      nonIleQueryAction,
      cache,
      stubMessagesControllerComponents(),
      mockConsignmentReferencePage
    )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
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
        val result = controller(ArrivalAnswers(), ValidForIleQuery).displayPage()(getRequest())

        status(result) mustBe OK
        theResponseForm.value mustBe empty
      }

      "display page method is invoked and cache contains data" when {
        "on arrival journey" in {
          val cachedData = ConsignmentReferences("D", "123456")

          val answers = ArrivalAnswers(Some(cachedData))
          val result = controller(answers, ValidForIleQuery).displayPage()(getRequest())

          status(result) mustBe OK
          theResponseForm.value.value mustBe cachedData
        }

        "on departure journey" in {
          val cachedData = ConsignmentReferences("D", "123456")

          val answers = DepartureAnswers(Some(cachedData))
          val result = controller(answers, ValidForIleQuery).displayPage()(getRequest())

          status(result) mustBe OK
          theResponseForm.value.value mustBe cachedData
        }
      }
    }

    "return 400 (BAD_REQUEST)" when {
      "form is incorrect" in {
        val incorrectForm: JsValue =
          JsObject(Map("eori" -> JsString("GB717572504502811"), "reference" -> JsString("reference"), "referenceValue" -> JsString("")))

        val result = controller(ArrivalAnswers(), ValidForIleQuery).saveConsignmentReferences()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 303 (SEE_OTHER) and redirect to goods date page" when {
      "form is correct during arrival journey" in {
        val correctForm: JsValue =
          JsObject(
            Map(
              "eori" -> JsString("GB717572504502811"),
              "reference" -> JsString("D"),
              "mucrValue" -> JsString(""),
              "ducrValue" -> JsString("5GB123456789000-123ABC456DEFIIIII")
            )
          )

        val result = controller(ArrivalAnswers(), ValidForIleQuery).saveConsignmentReferences()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.SpecificDateTimeController.displayPage().url
      }
    }

    "return 303 (SEE_OTHER) and redirect to movement details page" when {
      "form is correct during departure journey" in {
        val correctForm: JsValue =
          JsObject(
            Map(
              "eori" -> JsString("GB717572504502811"),
              "reference" -> JsString("D"),
              "mucrValue" -> JsString(""),
              "ducrValue" -> JsString("5GB123456789000-123ABC456DEFIIIII")
            )
          )

        val result = controller(DepartureAnswers(), ValidForIleQuery).saveConsignmentReferences()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.SpecificDateTimeController.displayPage().url
      }
    }

    "block access" when {
      "ileQuery enabled" in {
        intercept[RuntimeException] {
          await(controller(ArrivalAnswers(), NotValidForIleQuery).displayPage()(getRequest))
        } mustBe InvalidFeatureStateException
      }
    }
  }
}
