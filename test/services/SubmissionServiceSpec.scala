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

import base.{MetricsMatchers, MovementsMetricsStub}
import connectors.CustomsDeclareExportsMovementsConnector
import forms.Choice.AllowedChoiceValues.{Arrival, Departure}
import forms._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, verifyZeroInteractions, when}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.test.Helpers.ACCEPTED
import services.audit.AuditService
import testdata.ConsolidationTestData._
import testdata.MovementsTestData._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import unit.base.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.{Node, Utility, XML}

class SubmissionServiceSpec
    extends WordSpec with MustMatchers with MockitoSugar with ScalaFutures with MovementsMetricsStub
    with MetricsMatchers with BeforeAndAfterEach with UnitSpec {

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(100, Millis))

  implicit val headerCarrierMock = mock[HeaderCarrier]

  val mockAuditService = mock[AuditService]
  val customsCacheServiceMock = mock[CustomsCacheService]
  val customsExportsMovementConnectorMock = mock[CustomsDeclareExportsMovementsConnector]

  val submissionService =
    new SubmissionService(
      customsCacheServiceMock,
      customsExportsMovementConnectorMock,
      mockAuditService,
      movementsMetricsStub
    )

  override def afterEach(): Unit = {
    reset(customsCacheServiceMock, customsExportsMovementConnectorMock, mockAuditService)
    super.afterEach()
  }

  private def requestAcceptedTest(block: => Any): Any = {
    when(customsExportsMovementConnectorMock.sendArrivalDeclaration(any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(ACCEPTED)))
    when(customsExportsMovementConnectorMock.sendDepartureDeclaration(any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(ACCEPTED)))

    when(customsExportsMovementConnectorMock.sendAssociationRequest(any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(ACCEPTED)))
    when(customsExportsMovementConnectorMock.sendDisassociationRequest(any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(ACCEPTED)))
    when(customsExportsMovementConnectorMock.sendShutMucrRequest(any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(ACCEPTED)))
    block
  }

  "SubmissionService on submitMovementRequest" when {

    "submitting Arrival" should {

      "return response from CustomsDeclareExportsMovementsConnector" in requestAcceptedTest {

        when(customsCacheServiceMock.fetch(any())(any(), any()))
          .thenReturn(Future.successful(Some(CacheMap(Arrival, cacheMapData(Arrival)))))

        val CustomHttpResponseCode = 123
        when(customsExportsMovementConnectorMock.sendArrivalDeclaration(any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(CustomHttpResponseCode)))

        submissionService.submitMovementRequest("EAL-eori1", "eori1", Choice(Arrival)).futureValue must equal(
          CustomHttpResponseCode
        )
        verify(mockAuditService).auditMovements(
          any(),
          any(),
          any(),
          ArgumentMatchers.eq(Choice(Choice.AllowedChoiceValues.Arrival))
        )(any())
        verify(mockAuditService)
          .auditAllPagesUserInput(ArgumentMatchers.eq(Choice(Choice.AllowedChoiceValues.Arrival)), any())(any())
      }

      "call CustomsDeclareExportsMovementsConnector" in requestAcceptedTest {

        when(customsCacheServiceMock.fetch(any())(any(), any()))
          .thenReturn(Future.successful(Some(CacheMap(Arrival, cacheMapData(Arrival)))))

        submissionService.submitMovementRequest("EAL-eori1", "eori1", Choice(Arrival)).futureValue

        verify(customsExportsMovementConnectorMock).sendArrivalDeclaration(any())(any(), any())
        verify(mockAuditService).auditMovements(
          any(),
          any(),
          any(),
          ArgumentMatchers.eq(Choice(Choice.AllowedChoiceValues.Arrival))
        )(any())
        verify(mockAuditService)
          .auditAllPagesUserInput(ArgumentMatchers.eq(Choice(Choice.AllowedChoiceValues.Arrival)), any())(any())
      }

      "return Internal Server Error when no data in cache" in requestAcceptedTest {
        when(customsCacheServiceMock.fetch(any())(any(), any()))
          .thenReturn(Future.successful(None))

        submissionService.submitMovementRequest("EAL-eori1", "eori1", Choice(Arrival)).futureValue must equal(
          INTERNAL_SERVER_ERROR
        )
        verifyZeroInteractions(mockAuditService)
      }
    }

    "submitting Departure" should {

      "return response from CustomsDeclareExportsMovementsConnector" in requestAcceptedTest {

        when(customsCacheServiceMock.fetch(any())(any(), any()))
          .thenReturn(Future.successful(Some(CacheMap(Departure, cacheMapData(Departure)))))

        val CustomHttpResponseCode = 123
        when(customsExportsMovementConnectorMock.sendDepartureDeclaration(any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(CustomHttpResponseCode)))

        submissionService.submitMovementRequest("EDL-eori1", "eori1", Choice(Departure)).futureValue must equal(
          CustomHttpResponseCode
        )
        verify(mockAuditService).auditMovements(
          any(),
          any(),
          any(),
          ArgumentMatchers.eq(Choice(Choice.AllowedChoiceValues.Departure))
        )(any())
        verify(mockAuditService)
          .auditAllPagesUserInput(ArgumentMatchers.eq(Choice(Choice.AllowedChoiceValues.Departure)), any())(any())
      }

      "call CustomsDeclareExportsMovementsConnector" in requestAcceptedTest {

        when(customsCacheServiceMock.fetch(any())(any(), any()))
          .thenReturn(Future.successful(Some(CacheMap(Departure, cacheMapData(Departure)))))

        submissionService.submitMovementRequest("EDL-eori1", "eori1", Choice(Departure)).futureValue

        verify(customsExportsMovementConnectorMock).sendDepartureDeclaration(any())(any(), any())
        verify(mockAuditService).auditMovements(
          any(),
          any(),
          any(),
          ArgumentMatchers.eq(Choice(Choice.AllowedChoiceValues.Departure))
        )(any())
        verify(mockAuditService)
          .auditAllPagesUserInput(ArgumentMatchers.eq(Choice(Choice.AllowedChoiceValues.Departure)), any())(any())
      }

      "return Internal Server Error when no data in cache" in requestAcceptedTest {
        when(customsCacheServiceMock.fetch(any())(any(), any()))
          .thenReturn(Future.successful(None))

        submissionService.submitMovementRequest("EDL-eori1", "eori1", Choice(Departure)).futureValue must equal(
          INTERNAL_SERVER_ERROR
        )
        verifyZeroInteractions(mockAuditService)
      }
    }
  }

  "SubmissionService on submitDucrAssociation" should {

    "return response from CustomsDeclareExportsMovementsConnector" in {

      val CustomHttpResponseCode = 123
      when(customsExportsMovementConnectorMock.sendAssociationRequest(any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(CustomHttpResponseCode)))

      submissionService
        .submitDucrAssociation(MucrOptions(ValidMucr), AssociateDucr(ValidDucr), "eori")
        .futureValue must equal(CustomHttpResponseCode)
      verify(mockAuditService)
        .auditAssociate(ArgumentMatchers.eq("eori"), any(), any(), any())(any())
    }

    "call CustomsDeclareExportsMovementsConnector, passing correctly built request" in requestAcceptedTest {

      submissionService.submitDucrAssociation(MucrOptions(ValidMucr), AssociateDucr(ValidDucr), "eori").futureValue

      val requestCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      verify(customsExportsMovementConnectorMock).sendAssociationRequest(requestCaptor.capture())(any(), any())
      verify(mockAuditService)
        .auditAssociate(ArgumentMatchers.eq("eori"), any(), any(), any())(any())

      assertEqual(XML.loadString(requestCaptor.getValue), exampleAssociateDucrRequestXml)
    }

    "return Internal Server Error when no data in cache" in requestAcceptedTest {
      when(customsExportsMovementConnectorMock.sendAssociationRequest(any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR)))

      submissionService
        .submitDucrAssociation(MucrOptions(ValidMucr), AssociateDucr(ValidDucr), "eori")
        .futureValue must equal(INTERNAL_SERVER_ERROR)
      verify(mockAuditService)
        .auditAssociate(ArgumentMatchers.eq("eori"), any(), any(), ArgumentMatchers.eq(INTERNAL_SERVER_ERROR.toString))(
          any()
        )
    }
  }

  "SubmissionService on submitDucrDisassociation" should {

    "return response from CustomsDeclareExportsMovementsConnector" in {

      val CustomHttpResponseCode = 123
      when(customsExportsMovementConnectorMock.sendDisassociationRequest(any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(CustomHttpResponseCode)))

      submissionService.submitDucrDisassociation(DisassociateDucr(ValidDucr), "eori").futureValue must equal(
        CustomHttpResponseCode
      )
      verify(mockAuditService).auditDisassociate(ArgumentMatchers.eq("eori"), any(), any())(any())

    }

    "call CustomsDeclareExportsMovementsConnector, passing correctly built request" in requestAcceptedTest {

      submissionService.submitDucrDisassociation(DisassociateDucr(ValidDucr), "eori").futureValue

      val requestCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      verify(customsExportsMovementConnectorMock).sendDisassociationRequest(requestCaptor.capture())(any(), any())
      verify(mockAuditService).auditDisassociate(ArgumentMatchers.eq("eori"), any(), any())(any())

      assertEqual(XML.loadString(requestCaptor.getValue), exampleDisassociateDucrRequestXml)
    }

    "return Internal Server Error when no data in cache" in requestAcceptedTest {
      when(customsExportsMovementConnectorMock.sendDisassociationRequest(any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR)))

      submissionService.submitDucrDisassociation(DisassociateDucr(ValidDucr), "eori").futureValue must equal(
        INTERNAL_SERVER_ERROR
      )
      verify(mockAuditService)
        .auditDisassociate(ArgumentMatchers.eq("eori"), any(), ArgumentMatchers.eq(INTERNAL_SERVER_ERROR.toString))(
          any()
        )
    }

    "increase counter for successful submissions" in requestAcceptedTest {
      counter("disassociation.counter") must changeOn {
        submissionService.submitDucrDisassociation(DisassociateDucr(ValidDucr), "eori").futureValue
      }
    }

    "use timer to measure execution of successful disassociate request" in requestAcceptedTest {
      timer("disassociation.timer") must changeOn {
        submissionService.submitDucrDisassociation(DisassociateDucr(ValidDucr), "eori").futureValue
      }
    }
  }

  "SubmissionService on submitShutMucrRequest" should {

    "return response from CustomsDeclareExportsMovementsConnector" in {

      val CustomHttpResponseCode = 123
      when(customsExportsMovementConnectorMock.sendShutMucrRequest(any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(CustomHttpResponseCode)))

      submissionService.submitShutMucrRequest(ShutMucr(ValidMucr), "eori").futureValue must equal(
        CustomHttpResponseCode
      )
      verify(mockAuditService).auditShutMucr(ArgumentMatchers.eq("eori"), any(), any())(any())
    }

    "call CustomsDeclareExportsMovementsConnector, passing correctly built request" in requestAcceptedTest {

      submissionService.submitShutMucrRequest(ShutMucr(ValidMucr), "eori").futureValue

      val requestCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      verify(customsExportsMovementConnectorMock).sendShutMucrRequest(requestCaptor.capture())(any(), any())
      verify(mockAuditService).auditShutMucr(ArgumentMatchers.eq("eori"), any(), any())(any())

      assertEqual(XML.loadString(requestCaptor.getValue), exampleShutMucrRequestXml)
    }

    "return Internal Server Error when no data in cache" in requestAcceptedTest {
      when(customsExportsMovementConnectorMock.sendShutMucrRequest(any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR)))

      submissionService.submitShutMucrRequest(ShutMucr(ValidMucr), "eori").futureValue must equal(INTERNAL_SERVER_ERROR)
      verify(mockAuditService)
        .auditShutMucr(ArgumentMatchers.eq("eori"), any(), ArgumentMatchers.eq(INTERNAL_SERVER_ERROR.toString))(any())
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

  private def assertEqual(actual: Node, expected: Node): Unit = {
    val actualTrimmed = Utility.trim(actual)
    val expectedTrimmed = Utility.trim(expected)
    actualTrimmed must equal(expectedTrimmed)
  }

}
