/*
 * Copyright 2021 HM Revenue & Customs
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
import controllers.storage.FlashKeys
import forms.{UcrType, _}
import models.ReturnToStartException
import models.cache.{DisassociateUcrAnswers, JourneyType}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.SubmissionService
import views.html.disassociateucr.disassociate_ucr_summary

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class DisassociateUcrSummaryControllerSpec extends ControllerLayerSpec with ScalaFutures with IntegrationPatience {

  private val submissionService = mock[SubmissionService]
  private val mockDisassociateUcrSummaryPage = mock[disassociate_ucr_summary]

  private def controller(answers: DisassociateUcrAnswers) =
    new DisassociateUcrSummaryController(
      SuccessfulAuth(),
      ValidJourney(answers),
      stubMessagesControllerComponents(),
      submissionService,
      mockDisassociateUcrSummaryPage
    )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(submissionService, mockDisassociateUcrSummaryPage)
    when(mockDisassociateUcrSummaryPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(submissionService, mockDisassociateUcrSummaryPage)

    super.afterEach()
  }

  private def theResponseData: DisassociateUcr = {
    val disassociateUcrCaptor = ArgumentCaptor.forClass(classOf[DisassociateUcr])
    verify(mockDisassociateUcrSummaryPage).apply(disassociateUcrCaptor.capture())(any(), any())
    disassociateUcrCaptor.getValue
  }

  private val ucr = DisassociateUcr(UcrType.Ducr, ducr = Some("DUCR"), mucr = None)

  "Disassociate Ucr Summary Controller on displayPage" should {

    "return 200 (OK)" when {

      "display page is invoked with data in cache" in {
        val result = controller(DisassociateUcrAnswers(Some(ucr))).displayPage()(getRequest())

        status(result) mustBe OK
        verify(mockDisassociateUcrSummaryPage).apply(any())(any(), any())

        theResponseData.ducr.get mustBe "DUCR"
      }
    }

    "throw an ReturnToStartException exception" when {

      "DisassociateUcr is missing during displaying page" in {
        intercept[RuntimeException] {
          await(controller(DisassociateUcrAnswers(None)).displayPage()(getRequest()))
        } mustBe ReturnToStartException
      }
    }
  }

  "Disassociate Ucr Summary Controller on displayPage" when {

    "everything works correctly" should {

      "call SubmissionService" in {
        when(submissionService.submit(any(), any[DisassociateUcrAnswers])(any())).thenReturn(Future.successful((): Unit))
        val cachedAnswers = DisassociateUcrAnswers(Some(ucr))

        controller(cachedAnswers).submit()(postRequest()).futureValue

        val expectedEori = SuccessfulAuth().operator.eori
        verify(submissionService).submit(meq(expectedEori), meq(cachedAnswers))(any())
      }

      "return SEE_OTHER (303) that redirects to DisassociateUcrConfirmation" in {
        when(submissionService.submit(any(), any[DisassociateUcrAnswers])(any())).thenReturn(Future.successful((): Unit))

        val result =
          controller(DisassociateUcrAnswers(Some(ucr))).submit()(postRequest(Json.obj()))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.consolidations.routes.DisassociateUcrConfirmationController.displayPage().url)

      }

      "return response with Movement Type in flash" in {
        when(submissionService.submit(any(), any[DisassociateUcrAnswers])(any())).thenReturn(Future.successful((): Unit))

        val result =
          controller(DisassociateUcrAnswers(Some(ucr))).submit()(postRequest(Json.obj()))

        flash(result).get(FlashKeys.MOVEMENT_TYPE) mustBe Some(JourneyType.DISSOCIATE_UCR.toString)

      }
    }
  }
}
