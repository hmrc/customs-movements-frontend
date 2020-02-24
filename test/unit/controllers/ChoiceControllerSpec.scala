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

import controllers.consolidations.{routes => consolidationRoutes}
import controllers.{routes, ChoiceController}
import forms.Choice
import forms.Choice._
import models.cache.{ArrivalAnswers, Cache}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.repository.MockCache
import views.html.choice_page

import scala.concurrent.ExecutionContext.global

class ChoiceControllerSpec extends ControllerLayerSpec with MockCache {

  private val mockChoicePage = mock[choice_page]

  private val controller = new ChoiceController(SuccessfulAuth(), cache, stubMessagesControllerComponents(), mockChoicePage)(global)

  override def beforeEach() {
    super.beforeEach()
    givenTheCacheIsEmpty
    when(mockChoicePage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override def afterEach() {
    reset(mockChoicePage)

    super.afterEach()
  }

  private def theResponseForm: Form[Choice] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[Choice]])
    verify(mockChoicePage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  "Choice Controller" should {

    "return 200 (OK)" when {
      "display page method is invoked with empty cache" in {
        givenTheCacheIsEmpty()

        val result = controller.displayChoiceForm()(getRequest())

        status(result) mustBe OK
        theResponseForm.value mustBe empty
      }

      "display page method is invoked with data in cache" in {
        givenTheCacheContains(Cache("eori", ArrivalAnswers()))

        val result = controller.displayChoiceForm()(getRequest())

        status(result) mustBe OK
        theResponseForm.value mustBe Some(Arrival)
      }
    }

    "throw an IllegalArgumentException" when {
      "form is incorrect" in {
        val incorrectForm = JsObject(Map("choice" -> JsString("Incorrect")))

        val result = controller.submitChoice()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 303 (SEE_OTHER)" when {
      "choice is Arrival" in {
        val arrivalForm = JsObject(Map("choice" -> JsString(Arrival.value)))

        val result = controller.submitChoice()(postRequest(arrivalForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ConsignmentReferencesController.displayPage().url)
      }

      "choice is Departure" in {
        val departureForm = JsObject(Map("choice" -> JsString(Departure.value)))

        val result = controller.submitChoice()(postRequest(departureForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ConsignmentReferencesController.displayPage().url)
      }

      "choice is Associate Ducr" in {
        val associateDUCRForm = JsObject(Map("choice" -> JsString(AssociateUCR.value)))

        val result = controller.submitChoice()(postRequest(associateDUCRForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(consolidationRoutes.MucrOptionsController.displayPage().url)
      }

      "choice is Disassociate Ducr" in {
        val disassociateDUCRForm = JsObject(Map("choice" -> JsString(DisassociateUCR.value)))

        val result = controller.submitChoice()(postRequest(disassociateDUCRForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(consolidationRoutes.DisassociateUcrController.displayPage().url)
      }

      "choice is Shut Mucr" in {
        val shutMucrForm = JsObject(Map("choice" -> JsString(ShutMUCR.value)))

        val result = controller.submitChoice()(postRequest(shutMucrForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(consolidationRoutes.ShutMucrController.displayPage().url)
      }

      "choice is Submission" in {
        val submissionsForm = JsObject(Map("choice" -> JsString(Submissions.value)))

        val result = controller.submitChoice()(postRequest(submissionsForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.SubmissionsController.displayPage().url)
      }
    }

    "adapt" when {
      // TODO
    }
  }

  "Choice controller on startSpecificJourney method" should {
    "redirect to Consignment References page" when {
      "choice is arrival" in {
        val result = controller.startSpecificJourney(Arrival.value)(getRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ConsignmentReferencesController.displayPage().url)
      }

      "choice is departure" in {
        val result = controller.startSpecificJourney(Departure.value)(getRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ConsignmentReferencesController.displayPage().url)
      }
    }

    "redirect to Mucr Options page" when {
      "choice is association" in {
        val result = controller.startSpecificJourney(AssociateUCR.value)(getRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(consolidationRoutes.MucrOptionsController.displayPage().url)
      }
    }

    "redirect to Disassociate Ducr page" when {
      "choice is disassociation" in {
        val result = controller.startSpecificJourney(DisassociateUCR.value)(getRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(consolidationRoutes.DisassociateUcrController.displayPage().url)
      }
    }

    "redirect to Shut Mucr page" when {
      "choice is shut mucr" in {
        val result = controller.startSpecificJourney(ShutMUCR.value)(getRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(consolidationRoutes.ShutMucrController.displayPage().url)
      }
    }

    "redirect to Submissions page" when {
      "choice is submissions" in {
        val result = controller.startSpecificJourney(Submissions.value)(getRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.SubmissionsController.displayPage().url)
      }
    }
  }
}
