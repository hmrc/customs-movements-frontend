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

import controllers.{routes, TransportController}
import forms.Choice.AllowedChoiceValues
import forms.Transport.ModesOfTransport.Sea
import forms.{Choice, Transport}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.transport

import scala.concurrent.ExecutionContext.global

class TransportControllerSpec extends ControllerSpec with OptionValues {

  private val mockTransportPage = mock[transport]

  private val controller = new TransportController(
    mockAuthAction,
    mockJourneyAction,
    mockCustomsCacheService,
    stubMessagesControllerComponents(),
    mockTransportPage
  )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    withCaching(Choice.choiceId, Some(Choice(AllowedChoiceValues.Arrival)))
    when(mockTransportPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockTransportPage)

    super.afterEach()
  }

  private def theResponseForm: Form[Transport] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[Transport]])
    verify(mockTransportPage).apply(captor.capture(), any())(any(), any())
    captor.getValue
  }

  "Transport Controller" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in {

        withCaching(Transport.formId, None)

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        theResponseForm.value mustBe empty
      }

      "display page method is invoked and cache contains data" in {

        val cachedData = Transport(Sea, "PL", "")
        withCaching(Transport.formId, Some(cachedData))

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        theResponseForm.value.value mustBe cachedData
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect" in {

        withCaching(Transport.formId)

        val incorrectForm: JsValue =
          JsObject(Map("modeOfTransport" -> JsString("transport"), "nationality" -> JsString("Country")))

        val result = controller.saveTransport()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 303 (SEE_OTHER) and redirect to summary page" when {

      "form is correct" in {

        withCaching(Transport.formId)

        val incorrectForm: JsValue = JsObject(
          Map(
            "modeOfTransport" -> JsString(Sea),
            "nationality" -> JsString("PL"),
            "transportId" -> JsString("someReference")
          )
        )

        val result = controller.saveTransport()(postRequest(incorrectForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.SummaryController.displayPage().url
      }
    }
  }
}
