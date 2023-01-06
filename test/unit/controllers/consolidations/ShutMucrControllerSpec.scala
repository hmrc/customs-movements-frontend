/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.consolidations

import controllers.ControllerLayerSpec
import controllers.summary.routes.ShutMucrSummaryController
import forms.ShutMucr
import models.cache.{Cache, ShutMucrAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import repository.MockCache
import testdata.CommonTestData.validMucr
import views.html.consolidations.shut_mucr

import scala.concurrent.ExecutionContext.Implicits.global

class ShutMucrControllerSpec extends ControllerLayerSpec with MockCache with OptionValues {

  private val page = mock[shut_mucr]

  private def controller(answers: ShutMucrAnswers) =
    new ShutMucrController(SuccessfulAuth(), ValidJourney(answers), cacheRepository, stubMessagesControllerComponents(), page)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(page.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  private def theResponseForm: Form[ShutMucr] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[ShutMucr]])
    verify(page).apply(captor.capture())(any(), any())
    captor.getValue()
  }

  override protected def afterEach(): Unit = {
    reset(page)
    super.afterEach()
  }

  "Shut Mucr Controller" should {

    "return 200 (OK)" when {

      "GET displayPage is invoked without data in cache" in {
        givenTheCacheIsEmpty()

        val result = controller(ShutMucrAnswers()).displayPage(getRequest())

        status(result) mustBe OK
        theResponseForm.value mustBe empty
      }

      "GET displayPage is invoked with data in cache" in {
        val cachedForm = Some(ShutMucr("123"))
        givenTheCacheContains(Cache("12345", ShutMucrAnswers(shutMucr = cachedForm)))

        val result = controller(ShutMucrAnswers(shutMucr = cachedForm)).displayPage(getRequest())

        status(result) mustBe OK

        theResponseForm.value mustBe cachedForm
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
        givenTheCacheIsEmpty()

        val correctForm = Json.toJson(ShutMucr(validMucr))

        val result = controller(ShutMucrAnswers()).submitForm()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe ShutMucrSummaryController.displayPage.url
      }
    }
  }
}
