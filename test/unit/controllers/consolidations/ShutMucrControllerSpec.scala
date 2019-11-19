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

import controllers.consolidations.{routes, ShutMucrController}
import forms.ShutMucr
import models.cache.ShutMucrAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.OptionValues
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import testdata.ConsolidationTestData.validMucr
import unit.controllers.ControllerLayerSpec
import unit.repository.MockCache
import views.html.shut_mucr

import scala.concurrent.ExecutionContext.global

class ShutMucrControllerSpec extends ControllerLayerSpec with MockCache with OptionValues {

  private val page = mock[shut_mucr]

  private def controller(answers: ShutMucrAnswers) =
    new ShutMucrController(SuccessfulAuth(), ValidJourney(answers), cache, stubMessagesControllerComponents(), page)(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(page.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(page)
    super.afterEach()
  }

  "Shut Mucr Controller" should {

    "return 200 (OK) on displayPage method" when {

      "cache contains shut mucr data" in {
        val mucr = ShutMucr("Mucr")

        val result = controller(ShutMucrAnswers(Some(mucr))).displayPage()(getRequest())

        status(result) mustBe OK
        verify(page).apply(any())(any(), any())
      }

      "cache is empty" in {
        val result = controller(ShutMucrAnswers()).displayPage()(getRequest())

        status(result) mustBe OK
        verify(page).apply(any())(any(), any())
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect" in {
        val incorrectForm = Json.toJson(ShutMucr(""))

        val result = controller(ShutMucrAnswers()).submitForm()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 303 (SEE_OTHER)" when {

      "form is correct and submission service returned ACCEPTED" in {
        val correctForm = Json.toJson(ShutMucr(validMucr))

        val result = controller(ShutMucrAnswers()).submitForm()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.ShutMucrSummaryController.displayPage().url
      }
    }
  }
}
