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

package unit.controllers.consolidations

import controllers.consolidations.{MucrOptionsController, routes}
import forms.Choice.AllowedChoiceValues
import forms.MucrOptions.Create
import forms.{Choice, MucrOptions}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.OptionValues
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.mucr_options

import scala.concurrent.ExecutionContext.global

class MucrOptionsControllerSpec extends ControllerSpec with OptionValues {

  private val mockMucrOptionsPage = mock[mucr_options]

  private val controller = new MucrOptionsController(
    mockAuthAction,
    mockJourneyAction,
    stubMessagesControllerComponents(),
    mockCustomsCacheService,
    mockMucrOptionsPage
  )(global)

  override def beforeEach() {
    super.beforeEach()

    authorizedUser()
    withCaching(Choice.choiceId, Some(Choice(AllowedChoiceValues.AssociateDUCR)))
    withCaching(MucrOptions.formId)
    when(mockMucrOptionsPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override def afterEach(): Unit = {
    reset(mockMucrOptionsPage)

    super.afterEach()
  }

  "Mucr Options Controller" should {

    "return 200 (OK)" when {

      "display page method is invoked" in {

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        verify(mockMucrOptionsPage).apply(any())(any(), any())
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect during saving on first validation" in {

        val incorrectForm = Json.toJson(MucrOptions("8GB12345612345612345", "8GB12345612345612345", ""))

        val result = controller.save()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verify(mockMucrOptionsPage).apply(any())(any(), any())
      }

      "form is incorrect during saving on second validation" in {

        val incorrectForm = Json.toJson(MucrOptions("incorrect", "incorrect", Create))

        val result = controller.save()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verify(mockMucrOptionsPage).apply(any())(any(), any())
      }
    }

    "return 303 (SEE_OTHER)" when {

      "form is correct" in {

        val correctForm = Json.toJson(MucrOptions("8GB12345612345612345", "", Create))

        val result = controller.save()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.AssociateDucrController.displayPage().url
      }
    }
  }
}
