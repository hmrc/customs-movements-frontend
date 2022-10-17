/*
 * Copyright 2022 HM Revenue & Customs
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

import config.IleQueryConfig
import controllers.consolidations.routes.ShutMucrController
import controllers.ileQuery.routes.FindConsignmentController
import controllers.routes.{DucrPartChiefController, SubmissionsController}
import forms.Choice._
import forms.UcrType.Mucr
import forms._
import models.UcrBlock
import models.cache._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import repository.MockCache
import views.html.choice

import scala.concurrent.ExecutionContext.global

class ChoiceControllerSpec extends ControllerLayerSpec with MockCache {

  private val choicePage = mock[choice]

  private val ileQueryConfig = mock[IleQueryConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(choicePage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(ileQueryConfig.isIleQueryEnabled).thenReturn(true)
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

  val controller = new ChoiceController(SuccessfulAuth(), cacheRepository, stubMessagesControllerComponents(), ileQueryConfig, choicePage)(global)

  "ChoiceController.displayChoices" should {

    "return 200 (OK)" when {

      "invoked with empty cache" in {
        givenTheCacheIsEmpty()
        val result = controller.displayChoices(getRequest())

        status(result) mustBe OK
        theResponseForm.value mustBe empty
      }

      "invoked with data in cache" in {
        givenTheCacheContains(Cache("eori", ArrivalAnswers()))
        val result = controller.displayChoices(getRequest())

        status(result) mustBe OK
        theResponseForm.value mustBe Some(Arrival)
      }

      "invoked with cache containing UcrBlock but no Answer" in {
        givenTheCacheContains(Cache("eori", UcrBlock(ucr = "ucr", ucrType = Mucr), false))

        val result = controller.displayChoices(getRequest())

        status(result) mustBe OK
        theResponseForm.value mustBe empty
      }

      "invoked with cache containing Answer and UcrBlock" in {
        givenTheCacheContains(Cache("eori", ArrivalAnswers(), UcrBlock(ucr = "ucr", ucrType = Mucr), true))

        val result = controller.displayChoices(getRequest())

        status(result) mustBe OK
        theResponseForm.value mustBe Some(Arrival)
      }
    }
  }

  "ChoiceController.submitChoice" should {

    "throw an IllegalArgumentException" when {
      "form is incorrect" in {
        val incorrectForm = JsObject(Map("choice" -> JsString("Incorrect")))

        val result = controller.submitChoice(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "throw a MatchError" when {
      "ileQuery is disabled and choice is Find Consignment" in {
        val findConsignmentForm = JsObject(Map("choice" -> JsString(FindConsignment.value)))

        when(ileQueryConfig.isIleQueryEnabled).thenReturn(false)
        intercept[MatchError] {
          controller.submitChoice(postRequest(findConsignmentForm))
        }
      }
    }

    "create cache and return 303 (SEE_OTHER)" when {

      "choice is Find Consignment" in {
        val findConsignmentForm = JsObject(Map("choice" -> JsString(FindConsignment.value)))

        val result = controller.submitChoice(postRequest(findConsignmentForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(FindConsignmentController.displayPage.url)
        theCacheUpserted.answers mustBe None
      }

      "choice is Arrival" in {
        val arrivalForm = JsObject(Map("choice" -> JsString(Arrival.value)))

        val result = controller.submitChoice(postRequest(arrivalForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(DucrPartChiefController.displayPage.url)
        theCacheUpserted.answers mustBe Some(ArrivalAnswers())
      }

      "choice is Departure" in {
        val departureForm = JsObject(Map("choice" -> JsString(Departure.value)))

        val result = controller.submitChoice(postRequest(departureForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(DucrPartChiefController.displayPage.url)
        theCacheUpserted.answers mustBe Some(DepartureAnswers())
      }

      "choice is Associate Ducr" in {
        val associateDUCRForm = JsObject(Map("choice" -> JsString(AssociateUCR.value)))

        val result = controller.submitChoice(postRequest(associateDUCRForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(DucrPartChiefController.displayPage.url)
        theCacheUpserted.answers mustBe Some(AssociateUcrAnswers())
      }

      "choice is Disassociate Ducr" in {
        val disassociateDUCRForm = JsObject(Map("choice" -> JsString(DisassociateUCR.value)))

        val result = controller.submitChoice(postRequest(disassociateDUCRForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(DucrPartChiefController.displayPage.url)
        theCacheUpserted.answers mustBe Some(DisassociateUcrAnswers())
      }

      "choice is Shut Mucr" in {
        val shutMucrForm = JsObject(Map("choice" -> JsString(ShutMUCR.value)))

        val result = controller.submitChoice(postRequest(shutMucrForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(ShutMucrController.displayPage.url)
        theCacheUpserted.answers mustBe Some(ShutMucrAnswers())
      }

      "choice is Submission" in {
        val submissionsForm = JsObject(Map("choice" -> JsString(Submissions.value)))

        val result = controller.submitChoice(postRequest(submissionsForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(SubmissionsController.displayPage.url)
        theCacheUpserted.answers mustBe None
      }
    }
  }
}
