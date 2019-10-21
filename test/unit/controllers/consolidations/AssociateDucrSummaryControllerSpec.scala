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
import controllers.consolidations.AssociateDucrSummaryController
import controllers.exception.IncompleteApplication
import controllers.storage.FlashKeys
import forms.Choice.AssociateDUCR
import forms.{AssociateUcr, Choice, MucrOptions}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.OptionValues
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.associate_ducr_summary
import forms.AssociateKind._

import scala.concurrent.ExecutionContext.global

class AssociateDucrSummaryControllerSpec extends ControllerSpec with MockSubmissionService with OptionValues {

  private val mockAssociateDucrSummaryPage = mock[associate_ducr_summary]

  private val controller = new AssociateDucrSummaryController(
    mockAuthAction,
    mockJourneyAction,
    mockErrorHandler,
    stubMessagesControllerComponents(),
    mockCustomsCacheService,
    mockSubmissionService,
    mockAssociateDucrSummaryPage
  )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    setupErrorHandler()
    when(mockAssociateDucrSummaryPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    withCaching(Choice.choiceId, Some(AssociateDUCR))
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

    "return 200 (OK)" when {

      "display page is invoked with data in cache" in {
        withCaching(MucrOptions.formId, Some(MucrOptions("MUCR")))
        withCaching(AssociateUcr.formId, Some(AssociateUcr(Ducr, ducr = Some("DUCR"), mucr = None)))

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        verify(mockAssociateDucrSummaryPage).apply(any(), any())(any(), any())

        val (associateDucr, mucrOptions) = theResponseData
        associateDucr.ducr.value mustBe "DUCR"
        mucrOptions mustBe "MUCR"
      }
    }

    "throw an IncompleteApplication exception" when {

      "Mucr Options is missing during displaying page" in {

        withCaching(MucrOptions.formId, None)

        assertThrows[IncompleteApplication] {
          await(controller.displayPage()(getRequest()))
        }
      }

      "Associate Ducr is missing during displaying page" in {

        withCaching(MucrOptions.formId, Some(MucrOptions("MUCR")))
        withCaching(AssociateUcr.formId, None)

        assertThrows[IncompleteApplication] {
          await(controller.displayPage()(getRequest()))
        }
      }

      "Mucr Options is missing during submitting page" in {

        withCaching(MucrOptions.formId, None)

        assertThrows[IncompleteApplication] {
          await(controller.submit()(postRequest(Json.obj())))
        }
      }

      "Associate Ducr is missing during submitting page" in {

        withCaching(MucrOptions.formId, Some(MucrOptions("MUCR")))
        withCaching(AssociateUcr.formId, None)

        assertThrows[IncompleteApplication] {
          await(controller.submit()(postRequest(Json.obj())))
        }
      }
    }

    "return 303 (SEE_OTHER)" when {

      "all mandatory data is in cache and submission service returned ACCEPTED" in {

        withCaching(MucrOptions.formId, Some(MucrOptions("MUCR")))
        withCaching(AssociateUcr.formId, Some(AssociateUcr(Ducr, ducr = Some("DUCR"), mucr = None)))
        mockCustomsCacheServiceClearedSuccessfully()
        mockDucrAssociation()

        val result = controller.submit()(postRequest(Json.obj()))

        status(result) mustBe SEE_OTHER
        flash(result).get(FlashKeys.MUCR) mustBe None
        flash(result).get(FlashKeys.UCR).value mustBe "DUCR"
        flash(result).get(FlashKeys.CONSOLIDATION_KIND).value mustBe Ducr.formValue
      }
    }
  }
}
