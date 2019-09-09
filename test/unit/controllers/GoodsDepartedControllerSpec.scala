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
import forms.Choice.AllowedChoiceValues
import forms.GoodsDeparted.AllowedPlaces._
import forms.{Choice, GoodsDeparted}
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

  val mockGoodsDepartedPage = mock[goods_departed]

  val controller = new GoodsDepartedController(
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

  def mockArrivalJourney(): Unit = withCaching(Choice.choiceId, Some(Choice(AllowedChoiceValues.Arrival)))

  def mockDepartureJourney(): Unit = withCaching(Choice.choiceId, Some(Choice(AllowedChoiceValues.Departure)))

  def theResponseForm: Form[GoodsDeparted] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[GoodsDeparted]])
    verify(mockGoodsDepartedPage).apply(captor.capture())(any(), any())
    captor.getValue
  }

  "Goods Departed Controller" should {

    "return 200 for get request" when {

      "cache is empty" in {

        mockDepartureJourney()
        withCaching(GoodsDeparted.formId, None)

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        theResponseForm.value mustBe empty
      }

      "cache contains data" in {

        mockDepartureJourney()
        val cachedData = GoodsDeparted(outOfTheUk)
        withCaching(GoodsDeparted.formId, Some(cachedData))

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        theResponseForm.value.value mustBe cachedData
      }
    }

    "return BadRequest" when {

      "user is during arrival journey" in {

        mockArrivalJourney()

        val result = controller.displayPage()(getRequest())

        status(result) mustBe BAD_REQUEST
      }

      "form is incorrect" in {

        mockDepartureJourney()
        withCaching(GoodsDeparted.formId)

        val incorrectForm: JsValue = JsObject(Map("departedPlace" -> JsString("123456")))

        val result = controller.saveGoodsDeparted()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "redirect to date of departure page for correct form" in {

      mockDepartureJourney()
      withCaching(GoodsDeparted.formId)

      val correctForm: JsValue = JsObject(Map("departedPlace" -> JsString(outOfTheUk)))

      val result = controller.saveGoodsDeparted()(postRequest(correctForm))

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe routes.MovementDetailsController.displayPage().url
    }
  }
}
