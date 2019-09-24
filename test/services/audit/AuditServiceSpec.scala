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

package services.audit
import base.BaseSpec
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import services.audit.EventData._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.wco.dec.inventorylinking.common.UcrBlock
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest

import scala.concurrent.{ExecutionContext, Future}

class AuditServiceSpec extends BaseSpec with BeforeAndAfterEach {

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val headerCarrier = HeaderCarrier()

  val mockAuditConnector = mock[AuditConnector]
  val spyAuditService = Mockito.spy(new AuditService(mockAuditConnector, "appName"))

  override def beforeEach(): Unit =
    when(mockAuditConnector.sendEvent(any())(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(AuditResult.Success))

  override def afterEach(): Unit = reset(mockAuditConnector)

  "AuditService" should {
    "audit Shut a Mucr data" in {
      val dataToAudit = Map(EORI.toString -> "eori", MUCR.toString -> "mucr", SubmissionResult.toString -> "200")
      spyAuditService.auditShutMucr("eori", "mucr", "200")
      verify(spyAuditService).audit(AuditTypes.AuditShutMucr, dataToAudit)
    }

    "audit an association" in {
      val dataToAudit = Map(
        EORI.toString -> "eori",
        MUCR.toString -> "mucr",
        DUCR.toString -> "ducr",
        SubmissionResult.toString -> "200"
      )
      spyAuditService.auditAssociate("eori", "mucr", "ducr", "200")
      verify(spyAuditService).audit(AuditTypes.AuditAssociate, dataToAudit)
    }

    "audit a disassociation" in {
      val dataToAudit = Map(EORI.toString -> "eori", DUCR.toString -> "ducr", SubmissionResult.toString -> "200")
      spyAuditService.auditDisassociate("eori", "ducr", "200")
      verify(spyAuditService).audit(AuditTypes.AuditDisassociate, dataToAudit)
    }

    "audit a movement" in {
      val dataToAudit = Map(
        MovementReference.toString -> "",
        EORI.toString -> "eori",
        MessageCode.toString -> "EAD",
        UCR.toString -> "UCR",
        UCRType.toString -> "D",
        SubmissionResult.toString -> "200"
      )
      val data =
        InventoryLinkingMovementRequest(messageCode = "EAD", ucrBlock = UcrBlock("UCR", "D"), goodsLocation = "")
      spyAuditService.auditMovements("eori", data, "200", AuditTypes.AuditArrival)
      verify(spyAuditService).audit(AuditTypes.AuditArrival, dataToAudit)
    }
  }
}
