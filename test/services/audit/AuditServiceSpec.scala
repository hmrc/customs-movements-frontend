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

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.{JsValue, Json}
import services.audit.AuditService.EventData
import testdata.{CommonTestData, MovementsTestData}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.{DataEvent, ExtendedDataEvent}
import unit.base.UnitSpec

import scala.concurrent.{ExecutionContext, Future}

class AuditServiceSpec extends UnitSpec with BeforeAndAfterEach {

  private implicit val ec: ExecutionContext = ExecutionContext.global
  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  private val auditConnector = mock[AuditConnector]
  private val service = new AuditService(auditConnector, "appName")

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(auditConnector.sendEvent(any())(any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.successful(AuditResult.Success))
    when(auditConnector.sendExtendedEvent(any())(any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.successful(AuditResult.Success))
  }

  override def afterEach(): Unit = {
    reset(auditConnector)

    super.afterEach()
  }

  private def auditTagsPayload(auditType: String): Map[String, String] = Map(
    "clientIP" -> "-",
    "path" -> s"customs-declare-exports/$auditType/full-payload",
    "X-Session-ID" -> "-",
    "Akamai-Reputation" -> "-",
    "X-Request-ID" -> "-",
    "deviceID" -> "-",
    "clientPort" -> "-",
    "transactionName" -> s"Export-Declaration-${auditType}-payload-request"
  )

  private def auditTagsResult(auditType: String): Map[String, String] = Map(
    "clientIP" -> "-",
    "path" -> s"customs-declare-exports/$auditType",
    "X-Session-ID" -> "-",
    "Akamai-Reputation" -> "-",
    "X-Request-ID" -> "-",
    "deviceID" -> "-",
    "clientPort" -> "-",
    "transactionName" -> s"Export-Declaration-${auditType}-request"
  )

  private def sentExtendedDataEvent: ExtendedDataEvent = {
    val captor: ArgumentCaptor[ExtendedDataEvent] = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])
    verify(auditConnector).sendExtendedEvent(captor.capture())(any[HeaderCarrier], any[ExecutionContext])
    captor.getValue
  }

  private def sentDataEvent: DataEvent = {
    val captor: ArgumentCaptor[DataEvent] = ArgumentCaptor.forClass(classOf[DataEvent])
    verify(auditConnector).sendEvent(captor.capture())(any[HeaderCarrier], any[ExecutionContext])
    captor.getValue
  }

  "AuditService" should {

    "call AuditConnector sendExtendedEvent method with correct data when auditing submission payload" when {

      "used for Arrival" in {

        val answers = MovementsTestData.validArrivalAnswers

        val auditTags = auditTagsPayload("Arrival")
        val auditDetail: JsValue = Json.obj(
          "ConsignmentReferences" -> answers.consignmentReferences,
          "Location" -> answers.location,
          "MovementDetails" -> answers.arrivalDetails,
          "ArrivalReference" -> answers.arrivalReference
        )
        val expectedExtendedDataEvent =
          ExtendedDataEvent(auditSource = "appName", auditType = AuditType.AuditArrival.toString, tags = auditTags, detail = auditDetail)

        service.auditAllPagesUserInput(answers)

        val actualExtendedDataEvent = sentExtendedDataEvent
        actualExtendedDataEvent.auditSource mustBe expectedExtendedDataEvent.auditSource
        actualExtendedDataEvent.auditType mustBe expectedExtendedDataEvent.auditType
        actualExtendedDataEvent.tags mustBe expectedExtendedDataEvent.tags
        actualExtendedDataEvent.detail mustBe expectedExtendedDataEvent.detail
      }

      "used for Departure" in {

        val answers = MovementsTestData.validDepartureAnswers

        val auditTags = auditTagsPayload("Departure")
        val auditDetail: JsValue = Json.obj(
          "ConsignmentReferences" -> answers.consignmentReferences,
          "Location" -> answers.location,
          "MovementDetails" -> answers.departureDetails,
          "Transport" -> answers.transport
        )
        val expectedExtendedDataEvent =
          ExtendedDataEvent(auditSource = "appName", auditType = AuditType.AuditDeparture.toString, tags = auditTags, detail = auditDetail)

        service.auditAllPagesUserInput(answers)

        val actualExtendedDataEvent = sentExtendedDataEvent
        actualExtendedDataEvent.auditSource mustBe expectedExtendedDataEvent.auditSource
        actualExtendedDataEvent.auditType mustBe expectedExtendedDataEvent.auditType
        actualExtendedDataEvent.tags mustBe expectedExtendedDataEvent.tags
        actualExtendedDataEvent.detail mustBe expectedExtendedDataEvent.detail
      }
    }

    "call AuditConnector sendEvent method with correct data when auditing submission result" when {

      "used for Shut a Mucr" in {

        val auditTags = auditTagsResult("ShutMucr")
        val auditDetail =
          Map(EventData.eori.toString -> CommonTestData.validEori, EventData.mucr.toString -> "mucr", EventData.submissionResult.toString -> "200")
        val expectedDataEvent =
          DataEvent(auditSource = "appName", auditType = AuditType.AuditShutMucr.toString, tags = auditTags, detail = auditDetail)

        service.auditShutMucr(CommonTestData.validEori, "mucr", "200")

        val actualDataEvent = sentDataEvent
        actualDataEvent.auditSource mustBe expectedDataEvent.auditSource
        actualDataEvent.auditType mustBe expectedDataEvent.auditType
        actualDataEvent.tags mustBe expectedDataEvent.tags
        actualDataEvent.detail mustBe expectedDataEvent.detail
      }

      "used for Association" in {

        val auditTags = auditTagsResult("Associate")
        val auditDetail =
          Map(
            EventData.eori.toString -> CommonTestData.validEori,
            EventData.mucr.toString -> "mucr",
            EventData.ducr.toString -> "ducr",
            EventData.submissionResult.toString -> "200"
          )
        val expectedDataEvent =
          DataEvent(auditSource = "appName", auditType = AuditType.AuditAssociate.toString, tags = auditTags, detail = auditDetail)

        service.auditAssociate(CommonTestData.validEori, "mucr", "ducr", "200")

        val actualDataEvent = sentDataEvent
        actualDataEvent.auditSource mustBe expectedDataEvent.auditSource
        actualDataEvent.auditType mustBe expectedDataEvent.auditType
        actualDataEvent.tags mustBe expectedDataEvent.tags
        actualDataEvent.detail mustBe expectedDataEvent.detail
      }

      "used for Disassociation" in {

        val auditTags = auditTagsResult("Disassociate")
        val auditDetail =
          Map(EventData.eori.toString -> CommonTestData.validEori, EventData.ucr.toString -> "ucr", EventData.submissionResult.toString -> "200")
        val expectedDataEvent =
          DataEvent(auditSource = "appName", auditType = AuditType.AuditDisassociate.toString, tags = auditTags, detail = auditDetail)

        service.auditDisassociate(CommonTestData.validEori, "ucr", "200")

        val actualDataEvent = sentDataEvent
        actualDataEvent.auditSource mustBe expectedDataEvent.auditSource
        actualDataEvent.auditType mustBe expectedDataEvent.auditType
        actualDataEvent.tags mustBe expectedDataEvent.tags
        actualDataEvent.detail mustBe expectedDataEvent.detail
      }

      "used for Arrival" in {

        val auditTags = auditTagsResult("Arrival")
        val auditDetail = Map(
          EventData.eori.toString -> CommonTestData.validEori,
          EventData.messageCode.toString -> "EAL",
          EventData.ucr.toString -> CommonTestData.correctUcr,
          EventData.ucrType.toString -> "D",
          EventData.submissionResult.toString -> "200",
          EventData.movementReference.toString -> "arrivalReference"
        )
        val expectedDataEvent =
          DataEvent(auditSource = "appName", auditType = AuditType.AuditArrival.toString, tags = auditTags, detail = auditDetail)

        val data = MovementsTestData.validArrivalMovementRequest

        service.auditMovements(data, "200", AuditType.AuditArrival)

        val actualDataEvent = sentDataEvent
        actualDataEvent.auditSource mustBe expectedDataEvent.auditSource
        actualDataEvent.auditType mustBe expectedDataEvent.auditType
        actualDataEvent.tags mustBe expectedDataEvent.tags
        actualDataEvent.detail mustBe expectedDataEvent.detail
      }

      "used for Departure" in {

        val auditTags = auditTagsResult("Departure")
        val auditDetail = Map(
          EventData.eori.toString -> CommonTestData.validEori,
          EventData.messageCode.toString -> "EDL",
          EventData.ucr.toString -> CommonTestData.correctUcr,
          EventData.ucrType.toString -> "D",
          EventData.submissionResult.toString -> "200",
          EventData.movementReference.toString -> ""
        )
        val expectedDataEvent =
          DataEvent(auditSource = "appName", auditType = AuditType.AuditDeparture.toString, tags = auditTags, detail = auditDetail)

        val data = MovementsTestData.validDepartureMovementRequest

        service.auditMovements(data, "200", AuditType.AuditDeparture)

        val actualDataEvent = sentDataEvent
        actualDataEvent.auditSource mustBe expectedDataEvent.auditSource
        actualDataEvent.auditType mustBe expectedDataEvent.auditType
        actualDataEvent.tags mustBe expectedDataEvent.tags
        actualDataEvent.detail mustBe expectedDataEvent.detail
      }
    }
  }

}
