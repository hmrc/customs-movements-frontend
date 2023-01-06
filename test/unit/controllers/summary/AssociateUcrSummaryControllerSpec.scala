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

package controllers.summary

import controllers.ControllerLayerSpec
import controllers.summary.routes.MovementConfirmationController
import forms.UcrType._
import forms._
import models.ReturnToStartException
import models.cache.{AssociateUcrAnswers, JourneyType}
import models.confirmation.FlashKeys
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.SubmissionService
import views.html.summary.{associate_ucr_summary, associate_ucr_summary_no_change}

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class AssociateUcrSummaryControllerSpec extends ControllerLayerSpec with ScalaFutures {

  private val submissionService = mock[SubmissionService]
  private val mockAssociateDucrSummaryPage = mock[associate_ucr_summary]
  private val mockAssociateDucrSummaryNoChangePage = mock[associate_ucr_summary_no_change]

  private def controller(answers: AssociateUcrAnswers, ucrBlockFromIleQuery: Boolean = true) =
    new AssociateUcrSummaryController(
      SuccessfulAuth(),
      ValidJourney(answers, None, ucrBlockFromIleQuery),
      stubMessagesControllerComponents(),
      submissionService,
      mockAssociateDucrSummaryPage,
      mockAssociateDucrSummaryNoChangePage
    )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(mockAssociateDucrSummaryPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockAssociateDucrSummaryNoChangePage.apply(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(submissionService, mockAssociateDucrSummaryPage, mockAssociateDucrSummaryNoChangePage)
    super.afterEach()
  }

  private def theResponseDataOnNonFindConsignmentJourney: (AssociateUcr, String) = {
    val associateDucrCaptor = ArgumentCaptor.forClass(classOf[AssociateUcr])
    val mucrOptionsCaptor = ArgumentCaptor.forClass(classOf[String])
    verify(mockAssociateDucrSummaryPage).apply(associateDucrCaptor.capture(), mucrOptionsCaptor.capture())(any(), any())
    (associateDucrCaptor.getValue, mucrOptionsCaptor.getValue)
  }

  private def theResponseDataOnFindConsignmentJourney: (String, String, UcrType) = {
    val consignmentRefCaptor = ArgumentCaptor.forClass(classOf[String])
    val associateWithCaptor = ArgumentCaptor.forClass(classOf[String])
    val associateKindCaptor = ArgumentCaptor.forClass(classOf[UcrType])
    verify(mockAssociateDucrSummaryNoChangePage).apply(
      consignmentRefCaptor.capture(),
      associateWithCaptor.capture(),
      associateKindCaptor.capture(),
      any()
    )(any(), any())
    (consignmentRefCaptor.getValue, associateWithCaptor.getValue, associateKindCaptor.getValue)
  }

  private val mucrOptions = MucrOptions(MucrOptions.Create, "MUCR")
  private val associateUcr = AssociateUcr(Ducr, "DUCR")

  "AssociateUcrSummaryController.displayPage" should {

    "return 200 (OK)" when {

      "invoked on non-'Find a consignment' journey" in {
        val answer = AssociateUcrAnswers(None, Some(mucrOptions), Some(associateUcr))
        val result = controller(answer, false).displayPage(getRequest())

        status(result) mustBe OK
        verify(mockAssociateDucrSummaryPage).apply(any(), any())(any(), any())

        val (viewUCR, viewOptions) = theResponseDataOnNonFindConsignmentJourney
        viewUCR.ucr mustBe "DUCR"
        viewOptions mustBe "MUCR"
      }

      "on queried ducr" in {
        val result =
          controller(AssociateUcrAnswers(None, Some(MucrOptions(MucrOptions.Create, "MUCR")), Some(AssociateUcr(Ducr, "Queried DUCR"))))
            .displayPage(getRequest())

        status(result) mustBe OK
        verify(mockAssociateDucrSummaryNoChangePage).apply(any(), any(), any(), any())(any(), any())

        val (consignmentRef, associateWith, associateKind) = theResponseDataOnFindConsignmentJourney
        consignmentRef mustBe "Queried DUCR"
        associateWith mustBe "MUCR"
        associateKind mustBe UcrType.Mucr
      }

      "on queried mucr and 'Associate this consignment to another'" in {
        val result = controller(
          AssociateUcrAnswers(
            Some(ManageMucrChoice(ManageMucrChoice.AssociateThisMucr)),
            Some(MucrOptions(MucrOptions.Create, "MUCR")),
            Some(AssociateUcr(Mucr, "Queried MUCR"))
          )
        ).displayPage(getRequest())

        status(result) mustBe OK
        verify(mockAssociateDucrSummaryNoChangePage).apply(any(), any(), any(), any())(any(), any())

        val (consignmentRef, associateWith, associateKind) = theResponseDataOnFindConsignmentJourney
        consignmentRef mustBe "Queried MUCR"
        associateWith mustBe "MUCR"
        associateKind mustBe UcrType.Mucr
      }

      "on queried mucr and 'Associate another consignment to this one'" in {
        val result = controller(
          AssociateUcrAnswers(
            Some(ManageMucrChoice(ManageMucrChoice.AssociateAnotherMucr)),
            Some(MucrOptions(MucrOptions.Create, "Queried MUCR")),
            Some(AssociateUcr(Ducr, "DUCR"))
          )
        ).displayPage(getRequest())

        status(result) mustBe OK
        verify(mockAssociateDucrSummaryNoChangePage).apply(any(), any(), any(), any())(any(), any())

        val (consignmentRef, associateWith, associateKind) = theResponseDataOnFindConsignmentJourney
        consignmentRef mustBe "Queried MUCR"
        associateWith mustBe "DUCR"
        associateKind mustBe UcrType.Ducr
      }
    }

    "throw an ReturnToStartException exception" when {

      "Mucr Options is missing during displaying page" in {
        intercept[RuntimeException] {
          await(controller(AssociateUcrAnswers(mucrOptions = None, associateUcr = Some(associateUcr))).displayPage(getRequest()))
        } mustBe ReturnToStartException
      }

      "Associate Ducr is missing during displaying page" in {
        intercept[RuntimeException] {
          await(controller(AssociateUcrAnswers(mucrOptions = Some(mucrOptions), associateUcr = None)).displayPage(getRequest()))
        } mustBe ReturnToStartException
      }
    }
  }

  "AssociateUcrSummaryController.submit" when {

    "everything works correctly" should {
      val conversationId = "conversationId"

      "call SubmissionService" in {
        when(submissionService.submit(any(), any[AssociateUcrAnswers])(any())).thenReturn(Future.successful(conversationId))
        val cachedAnswers = AssociateUcrAnswers(mucrOptions = Some(mucrOptions), associateUcr = Some(associateUcr))

        controller(cachedAnswers).submit(postRequest()).futureValue

        val expectedEori = SuccessfulAuth().operator.eori
        verify(submissionService).submit(meq(expectedEori), meq(cachedAnswers))(any())
      }

      "return SEE_OTHER (303) that redirects to AssociateUcrConfirmation" in {
        when(submissionService.submit(any(), any[AssociateUcrAnswers])(any())).thenReturn(Future.successful(conversationId))

        val result =
          controller(AssociateUcrAnswers(mucrOptions = Some(mucrOptions), associateUcr = Some(associateUcr))).submit(postRequest(Json.obj()))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(MovementConfirmationController.displayPage.url)
      }

      "return response with Movement Type and Conversation Id in flash" in {
        when(submissionService.submit(any(), any[AssociateUcrAnswers])(any())).thenReturn(Future.successful(conversationId))

        val result =
          controller(AssociateUcrAnswers(mucrOptions = Some(mucrOptions), associateUcr = Some(associateUcr))).submit(postRequest(Json.obj()))

        flash(result).get(FlashKeys.JOURNEY_TYPE) mustBe Some(JourneyType.ASSOCIATE_UCR.toString)
        flash(result).get(FlashKeys.CONVERSATION_ID) mustBe Some(conversationId)
      }
    }
  }
}
