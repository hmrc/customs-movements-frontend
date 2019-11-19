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

import controllers.consolidations.AssociateUcrSummaryController
import controllers.storage.FlashKeys
import forms.AssociateKind._
import forms.{AssociateUcr, MucrOptions}
import models.ReturnToStartException
import models.cache.AssociateUcrAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.OptionValues
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.SubmissionService
import unit.controllers.ControllerLayerSpec
import unit.repository.MockCache
import views.html.associate_ucr_summary

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class AssociateUcrSummaryControllerSpec extends ControllerLayerSpec with MockCache with OptionValues {

  private val service = mock[SubmissionService]
  private val mockAssociateDucrSummaryPage = mock[associate_ucr_summary]

  private def controller(answers: AssociateUcrAnswers) = new AssociateUcrSummaryController(
    SuccessfulAuth(),
    ValidJourney(answers),
    stubMessagesControllerComponents(),
    cache,
    service,
    mockAssociateDucrSummaryPage
  )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(mockAssociateDucrSummaryPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockAssociateDucrSummaryPage)

    super.afterEach()
  }

  private def theResponseData: (AssociateUcr, String) = {
    val associateDucrCaptor = ArgumentCaptor.forClass(classOf[AssociateUcr])
    val mucrOptionsCaptor = ArgumentCaptor.forClass(classOf[String])
    verify(mockAssociateDucrSummaryPage).apply(associateDucrCaptor.capture(), mucrOptionsCaptor.capture())(any(), any())
    (associateDucrCaptor.getValue, mucrOptionsCaptor.getValue)
  }

  "Associate Ducr Summary Controller" should {
    val mucrOptions = MucrOptions("MUCR")
    val associateUcr = AssociateUcr(Ducr, "DUCR")

    "return 200 (OK)" when {

      "display page is invoked with data in cache" in {
        val result = controller(AssociateUcrAnswers(Some(mucrOptions), Some(associateUcr))).displayPage()(getRequest())

        status(result) mustBe OK
        verify(mockAssociateDucrSummaryPage).apply(any(), any())(any(), any())

        val (viewUCR, viewOptions) = theResponseData
        viewUCR.ucr mustBe "DUCR"
        viewOptions mustBe "MUCR"
      }
    }

    "throw an IncompleteApplication exception" when {

      "Mucr Options is missing during displaying page" in {
        intercept[RuntimeException] {
          await(controller(AssociateUcrAnswers(mucrOptions = None, associateUcr = Some(associateUcr))).displayPage()(getRequest()))
        } mustBe ReturnToStartException
      }

      "Associate Ducr is missing during displaying page" in {
        intercept[RuntimeException] {
          await(controller(AssociateUcrAnswers(mucrOptions = Some(mucrOptions), associateUcr = None)).displayPage()(getRequest()))
        } mustBe ReturnToStartException
      }

      "Associate Ducr is missing during submitting page" in {
        intercept[RuntimeException] {
          await(controller(AssociateUcrAnswers(mucrOptions = Some(mucrOptions), associateUcr = None)).submit()(postRequest(Json.obj())))
        } mustBe ReturnToStartException
      }
    }

    "return 303 (SEE_OTHER)" when {

      "all mandatory data is in cache and submission service returned ACCEPTED" in {
        when(service.submit(any(), any[AssociateUcrAnswers])(any())).thenReturn(Future.successful((): Unit))

        val result = controller(AssociateUcrAnswers(mucrOptions = Some(mucrOptions), associateUcr = Some(associateUcr))).submit()(postRequest(Json.obj()))

        status(result) mustBe SEE_OTHER
        flash(result).get(FlashKeys.MUCR) mustBe None
        flash(result).get(FlashKeys.UCR).value mustBe "DUCR"
        flash(result).get(FlashKeys.CONSOLIDATION_KIND).value mustBe Ducr.formValue
      }
    }
  }
}
