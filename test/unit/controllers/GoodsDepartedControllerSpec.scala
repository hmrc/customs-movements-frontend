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

import controllers.{routes, GoodsDepartedController}
import forms.GoodsDeparted
import forms.GoodsDeparted.AllowedPlaces._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.goods_departed

import scala.concurrent.ExecutionContext.global

class GoodsDepartedControllerSpec extends ControllerSpec with OptionValues {

  private val mockGoodsDepartedPage = mock[goods_departed]

  private val controller = new GoodsDepartedController(
    mockAuthAction,
    mockJourneyAction,
    mockCustomsCacheService,
    mockErrorHandler,
    stubMessagesControllerComponents(),
    mockGoodsDepartedPage
  )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    setupErrorHandler()
    when(mockGoodsDepartedPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockGoodsDepartedPage)

    super.afterEach()
  }

  private def theResponseForm: Form[GoodsDeparted] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[GoodsDeparted]])
    verify(mockGoodsDepartedPage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  "Goods Departed Controller" should {

    "return 200 (OK)" when {

      "display page method is invoked and cache is empty" in {

        givenAUserOnTheDepartureJourney()
        withCaching(GoodsDeparted.formId, None)

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        theResponseForm.value mustBe empty
      }

      "display page method is invoked and cache contains data" in {

        givenAUserOnTheDepartureJourney()
        val cachedData = GoodsDeparted(outOfTheUk)
        withCaching(GoodsDeparted.formId, Some(cachedData))

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        theResponseForm.value.value mustBe cachedData
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "user is during arrival journey" in {

        givenAUserOnTheArrivalJourney()

        val result = controller.displayPage()(getRequest())

        status(result) mustBe BAD_REQUEST
      }

      "form is incorrect" in {

        givenAUserOnTheDepartureJourney()
        withCaching(GoodsDeparted.formId)

        val incorrectForm: JsValue = JsObject(Map("departedPlace" -> JsString("123456")))

        val result = controller.saveGoodsDeparted()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 303 (SEE_OTHER) and redirect to date of departure page" when {

      "user choose out of UK option" in {

        givenAUserOnTheDepartureJourney()
        withCaching(GoodsDeparted.formId)

        val correctForm: JsValue = JsObject(Map("departedPlace" -> JsString(outOfTheUk)))

        val result = controller.saveGoodsDeparted()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.MovementDetailsController.displayPage().url
      }
    }

    "return 303 (SEE_OTHER) and redirect to summary page" when {

      "user choose back into the UK option" in {

        givenAUserOnTheDepartureJourney()
        withCaching(GoodsDeparted.formId)

        val correctForm: JsValue = JsObject(Map("departedPlace" -> JsString(backIntoTheUk)))

        val result = controller.saveGoodsDeparted()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.SummaryController.displayPage().url
      }
    }
  }
}
