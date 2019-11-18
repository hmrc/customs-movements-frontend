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

import controllers.ArrivalReferenceController
import forms.ArrivalReference
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.LegacyControllerSpec
import views.html.arrival_reference

import scala.concurrent.ExecutionContext.global

class ArrivalReferenceControllerSpec extends LegacyControllerSpec with OptionValues with ScalaFutures {

  private val mockArrivalReferencePage = mock[arrival_reference]

  private val controller = new ArrivalReferenceController(
    mockAuthAction,
    mockJourneyAction,
    mockCustomsCacheService,
    mockErrorHandler,
    stubMessagesControllerComponents(),
    mockArrivalReferencePage
  )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    setupErrorHandler()
    when(mockArrivalReferencePage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockArrivalReferencePage)

    super.afterEach()
  }

  private def theResponseForm: Form[ArrivalReference] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[ArrivalReference]])
    verify(mockArrivalReferencePage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  "Arrival Reference Controller" should {

    "return 200 (OK)" when {

      "display page is invoked during arrival journey with empty cache" in {

        givenAUserOnTheArrivalJourney()
        withCaching(ArrivalReference.formId, None)

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        theResponseForm.value mustBe empty
      }

      "display page is invoked during arrival journey with data in cache" in {

        givenAUserOnTheArrivalJourney()
        val reference = "123456"
        withCaching(ArrivalReference.formId, Some(ArrivalReference(Some(reference))))

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        theResponseForm.value.value.reference.value mustBe reference
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "display page is invoked during departure journey" in {

        givenAUserOnTheDepartureJourney()

        val result = controller.displayPage()(getRequest())

        status(result) mustBe BAD_REQUEST
      }

      "form contains errors during submission" in {

        givenAUserOnTheArrivalJourney()
        withCaching(ArrivalReference.formId, None)

        val incorrectForm = Json.toJson(ArrivalReference(Some("!@#$%")))

        val result = controller.submit()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 303 (SEE_OTHER) and redirect to Movement Details page" when {

      "form is correct" in {

        givenAUserOnTheArrivalJourney()
        withCaching(ArrivalReference.formId)
        withCaching(ArrivalReference.formId, None)

        val correctForm = Json.toJson(ArrivalReference(Some("123456")))

        val result = controller.submit()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
      }
    }
  }
}
