/*
 * Copyright 2024 HM Revenue & Customs
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

import controllers.consolidations.routes.{DisassociateUcrController, MucrOptionsController, ShutMucrController}
import controllers.ileQuery.routes.FindConsignmentController
import controllers.routes.{ConsignmentReferencesController, SubmissionsController}
import forms.Choice._
import forms.UcrType.Mucr
import forms._
import models.UcrBlock
import models.cache._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import play.api.data.Form
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import repository.MockCache
import views.html.choice

import scala.concurrent.ExecutionContext.global

class ChoiceControllerSpec extends ControllerLayerSpec with MockCache {

  private val choicePage = mock[choice]

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(choicePage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override def afterEach(): Unit = {
    reset(choicePage)
    super.afterEach()
  }

  private def theResponseForm: Form[Choice] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[Choice]])
    verify(choicePage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  val controller = new ChoiceController(SuccessfulAuth(), cacheRepository, stubMessagesControllerComponents(), choicePage)(global)

  "ChoiceController.displayChoices" should {

    "return 200 (OK)" when {

      "invoked with empty cache" in {
        givenTheCacheIsEmpty()
        val result = controller.displayChoices(getRequest())

        status(result) mustBe OK
        theResponseForm.value mustBe empty
      }

      "invoked with data in cache" in {
        val cache = Cache("eori", ArrivalAnswers())
        givenTheCacheContains(cache)
        val result = controller.displayChoices(getRequest(cache))

        status(result) mustBe OK
        theResponseForm.value mustBe Some(Arrival)
      }

      "invoked with cache containing UcrBlock but no Answer" in {
        val cache = Cache("eori", UcrBlock(ucr = "ucr", ucrType = Mucr), ucrBlockFromIleQuery = false)
        givenTheCacheContains(cache)

        val result = controller.displayChoices(getRequest(cache))

        status(result) mustBe OK
        theResponseForm.value mustBe empty
      }

      "invoked with cache containing Answer and UcrBlock" in {
        val cache = Cache("eori", ArrivalAnswers(), UcrBlock(ucr = "ucr", ucrType = Mucr), ucrBlockFromIleQuery = true)
        givenTheCacheContains(cache)

        val result = controller.displayChoices(getRequest(cache))

        status(result) mustBe OK
        theResponseForm.value mustBe Some(Arrival)
      }
    }
  }

  "ChoiceController.submitChoice" should {

    "return page not found" when {
      "path parameter incorrect" in {
        val result = controller.submitChoice("incorrect")(getRequest())
        status(result) mustBe NOT_FOUND
      }
    }

    "create cache and return 303 (SEE_OTHER)" when {

      "choice is Find Consignment" in {
        val result = controller.submitChoice(FindConsignment.value)(getRequest())
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(FindConsignmentController.displayPage.url)
        theCacheUpserted.answers mustBe None
      }

      "choice is Arrival" in {
        val result = controller.submitChoice(Arrival.value)(getRequest())
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(ConsignmentReferencesController.displayPage.url)
        theCacheUpserted.answers mustBe Some(ArrivalAnswers())
      }

      "choice is Departure" in {
        val result = controller.submitChoice(Departure.value)(getRequest())
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(ConsignmentReferencesController.displayPage.url)
        theCacheUpserted.answers mustBe Some(DepartureAnswers())
      }

      "choice is Associate Ducr" in {
        val result = controller.submitChoice(AssociateUCR.value)(getRequest())
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(MucrOptionsController.displayPage.url)
        theCacheUpserted.answers mustBe Some(AssociateUcrAnswers())
      }

      "choice is Disassociate Ducr" in {
        val result = controller.submitChoice(DisassociateUCR.value)(getRequest())
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(DisassociateUcrController.displayPage.url)
        theCacheUpserted.answers mustBe Some(DisassociateUcrAnswers())
      }

      "choice is Shut Mucr" in {
        val result = controller.submitChoice(ShutMUCR.value)(getRequest())
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(ShutMucrController.displayPage.url)
        theCacheUpserted.answers mustBe Some(ShutMucrAnswers())
      }

      "choice is Submission" in {
        val result = controller.submitChoice(Submissions.value)(getRequest())
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(SubmissionsController.displayPage.url)
        theCacheUpserted.answers mustBe None
      }
    }
  }
}
