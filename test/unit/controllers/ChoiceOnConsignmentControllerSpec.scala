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

import controllers.actions.ArriveDepartAllowList
import controllers.consolidations.routes.{DisassociateUcrSummaryController, ManageMucrController, ShutMucrSummaryController}
import controllers.routes.{ChoiceController, ChoiceOnConsignmentController, SpecificDateTimeController}
import forms.Choice._
import forms.UcrType.Mucr
import forms._
import models.UcrBlock
import models.cache._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.Assertion
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.{Call, Result}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import repository.MockCache
import views.html.choice_on_consignment

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class ChoiceOnConsignmentControllerSpec extends ControllerLayerSpec with MockCache {

  private val arriveDepartAllowList = mock[ArriveDepartAllowList]

  private val choicePage = mock[choice_on_consignment]

  private val controller =
    new ChoiceOnConsignmentController(SuccessfulAuth(), cacheRepository, stubMessagesControllerComponents(), arriveDepartAllowList, choicePage)(
      global
    )

  override def beforeEach() {
    super.beforeEach()
    givenTheCacheIsEmpty()
    when(arriveDepartAllowList.contains(any())).thenReturn(true)
    when(choicePage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override def afterEach() {
    reset(arriveDepartAllowList, choicePage)

    super.afterEach()
  }

  private def genCache(ucrType: UcrType = Mucr): Cache =
    Cache("eori", UcrBlock(ucr = "ucr", ucrType = ucrType), true)

  private val cache = genCache()

  private def theResponseForm: Form[Choice] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[Choice]])
    verify(choicePage).apply(captor.capture(), any())(any(), any())
    captor.getValue
  }

  "ChoiceOnConsignmentController.displayChoices" should {

    "return 200 (OK)" when {

      "invoked with no answer in cache" in {
        givenTheCacheContains(cache)
        status(controller.displayChoices(getRequest())) mustBe OK
        theResponseForm.value mustBe empty
      }

      "invoked with answer in cache" in {
        givenTheCacheContains(Cache("eori", ArrivalAnswers(), UcrBlock(ucr = "ucr", ucrType = Mucr), true))
        status(controller.displayChoices(getRequest())) mustBe OK
        theResponseForm.value mustBe Some(Arrival)
      }
    }

    "redirect to the /choice page" when {

      "there's no cache for the eori" in {
        givenTheCacheIsEmpty()
        testChoicePageRedirect(controller.displayChoices(getRequest()))
      }

      "the cache does not contain Answer and UcrBlock" in {
        givenTheCacheContains(Cache("eori"))
        testChoicePageRedirect(controller.displayChoices(getRequest()))
      }

      "the cache contains Answer but not UcrBlock" in {
        givenTheCacheContains(Cache("eori", ArrivalAnswers()))
        testChoicePageRedirect(controller.displayChoices(getRequest()))
      }
    }
  }

  "ChoiceOnConsignmentController.submitChoice" should {

    "throw an IllegalArgumentException" when {
      "form is incorrect" in {
        givenTheCacheContains(cache)
        val incorrectForm = JsObject(Map("choice" -> JsString("Incorrect")))
        status(controller.submitChoice(postRequest(incorrectForm))) mustBe BAD_REQUEST
      }
    }

    "redirect to the /choice page" when {
      val request = postRequest(JsObject(Map("choice" -> JsString(Arrival.value))))

      "there's no cache for the eori" in {
        givenTheCacheIsEmpty()
        testChoicePageRedirect(controller.submitChoice(request))
      }

      "the cache does not contain Answer and UcrBlock" in {
        givenTheCacheContains(Cache("eori"))
        testChoicePageRedirect(controller.submitChoice(request))
      }

      "the cache contains Answer but not UcrBlock" in {
        givenTheCacheContains(Cache("eori", ArrivalAnswers()))
        testChoicePageRedirect(controller.submitChoice(request))
      }
    }

    "not be redirected" when {
      "the user's eori is not in the ArriveDepartAllowList and" when {

        "choice is Arrival" in {
          when(arriveDepartAllowList.contains(any())).thenReturn(false)

          givenTheCacheContains(cache)
          val arrivalForm = JsObject(Map("choice" -> JsString(Arrival.value)))
          val result = controller.submitChoice(postRequest(arrivalForm))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(ChoiceOnConsignmentController.displayChoices.url)
        }

        "choice is Departure" in {
          when(arriveDepartAllowList.contains(any())).thenReturn(false)

          givenTheCacheContains(cache)
          val departureForm = JsObject(Map("choice" -> JsString(Departure.value)))
          val result = controller.submitChoice(postRequest(departureForm))

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(ChoiceOnConsignmentController.displayChoices.url)
        }
      }
    }

    "update cache and return 303 (SEE_OTHER)" when {
      "the user's eori is present in the ArriveDepartAllowList and" when {

        "choice is Arrival" in {
          val expectedAnswer = testRedirection(Arrival, SpecificDateTimeController.displayPage)
          expectedAnswer mustBe ArrivalAnswers(Some(ConsignmentReferences(cache.ucrBlock.get)), None, None)
        }

        "choice is Departure" in {
          val expectedAnswer = testRedirection(Departure, SpecificDateTimeController.displayPage)
          expectedAnswer mustBe DepartureAnswers(Some(ConsignmentReferences(cache.ucrBlock.get)), None, None, None)
        }

        "choice is Associate Ducr" in {
          val expectedAnswer = testRedirection(AssociateUCR, ManageMucrController.displayPage)
          expectedAnswer mustBe AssociateUcrAnswers(None, None, Some(AssociateUcr(Mucr, cache.ucrBlock.get.ucr)))
        }

        "choice is Disassociate Ducr" in {
          val expectedAnswer = testRedirection(DisassociateUCR, DisassociateUcrSummaryController.displayPage)
          expectedAnswer mustBe DisassociateUcrAnswers(Some(DisassociateUcr(Mucr, None, Some(cache.ucrBlock.get.ucr))))
        }

        "choice is Shut Mucr" in {
          val expectedAnswer = testRedirection(ShutMUCR, ShutMucrSummaryController.displayPage)
          expectedAnswer mustBe ShutMucrAnswers(Some(ShutMucr(cache.ucrBlock.get.ucr)))
        }
      }
    }
  }

  private def testChoicePageRedirect(result: Future[Result]): Assertion = {
    status(result) mustBe SEE_OTHER
    redirectLocation(result) mustBe Some(ChoiceController.displayChoices.url)
  }

  private def testRedirection(choice: Choice, expectedCall: Call): Answers = {
    givenTheCacheContains(cache)
    val body = JsObject(Map("choice" -> JsString(choice.value)))

    val result = controller.submitChoice(postRequest(body))

    status(result) mustBe SEE_OTHER
    redirectLocation(result).get mustBe expectedCall.url
    theCacheUpserted.answers.get
  }
}
