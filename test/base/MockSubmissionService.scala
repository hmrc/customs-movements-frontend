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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers.ACCEPTED
import services.SubmissionService

import scala.concurrent.Future

trait MockSubmissionService extends MockitoSugar with BeforeAndAfterEach { self: Suite =>

  val mockSubmissionService: SubmissionService = mock[SubmissionService]

  def mockSubmission(status: Int = ACCEPTED): OngoingStubbing[Future[Int]] =
    when(mockSubmissionService.submitMovementRequest(any(), any(), any())(any(), any(), any()))
      .thenReturn(Future.successful(status))

  def mockShutMucr(status: Int = ACCEPTED): OngoingStubbing[Future[Int]] =
    when(mockSubmissionService.submitShutMucrRequest(any())(any(), any())).thenReturn(Future.successful(status))

  def mockDucrAssociation(status: Int = ACCEPTED): OngoingStubbing[Future[Int]] =
    when(mockSubmissionService.submitDucrAssociation(any(), any())(any(), any(), any())).thenReturn(Future.successful(status))

  override protected def afterEach(): Unit = {
    reset(mockSubmissionService)

    super.afterEach()
  }
}
