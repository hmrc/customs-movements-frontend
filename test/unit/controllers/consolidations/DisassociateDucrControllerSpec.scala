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
import org.mockito.ArgumentMatchers.{any, eq => meq}
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
    mockJourneyAction,
    mockSubmissionService,
    mockErrorHandler,
    stubMessagesControllerComponents(),
    mockDisassociateDucrPage
  )(global)

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

  private val correctForm = Json.toJson(DisassociateDucr(correctUcr))
  private val incorrectForm = Json.toJson(DisassociateDucr("abc"))

  "Disassociate Ducr Controller on GET" should {

    "return 200 for get request" in {

      val result = controller.displayPage()(getRequest())

      status(result) mustBe OK
    }
  }

  "Disassociate Ducr Controller on POST" when {

    "provided with correct data" should {

      "return SeeOther code" in {

        when(mockSubmissionService.submitDucrDisassociation(any())(any(), any()))
          .thenReturn(Future.successful(ACCEPTED))

        val result = controller.submit()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
      }

      "call SubmissionService" in {

        when(mockSubmissionService.submitDucrDisassociation(any())(any(), any()))
          .thenReturn(Future.successful(ACCEPTED))

        controller.submit()(postRequest(correctForm)).futureValue

        verify(mockSubmissionService).submitDucrDisassociation(meq(DisassociateDucr(correctUcr)))(any(), any())
      }

      "redirect to confirmation page" in {

        when(mockSubmissionService.submitDucrDisassociation(any())(any(), any()))
          .thenReturn(Future.successful(ACCEPTED))

        val result = controller.submit()(postRequest(correctForm))

        redirectLocation(result).value mustBe routes.DisassociateDucrConfirmationController.displayPage().url
      }
    }

    "provided with incorrect data" should {

      "return BadRequest code" in {

        val result = controller.submit()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }

      "not call SubmissionService" in {

        controller.submit()(postRequest(incorrectForm)).futureValue

        verifyZeroInteractions(mockSubmissionService)
      }
    }

    "SubmissionService returns status other than Accepted" should {
      "return InternalServerError code" in {

        when(mockSubmissionService.submitDucrDisassociation(any())(any(), any()))
          .thenReturn(Future.successful(BAD_REQUEST))

        val result = controller.submit()(postRequest(correctForm))

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
