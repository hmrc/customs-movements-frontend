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

import connectors.{CustomsDeclareExportsMovementsConnector, NrsConnector}
import models._

import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers.{ACCEPTED, BAD_REQUEST, OK}
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

trait MockConnectors extends MockitoSugar {
  lazy val mockCustomsDeclareExportsMovementsConnector: CustomsDeclareExportsMovementsConnector =
    mock[CustomsDeclareExportsMovementsConnector]

  lazy val mockNrsConnector: NrsConnector = mock[NrsConnector]


  def successfulMovementsResponse(): OngoingStubbing[Future[CustomsDeclareExportsMovementsResponse]] =
    when(mockCustomsDeclareExportsMovementsConnector.saveMovementSubmission(any())(any(), any()))
      .thenReturn(Future.successful(CustomsDeclareExportsMovementsResponse(OK, "")))


  def sendMovementRequest(): OngoingStubbing[Future[HttpResponse]] =
    when(mockCustomsDeclareExportsMovementsConnector
      .submitMovementDeclaration(any[String], any[Option[String]], any[String], any[String])(any(), any()))
      .thenReturn(Future.successful(HttpResponse(ACCEPTED)))

  def sendMovementRequest400Response(): OngoingStubbing[Future[HttpResponse]] =
  when(mockCustomsDeclareExportsMovementsConnector
    .submitMovementDeclaration(any[String], any[Option[String]], any[String], any[String])(any(), any()))
    .thenReturn(Future.successful(HttpResponse(BAD_REQUEST)))

  def submitNrsRequest(): OngoingStubbing[Future[NrsSubmissionResponse]] =
    when(mockNrsConnector.submitNonRepudiation(any())(any(), any()))
      .thenReturn(Future.successful(NrsSubmissionResponse("submissionId1")))

}
