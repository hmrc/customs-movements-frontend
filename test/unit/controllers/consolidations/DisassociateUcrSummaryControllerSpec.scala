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
import controllers.consolidations.DisassociateUcrSummaryController
import controllers.exception.IncompleteApplication
import controllers.storage.FlashKeys
import forms.AssociateKind._
import forms.Choice.DisassociateUCR
import forms.{DisassociateKind, _}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.OptionValues
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.disassociate_ucr_summary

import scala.concurrent.ExecutionContext.global

class DisassociateUcrSummaryControllerSpec extends ControllerSpec with MockSubmissionService with OptionValues {

  private val mockDisassociateUcrSummaryPage = mock[disassociate_ucr_summary]

  private val controller = new DisassociateUcrSummaryController(
    mockAuthAction,
    mockJourneyAction,
    mockErrorHandler,
    stubMessagesControllerComponents(),
    mockCustomsCacheService,
    mockSubmissionService,
    mockDisassociateUcrSummaryPage
  )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    setupErrorHandler()
    when(mockDisassociateUcrSummaryPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
    withCaching(Choice.choiceId, Some(DisassociateUCR))
  }

  override protected def afterEach(): Unit = {
    reset(mockDisassociateUcrSummaryPage)

    super.afterEach()
  }

  private def theResponseData: DisassociateUcr = {
    val disassociateUcrCaptor = ArgumentCaptor.forClass(classOf[DisassociateUcr])
    verify(mockDisassociateUcrSummaryPage).apply(disassociateUcrCaptor.capture())(any(), any())
    disassociateUcrCaptor.getValue
  }

  "Disassociate Ucr Summary Controller" should {

    "return 200 (OK)" when {

      "display page is invoked with data in cache" in {
        withCaching(DisassociateUcr.formId, Some(DisassociateUcr(DisassociateKind.Ducr, ducr = Some("DUCR"), mucr = None)))

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        verify(mockDisassociateUcrSummaryPage).apply(any())(any(), any())

        val disassocuiateUcr = theResponseData
        disassocuiateUcr.ducr.value mustBe "DUCR"
      }
    }

    "throw an IncompleteApplication exception" when {

      "DisassociateUcr is missing during displaying page" in {

        withCaching(DisassociateUcr.formId, None)

        assertThrows[IncompleteApplication] {
          await(controller.displayPage()(getRequest()))
        }
      }

      "DisassociateUcr is missing during submitting page" in {

        withCaching(DisassociateUcr.formId, None)

        assertThrows[IncompleteApplication] {
          await(controller.submit()(postRequest(Json.obj())))
        }
      }
    }

    "return 303 (SEE_OTHER)" when {

      "all mandatory data is in cache and submission service returned ACCEPTED" in {

        withCaching(DisassociateUcr.formId, Some(DisassociateUcr(DisassociateKind.Ducr, ducr = Some("DUCR"), mucr = None)))
        mockCustomsCacheServiceClearedSuccessfully()
        mockUcrDisassociation()

        val result = controller.submit()(postRequest(Json.obj()))

        status(result) mustBe SEE_OTHER
        flash(result).get(FlashKeys.MUCR) mustBe None
        flash(result).get(FlashKeys.UCR).value mustBe "DUCR"
        flash(result).get(FlashKeys.CONSOLIDATION_KIND).value mustBe "DUCR"
      }
    }
  }
}
