/*
 * Copyright 2020 HM Revenue & Customs
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

import controllers.consolidations.DisassociateUcrSummaryController
import controllers.storage.FlashKeys
import forms.{DisassociateKind, _}
import models.ReturnToStartException
import models.cache.DisassociateUcrAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito._
import org.scalatest.OptionValues
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.SubmissionService
import unit.controllers.ControllerLayerSpec
import unit.repository.MockCache
import views.html.disassociate_ucr_summary

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class DisassociateUcrSummaryControllerSpec extends ControllerLayerSpec with MockCache with OptionValues {

  private val service = mock[SubmissionService]
  private val mockDisassociateUcrSummaryPage = mock[disassociate_ucr_summary]

  private def controller(answers: DisassociateUcrAnswers) =
    new DisassociateUcrSummaryController(
      SuccessfulAuth(),
      ValidJourney(answers),
      stubMessagesControllerComponents(),
      cache,
      service,
      mockDisassociateUcrSummaryPage
    )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(mockDisassociateUcrSummaryPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
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
    val ucr = DisassociateUcr(DisassociateKind.Ducr, ducr = Some("DUCR"), mucr = None)

    "return 200 (OK)" when {

      "display page is invoked with data in cache" in {
        val result = controller(DisassociateUcrAnswers(Some(ucr))).displayPage()(getRequest())

        status(result) mustBe OK
        verify(mockDisassociateUcrSummaryPage).apply(any())(any(), any())

        theResponseData.ducr.value mustBe "DUCR"
      }
    }

    "throw an IncompleteApplication exception" when {

      "DisassociateUcr is missing during displaying page" in {
        intercept[RuntimeException] {
          await(controller(DisassociateUcrAnswers(None)).displayPage()(getRequest()))
        } mustBe ReturnToStartException
      }

      "DisassociateUcr is missing during submitting page" in {
        intercept[RuntimeException] {
          await(controller(DisassociateUcrAnswers(None)).submit()(postRequest(Json.obj())))
        } mustBe ReturnToStartException
      }
    }

    "return 303 (SEE_OTHER)" when {

      "all mandatory data is in cache and submission service returned ACCEPTED" in {
        given(service.submit(anyString(), any[DisassociateUcrAnswers])(any())).willReturn(Future.successful((): Unit))

        val result = controller(DisassociateUcrAnswers(Some(ucr))).submit()(postRequest(Json.obj()))

        status(result) mustBe SEE_OTHER
        flash(result).get(FlashKeys.MUCR) mustBe None
        flash(result).get(FlashKeys.UCR).value mustBe "DUCR"
        flash(result).get(FlashKeys.CONSOLIDATION_KIND).value mustBe "DUCR"
      }
    }
  }
}
