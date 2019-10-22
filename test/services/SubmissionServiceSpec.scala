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

package services

import base.{MetricsMatchers, MockCustomsCacheService, MockCustomsExportsMovement, MovementsMetricsStub}
import forms.AssociateKind.Ducr
import forms.Choice.{Arrival, Departure}
import forms._
import models.external.requests.ConsolidationRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, verifyZeroInteractions, when}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.test.Helpers._
import services.audit.{AuditService, AuditTypes}
import testdata.ConsolidationTestData._
import testdata.MovementsTestData._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import unit.base.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubmissionServiceSpec
    extends UnitSpec with ScalaFutures with MovementsMetricsStub with MockCustomsExportsMovement with MetricsMatchers with BeforeAndAfterEach
    with MockCustomsCacheService {

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(100, Millis))

  implicit val headerCarrierMock = mock[HeaderCarrier]

  val mockAuditService = mock[AuditService]

  val submissionService =
    new SubmissionService(mockCustomsCacheService, mockCustomsExportsMovementConnector, mockAuditService, movementsMetricsStub)

  override def afterEach(): Unit = {
    reset(mockCustomsCacheService, mockCustomsExportsMovementConnector, mockAuditService)
    super.afterEach()
  }

  private def requestAcceptedTest(block: => Any): Any = {
    when(mockCustomsExportsMovementConnector.sendArrivalDeclaration(any())(any()))
      .thenReturn(Future.successful(HttpResponse(ACCEPTED)))
    when(mockCustomsExportsMovementConnector.sendDepartureDeclaration(any())(any()))
      .thenReturn(Future.successful(HttpResponse(ACCEPTED)))

    when(mockCustomsExportsMovementConnector.sendConsolidationRequest(any())(any()))
      .thenReturn(Future.successful(exampleAssociateDucrRequest))
    block
  }

  "SubmissionService on submitMovementRequest" when {

    "submitting Arrival" should {

      "return response from CustomsDeclareExportsMovementsConnector" in requestAcceptedTest {

        when(mockCustomsCacheService.fetch(any())(any(), any()))
          .thenReturn(Future.successful(Some(CacheMap(Arrival.value, cacheMapData(Arrival)))))

        val CustomHttpResponseCode = 123
        when(mockCustomsExportsMovementConnector.sendArrivalDeclaration(any())(any()))
          .thenReturn(Future.successful(HttpResponse(CustomHttpResponseCode)))

        await(submissionService.submitMovementRequest("arrival-eori1", "eori1", Arrival).map(_._2)) must equal(CustomHttpResponseCode)
        verify(mockAuditService).auditMovements(any(), any(), any(), ArgumentMatchers.eq(AuditTypes.AuditArrival))(any())
        verify(mockAuditService)
          .auditAllPagesUserInput(ArgumentMatchers.eq(Arrival), any())(any())
      }

      "call CustomsDeclareExportsMovementsConnector" in requestAcceptedTest {

        when(mockCustomsCacheService.fetch(any())(any(), any()))
          .thenReturn(Future.successful(Some(CacheMap(Arrival.value, cacheMapData(Arrival)))))

        submissionService.submitMovementRequest("arrival-eori1", "eori1", Arrival).futureValue

        verify(mockCustomsExportsMovementConnector).sendArrivalDeclaration(any())(any())
        verify(mockAuditService).auditMovements(any(), any(), any(), ArgumentMatchers.eq(AuditTypes.AuditArrival))(any())
        verify(mockAuditService)
          .auditAllPagesUserInput(ArgumentMatchers.eq(Arrival), any())(any())
      }

      "return Internal Server Error when no data in cache" in requestAcceptedTest {
        when(mockCustomsCacheService.fetch(any())(any(), any()))
          .thenReturn(Future.successful(None))

        submissionService.submitMovementRequest("arrival-eori1", "eori1", Arrival).map(_._2).futureValue must equal(INTERNAL_SERVER_ERROR)
        verifyZeroInteractions(mockAuditService)
      }
    }

    "submitting Departure" should {

      "return response from CustomsDeclareExportsMovementsConnector" in requestAcceptedTest {

        when(mockCustomsCacheService.fetch(any())(any(), any()))
          .thenReturn(Future.successful(Some(CacheMap(Departure.value, cacheMapData(Departure)))))

        val CustomHttpResponseCode = 123
        when(mockCustomsExportsMovementConnector.sendDepartureDeclaration(any())(any()))
          .thenReturn(Future.successful(HttpResponse(CustomHttpResponseCode)))

        submissionService.submitMovementRequest("departure-eori1", "eori1", Departure).map(_._2).futureValue must equal(CustomHttpResponseCode)
        verify(mockAuditService).auditMovements(any(), any(), any(), ArgumentMatchers.eq(AuditTypes.AuditDeparture))(any())
        verify(mockAuditService)
          .auditAllPagesUserInput(ArgumentMatchers.eq(Departure), any())(any())
      }

      "call CustomsDeclareExportsMovementsConnector" in requestAcceptedTest {

        when(mockCustomsCacheService.fetch(any())(any(), any()))
          .thenReturn(Future.successful(Some(CacheMap(Departure.value, cacheMapData(Departure)))))

        submissionService.submitMovementRequest("departure-eori1", "eori1", Departure).futureValue

        verify(mockCustomsExportsMovementConnector).sendDepartureDeclaration(any())(any())
        verify(mockAuditService).auditMovements(any(), any(), any(), ArgumentMatchers.eq(AuditTypes.AuditDeparture))(any())
        verify(mockAuditService)
          .auditAllPagesUserInput(ArgumentMatchers.eq(Departure), any())(any())
      }

      "return Internal Server Error when no data in cache" in requestAcceptedTest {
        when(mockCustomsCacheService.fetch(any())(any(), any()))
          .thenReturn(Future.successful(None))

        submissionService.submitMovementRequest("departure-eori1", "eori1", Departure).map(_._2).futureValue must equal(INTERNAL_SERVER_ERROR)
        verifyZeroInteractions(mockAuditService)
      }
    }
  }

  "SubmissionService on submitDucrAssociation" should {

    val validDucrAssociation = AssociateUcr(Ducr, ducr = Some(ValidDucr), mucr = None)

    "return response from CustomsDeclareExportsMovementsConnector" in {

      when(mockCustomsExportsMovementConnector.sendConsolidationRequest(any())(any()))
        .thenReturn(Future.successful(exampleAssociateDucrRequest))

      submissionService
        .submitUcrAssociation(MucrOptions(ValidMucr), validDucrAssociation, "eori")
        .futureValue must equal(exampleAssociateDucrRequest)
      verify(mockAuditService)
        .auditAssociate(ArgumentMatchers.eq("eori"), any(), any(), any())(any())
    }

    "call CustomsDeclareExportsMovementsConnector, passing correctly built request" in requestAcceptedTest {

      submissionService.submitUcrAssociation(MucrOptions(ValidMucr), validDucrAssociation, "eori").futureValue

      val requestCaptor: ArgumentCaptor[ConsolidationRequest] = ArgumentCaptor.forClass(classOf[ConsolidationRequest])
      verify(mockCustomsExportsMovementConnector).sendConsolidationRequest(requestCaptor.capture())(any())
      verify(mockAuditService)
        .auditAssociate(ArgumentMatchers.eq("eori"), any(), any(), any())(any())

      requestCaptor.getValue mustBe exampleAssociateDucrRequest
    }
  }

  "SubmissionService on submitUcrDisassociation" should {

    "return response from CustomsDeclareExportsMovementsConnector" in {

      when(mockCustomsExportsMovementConnector.sendConsolidationRequest(any())(any()))
        .thenReturn(Future.successful(exampleDisassociateDucrRequest))

      submissionService.submitUcrDisassociation(DisassociateUcr(DisassociateKind.Ducr, Some(ValidDucr), None), "eori").futureValue must equal(
        exampleDisassociateDucrRequest
      )
      verify(mockAuditService).auditDisassociate(ArgumentMatchers.eq("eori"), any(), any())(any())

    }

    "call CustomsDeclareExportsMovementsConnector, passing correctly built request" in requestAcceptedTest {

      submissionService.submitUcrDisassociation(DisassociateUcr(DisassociateKind.Ducr, Some(ValidDucr), None), "eori").futureValue

      val requestCaptor: ArgumentCaptor[ConsolidationRequest] = ArgumentCaptor.forClass(classOf[ConsolidationRequest])
      verify(mockCustomsExportsMovementConnector).sendConsolidationRequest(requestCaptor.capture())(any())
      verify(mockAuditService).auditDisassociate(ArgumentMatchers.eq("eori"), any(), any())(any())

      requestCaptor.getValue mustBe exampleDisassociateDucrRequest
    }

    "increase counter for successful submissions" in requestAcceptedTest {
      counter("disassociation.counter") must changeOn {
        submissionService.submitUcrDisassociation(DisassociateUcr(DisassociateKind.Ducr, Some(ValidDucr), None), "eori").futureValue
      }
    }

    "use timer to measure execution of successful disassociate request" in requestAcceptedTest {
      timer("disassociation.timer") must changeOn {
        submissionService.submitUcrDisassociation(DisassociateUcr(DisassociateKind.Ducr, Some(ValidDucr), None), "eori").futureValue
      }
    }
  }

  "SubmissionService on submitShutMucrRequest" should {

    "return response from CustomsDeclareExportsMovementsConnector" in {

      when(mockCustomsExportsMovementConnector.sendConsolidationRequest(any())(any()))
        .thenReturn(Future.successful(exampleShutMucrRequest))

      submissionService.submitShutMucrRequest(ShutMucr(ValidMucr), "eori").futureValue must equal(exampleShutMucrRequest)
      verify(mockAuditService).auditShutMucr(ArgumentMatchers.eq("eori"), any(), any())(any())
    }

    "call CustomsDeclareExportsMovementsConnector, passing correctly built request" in requestAcceptedTest {

      submissionService.submitShutMucrRequest(ShutMucr(ValidMucr), "eori").futureValue

      val requestCaptor: ArgumentCaptor[ConsolidationRequest] = ArgumentCaptor.forClass(classOf[ConsolidationRequest])
      verify(mockCustomsExportsMovementConnector).sendConsolidationRequest(requestCaptor.capture())(any())
      verify(mockAuditService).auditShutMucr(ArgumentMatchers.eq("eori"), any(), any())(any())

      requestCaptor.getValue mustBe exampleShutMucrRequest
    }

    "increase counter of successful shut request" in requestAcceptedTest {
      counter("shut.counter") must changeOn {
        submissionService.submitShutMucrRequest(ShutMucr(ValidMucr), "eori").futureValue
      }
    }

    "use timer to measure execution of successful shut request" in requestAcceptedTest {
      timer("shut.timer") must changeOn {
        submissionService.submitShutMucrRequest(ShutMucr(ValidMucr), "eori").futureValue
      }
    }
  }
}
