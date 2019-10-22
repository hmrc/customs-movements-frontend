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

package base

import forms.ConsignmentReferences
import models.external.requests.ConsolidationRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers.ACCEPTED
import services.SubmissionService
import testdata.ConsolidationTestData._

import scala.concurrent.Future

trait MockSubmissionService extends MockitoSugar with BeforeAndAfterEach { self: Suite =>

  val mockSubmissionService: SubmissionService = mock[SubmissionService]
  private val ucr = "5GB123456789000-123ABC456DEFIIII"

  def mockSubmission(status: Int = ACCEPTED): OngoingStubbing[Future[(Option[ConsignmentReferences], Int)]] =
    when(mockSubmissionService.submitMovementRequest(any(), any(), any())(any()))
      .thenReturn(Future.successful((Some(ConsignmentReferences("D", ucr)), status)))

  def mockShutMucr(shutMucrRequest: ConsolidationRequest = exampleShutMucrRequest): OngoingStubbing[Future[ConsolidationRequest]] =
    when(mockSubmissionService.submitShutMucrRequest(any(), any())(any()))
      .thenReturn(Future.successful(shutMucrRequest))

  def mockUcrAssociation(consolidationRequest: ConsolidationRequest = exampleAssociateDucrRequest): OngoingStubbing[Future[ConsolidationRequest]] =
    when(mockSubmissionService.submitUcrAssociation(any(), any(), any())(any()))
      .thenReturn(Future.successful(consolidationRequest))

  def mockUcrDisassociation(
    consolidationRequest: ConsolidationRequest = exampleDisassociateDucrRequest
  ): OngoingStubbing[Future[ConsolidationRequest]] =
    when(mockSubmissionService.submitUcrDisassociation(any(), any())(any()))
      .thenReturn(Future.successful(consolidationRequest))

  override protected def afterEach(): Unit = {
    reset(mockSubmissionService)

    super.afterEach()
  }
}
