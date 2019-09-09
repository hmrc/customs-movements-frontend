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

import controllers.{consolidations, routes, ChoiceController}
import forms.Choice
import forms.Choice._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.choice_page

import scala.concurrent.ExecutionContext.global

class ChoiceControllerSpec extends ControllerSpec with OptionValues with BeforeAndAfterEach {

  val mockChoicePage = mock[choice_page]

  val controller =
    new ChoiceController(mockAuthAction, mockCustomsCacheService, stubMessagesControllerComponents(), mockChoicePage)(
      global
    )

  override def beforeEach {
    super.beforeEach()

    authorizedUser()
    withCaching[Choice](Choice.choiceId, None)
    when(mockChoicePage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override def afterEach {
    reset(mockChoicePage)

    super.afterEach()
  }

  def theResponseForm: Form[Choice] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[Choice]])
    verify(mockChoicePage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  "Choice Controller on GET" should {

    "return 200 status code" in {

      val result = controller.displayChoiceForm()(getRequest())

      status(result) mustBe OK
      theResponseForm.value mustBe empty
    }

    "read item from cache and display it" in {

      val cachedChoice = Choice("EAL")
      withCaching[Choice](Choice.choiceId, Some(cachedChoice))

      val result = controller.displayChoiceForm()(getRequest())

      status(result) mustBe OK
      theResponseForm.value.value mustBe cachedChoice
    }
  }

  "ChoiceController on POST" should {

    "display the choice page with error" when {

      "no value provided for choice" in {

        val emptyForm: JsValue = JsObject(Map("" -> JsString("")))

        val result = controller.submitChoice()(postRequest(emptyForm))

        status(result) mustBe BAD_REQUEST
      }

      "wrong value provided for choice" in {

        val wrongForm = JsObject(Map("choice" -> JsString("test")))

        val result = controller.submitChoice()(postRequest(wrongForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "save the choice data to the cache" in {

      withCaching(Choice.choiceId)

      val validChoiceForm = JsObject(Map("choice" -> JsString("EDL")))
      val result = controller.submitChoice()(postRequest(validChoiceForm))

      status(result) mustBe SEE_OTHER

      verify(mockCustomsCacheService)
        .cache[Choice](any(), ArgumentMatchers.eq(Choice.choiceId), any())(any(), any(), any())
    }

    "redirect to arrival page when 'Arrival' is selected" in {

      withCaching(Choice.choiceId)

      val correctForm = JsObject(Map("choice" -> JsString(AllowedChoiceValues.Arrival)))

      val result = controller.submitChoice()(postRequest(correctForm))

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(routes.ConsignmentReferencesController.displayPage().url))
    }

    "redirect to departure page when 'Departure' is selected" in {

      withCaching(Choice.choiceId)

      val correctForm = JsObject(Map("choice" -> JsString(AllowedChoiceValues.Departure)))
      val result = controller.submitChoice()(postRequest(correctForm))

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(routes.ConsignmentReferencesController.displayPage().url))
    }

    "redirect to associate page when 'Associate' is selected" in {
      withCaching(Choice.choiceId)

      val associateChoice = JsObject(Map("choice" -> JsString(AllowedChoiceValues.AssociateDUCR)))
      val result = controller.submitChoice()(postRequest(associateChoice))

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(consolidations.routes.MucrOptionsController.displayPage().url))
    }

    "redirect to disassociate page when 'Disassociate' is selected" in {

      withCaching(Choice.choiceId)

      val correctForm = JsObject(Map("choice" -> JsString(AllowedChoiceValues.DisassociateDUCR)))
      val result = controller.submitChoice()(postRequest(correctForm))

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(consolidations.routes.DisassociateDucrController.displayPage().url))
    }

    "redirect to Shut a MUCR page when 'Shut a MUCR' is selected" in {

      withCaching(Choice.choiceId)

      val correctForm =
        JsObject(Map("choice" -> JsString(AllowedChoiceValues.ShutMucr)))
      val result = controller.submitChoice()(postRequest(correctForm))

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(consolidations.routes.ShutMucrController.displayPage().url))
    }

    "redirect to Movements submissions summary page when 'Inspect my movements' is selected" in {

      withCaching(Choice.choiceId)

      val correctForm =
        JsObject(Map("choice" -> JsString(AllowedChoiceValues.Submissions)))
      val result = controller.submitChoice()(postRequest(correctForm))

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(controllers.routes.MovementsController.displayPage().url))
    }
  }
}
