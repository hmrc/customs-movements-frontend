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
import controllers.consolidations.{routes, DisassociateDucrController}
import forms.DisassociateDucr
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import testdata.CommonTestData.correctUcr
import unit.base.ControllerSpec
import views.html.disassociate_ducr

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class DisassociateDucrControllerSpec
    extends ControllerSpec with MockSubmissionService with ScalaFutures with OptionValues {

  private val mockDisassociateDucrPage = mock[disassociate_ducr]

  private val controller = new DisassociateDucrController(
    mockAuthAction,
    mockSubmissionService,
    mockErrorHandler,
    stubMessagesControllerComponents(),
    mockDisassociateDucrPage
  )(global)
  private val correctForm = Json.toJson(DisassociateDucr(correctUcr))
  private val incorrectForm = Json.toJson(DisassociateDucr("abc"))

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    setupErrorHandler()
    when(mockDisassociateDucrPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockDisassociateDucrPage)

    super.afterEach()
  }

  "Disassociate Ducr Controller" should {

    "return 200 (OK)" when {

      "display page is invoked" in {

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "incorrect form is submitted" in {

        val result = controller.submit()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 500 (BAD_REQUEST)" when {

      "form is correct and submission service return status different than ACCEPTED" in {

        when(mockSubmissionService.submitDucrDisassociation(any())(any(), any(), any()))
          .thenReturn(Future.successful(BAD_REQUEST))

        val result = controller.submit()(postRequest(correctForm))

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "return 303 (SEE_OTHER)" when {

      "form is correct and submission service return ACCEPTED status" in {

        when(mockSubmissionService.submitDucrDisassociation(any())(any(), any(), any()))
          .thenReturn(Future.successful(ACCEPTED))

        val result = controller.submit()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.DisassociateDucrConfirmationController.displayPage().url
      }
    }
  }
}
