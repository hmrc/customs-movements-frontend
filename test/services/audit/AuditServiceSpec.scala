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

import base.{BaseSpec, MockCustomsCacheService}
import forms.Choice.Arrival
import forms._
import forms.common.{Date, Time}
import models.requests.{MovementDetailsRequest, MovementRequest, MovementType}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.Json
import services.audit.EventData._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}

import scala.concurrent.{ExecutionContext, Future}

class AuditServiceSpec extends BaseSpec with BeforeAndAfterEach with MockCustomsCacheService {

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

    "get movements data in a Json format" in {

      val expectedResult = Map(
        Location.formId -> Json.toJson(Location("A", "U", "correct", "PL")),
        MovementDetails.formId -> Json.toJson(
          ArrivalDetails(
            dateOfArrival = Date(Some(12), Some(1), Some(2019)),
            timeOfArrival = Time(Some("10"), Some("10"))
          )
        ),
        ArrivalReference.formId -> Json.toJson(ArrivalReference(Some("213"))),
        GoodsDeparted.formId -> Json.toJson(GoodsDeparted("Bricks")),
        ConsignmentReferences.formId -> Json.toJson(ConsignmentReferences("reference", "value")),
        Transport.formId -> Json.toJson(Transport("1", "GB", "SHIP-123"))
      )

      val cacheMap = CacheMap(id = "CacheID", data = expectedResult)
      spyAuditService.getMovementsData(Arrival, cacheMap) mustBe Json.toJson(expectedResult)
    }

    "audit a movement" in {
      val dataToAudit = Map(
        MovementReference.toString -> "",
        EORI.toString -> "eori",
        MessageCode.toString -> "EAL",
        UCR.toString -> "UCR",
        UCRType.toString -> "D",
        SubmissionResult.toString -> "200"
      )
      val data =
        MovementRequest(
          choice = MovementType.Arrival,
          consignmentReference = ConsignmentReferences("UCR", "D"),
          movementDetails = MovementDetailsRequest("dateTime")
        )
      spyAuditService.auditMovements("eori", data, "200", AuditTypes.AuditArrival)
      verify(spyAuditService).audit(AuditTypes.AuditArrival, dataToAudit)
    }
  }
}
