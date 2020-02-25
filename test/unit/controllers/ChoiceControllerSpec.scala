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
import forms.{AssociateKind, AssociateUcr, Choice, ConsignmentReferences, DisassociateKind, DisassociateUcr, ShutMucr, UcrType}
import forms.Choice._
import models.UcrBlock
import models.cache.{ArrivalAnswers, AssociateUcrAnswers, Cache, DepartureAnswers, DisassociateUcrAnswers, ShutMucrAnswers}
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
  private val ucrBlock = UcrBlock("MUCR", UcrType.Mucr)
  private val cacheWithUcr = Cache("eori", ucrBlock)

  private val controller = new ChoiceController(SuccessfulAuth(), cache, stubMessagesControllerComponents(), mockChoicePage)(global)

  override def beforeEach() {
    super.beforeEach()
    givenTheCacheIsEmpty()
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
      "choice is Arrival" should {
        val arrivalForm = JsObject(Map("choice" -> JsString(Arrival.value)))

        "create cache if empty" in {
          givenTheCacheIsEmpty()
          val result = controller.submitChoice()(postRequest(arrivalForm))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.ConsignmentReferencesController.displayPage().url)
          theCacheUpserted.answers mustBe Some(ArrivalAnswers(None, None, None))
        }

        "update cache if not empty" in {
          givenTheCacheContains(cacheWithUcr)
          val result = controller.submitChoice()(postRequest(arrivalForm))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.ConsignmentReferencesController.displayPage().url)
          theCacheUpserted.answers mustBe Some(ArrivalAnswers(Some(ConsignmentReferences(ucrBlock)), None, None))
        }
      }

      "choice is Departure" should {
        val departureForm = JsObject(Map("choice" -> JsString(Departure.value)))

        "create cache if empty" in {
          givenTheCacheIsEmpty()
          val result = controller.submitChoice()(postRequest(departureForm))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.ConsignmentReferencesController.displayPage().url)
          theCacheUpserted.answers mustBe Some(DepartureAnswers(None, None, None, None))
        }

        "update cache if not empty" in {
          givenTheCacheContains(cacheWithUcr)
          val result = controller.submitChoice()(postRequest(departureForm))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.ConsignmentReferencesController.displayPage().url)
          theCacheUpserted.answers mustBe Some(DepartureAnswers(Some(ConsignmentReferences(ucrBlock)), None, None, None))
        }
      }

      "choice is Associate Ducr" should {
        val associateDUCRForm = JsObject(Map("choice" -> JsString(AssociateUCR.value)))

        "create cache if empty" in {
          givenTheCacheIsEmpty()
          val result = controller.submitChoice()(postRequest(associateDUCRForm))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(consolidationRoutes.MucrOptionsController.displayPage().url)
          theCacheUpserted.answers mustBe Some(AssociateUcrAnswers(None, None))
        }

        "update cache if not empty" in {
          givenTheCacheContains(cacheWithUcr)
          val result = controller.submitChoice()(postRequest(associateDUCRForm))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(consolidationRoutes.MucrOptionsController.displayPage().url)
          theCacheUpserted.answers mustBe Some(AssociateUcrAnswers(None, Some(AssociateUcr(AssociateKind.Mucr, ucrBlock.ucr))))
        }
      }

      "choice is Disassociate Ducr" should {
        val disassociateDUCRForm = JsObject(Map("choice" -> JsString(DisassociateUCR.value)))

        "create cache if empty" in {
          givenTheCacheIsEmpty()
          val result = controller.submitChoice()(postRequest(disassociateDUCRForm))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(consolidationRoutes.DisassociateUcrController.displayPage().url)
          theCacheUpserted.answers mustBe Some(DisassociateUcrAnswers(None))
        }

        "update cache if not empty" in {
          givenTheCacheContains(cacheWithUcr)
          val result = controller.submitChoice()(postRequest(disassociateDUCRForm))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(consolidationRoutes.DisassociateUcrController.displayPage().url)
          theCacheUpserted.answers mustBe Some(DisassociateUcrAnswers(Some(DisassociateUcr(DisassociateKind.Mucr, None, Some(ucrBlock.ucr)))))
        }
      }

      "choice is Shut Mucr" should {
        val shutMucrForm = JsObject(Map("choice" -> JsString(ShutMUCR.value)))

        "create cache if empty" in {
          givenTheCacheIsEmpty()
          val result = controller.submitChoice()(postRequest(shutMucrForm))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(consolidationRoutes.ShutMucrController.displayPage().url)
          theCacheUpserted.answers mustBe Some(ShutMucrAnswers(None))
        }

        "update cache if not empty" in {
          givenTheCacheContains(cacheWithUcr)
          val result = controller.submitChoice()(postRequest(shutMucrForm))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(consolidationRoutes.ShutMucrController.displayPage().url)
          theCacheUpserted.answers mustBe Some(ShutMucrAnswers(Some(ShutMucr(ucrBlock.ucr))))
        }
      }

      "choice is Submission" in {
        val submissionsForm = JsObject(Map("choice" -> JsString(Submissions.value)))

        val result = controller.submitChoice()(postRequest(submissionsForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.SubmissionsController.displayPage().url)
      }
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
