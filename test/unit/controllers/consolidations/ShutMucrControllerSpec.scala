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

import base.MockSubmissionService
import controllers.consolidations.{routes, ShutMucrController}
import forms.ShutMucr
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.OptionValues
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import testdata.ConsolidationTestData.ValidMucr
import unit.base.ControllerSpec
import views.html.shut_mucr

import scala.concurrent.ExecutionContext.global

class ShutMucrControllerSpec extends ControllerSpec with MockSubmissionService with OptionValues {

  private val mockShutMucrPage = mock[shut_mucr]

  private val controller =
    new ShutMucrController(mockAuthAction, mockSubmissionService, stubMessagesControllerComponents(), mockErrorHandler, mockShutMucrPage)(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    setupErrorHandler()
    when(mockShutMucrPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockShutMucrPage)

    super.afterEach()
  }

  "Shut Mucr Controller" should {

    "return 200 (OK)" when {

      "display page method is invoked" in {

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        verify(mockShutMucrPage).apply(any())(any(), any())
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect" in {

        val incorrectForm = Json.toJson(ShutMucr(""))

        val result = controller.submitForm()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 303 (SEE_OTHER)" when {

      "form is correct and submission service returned ACCEPTED" in {

        mockShutMucr()

        val correctForm = Json.toJson(ShutMucr(ValidMucr))

        val result = controller.submitForm()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.ShutMucrConfirmationController.displayPage().url
      }
    }
  }
}
