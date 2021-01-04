/*
 * Copyright 2021 HM Revenue & Customs
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

import controllers.actions.{DucrPartsAction, NonIleQueryAction}
import controllers.exception.InvalidFeatureStateException
import forms.DucrPartChiefChoice
import models.cache.ArrivalAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import repository.MockCache
import views.html.ducr_part_chief

import scala.concurrent.ExecutionContext.global

class DucrPartChiefControllerSpec extends ControllerLayerSpec with MockCache with OptionValues {

  private val ducrPartChiefPage = mock[ducr_part_chief]

  private def controller(
    ducrPartChiefChoice: Option[DucrPartChiefChoice] = None,
    ducrPartsAction: DucrPartsAction = DucrPartsEnabled,
    nonIleQueryAction: NonIleQueryAction = ValidForIleQuery
  ) =
    new DucrPartChiefController(
      SuccessfulAuth(),
      ValidJourney(ArrivalAnswers(), ducrPartChiefChoice = ducrPartChiefChoice),
      ducrPartsAction,
      nonIleQueryAction,
      cache,
      stubMessagesControllerComponents(),
      ducrPartChiefPage
    )(global)

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(ducrPartChiefPage)
    when(ducrPartChiefPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override def afterEach(): Unit = {
    reset(ducrPartChiefPage)

    super.afterEach()
  }

  private def theResponseForm: Form[DucrPartChiefChoice] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[DucrPartChiefChoice]])
    verify(ducrPartChiefPage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  "DucrPartChiefController on displayPage" when {

    "ducrParts feature is disabled" should {

      "throw InvalidFeatureStateException" in {
        intercept[InvalidFeatureStateException](await(controller(ducrPartsAction = DucrPartsDisabled).displayPage()(getRequest())))
      }
    }

    "ileQuery feature is enabled" should {

      "throw InvalidFeatureStateException" in {
        intercept[InvalidFeatureStateException](await(controller(nonIleQueryAction = NotValidForIleQuery).displayPage()(getRequest())))
      }
    }

    "return Ok (200) response" should {

      "display page method is invoked with empty cache" in {
        val result = controller().displayPage()(getRequest())
        status(result) mustBe OK

        theResponseForm.value mustBe empty
      }
    }

    "return 400 (BAD_REQUEST)" when {
      "form is incorrect" in {
        val incorrectForm: JsValue =
          JsObject(Map("choice" -> JsString("invalid")))

        val result = controller().submit()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 303 (SEE_OTHER)" when {
      "user selects yes" in {
        val incorrectForm: JsValue =
          JsObject(Map("choice" -> JsString("ducr_part_yes")))

        val result = controller().submit()(postRequest(incorrectForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.DucrPartDetailsController.displayPage().url
      }

      "user selects no" in {
        val incorrectForm: JsValue =
          JsObject(Map("choice" -> JsString("ducr_part_no")))

        val result = controller().submit()(postRequest(incorrectForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.ConsignmentReferencesController.displayPage().url
      }
    }

  }
}
