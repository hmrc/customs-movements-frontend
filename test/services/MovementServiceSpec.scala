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

import base.MockFactory
import base.testdata.ConsolidationTestData._
import base.testdata.MovementsTestData._
import forms.Choice.AllowedChoiceValues.Arrival
import forms._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{MustMatchers, WordSpec}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.test.Helpers.ACCEPTED
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.{Node, Utility, XML}

class MovementServiceSpec extends WordSpec with MustMatchers with MockitoSugar with ScalaFutures {

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(100, Millis))

  private trait Test {
    implicit val headerCarrierMock = mock[HeaderCarrier]
    val customsCacheServiceMock = MockFactory.buildCustomsCacheServiceMock
    val customsExportsMovementConnectorMock = MockFactory.buildCustomsDeclareExportsMovementsConnectorMock
    val metricsMock = MockFactory.buildMovementsMetricsMock
    val submissionService =
      new SubmissionService(customsCacheServiceMock, customsExportsMovementConnectorMock, metricsMock)
  }

  private trait RequestAcceptedTest extends Test {
    when(customsExportsMovementConnectorMock.submitMovementDeclaration(any(), any(), any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(ACCEPTED)))
    when(customsExportsMovementConnectorMock.sendConsolidationRequest(any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(ACCEPTED)))
  }

  "SubmissionService on submitMovementRequest" should {

    "submit valid Movement Request" in new RequestAcceptedTest {

      when(customsCacheServiceMock.fetch(any())(any(), any()))
        .thenReturn(Future.successful(Some(CacheMap(Arrival, cacheMapData(Arrival)))))

      submissionService.submitMovementRequest("EAL-eori1", "eori1", Choice(Arrival)).futureValue must equal(ACCEPTED)
    }

    "handle failure when no data" in new RequestAcceptedTest {

      submissionService.submitMovementRequest("EAL-eori1", "eori1", Choice(Arrival)).futureValue must equal(
        INTERNAL_SERVER_ERROR
      )
    }
  }

  "SubmissionService on submitDucrAssociation" should {

    "return response from CustomsDeclareExportsMovementsConnector" in new Test {

      val CustomHttpResponseCode = 123
      when(customsExportsMovementConnectorMock.sendConsolidationRequest(any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(CustomHttpResponseCode)))

      submissionService.submitDucrAssociation(MucrOptions(ValidMucr), AssociateDucr(ValidDucr)).futureValue must equal(
        CustomHttpResponseCode
      )
    }

    "call CustomsDeclareExportsMovementsConnector, passing correctly built request" in new RequestAcceptedTest {

      submissionService.submitDucrAssociation(MucrOptions(ValidMucr), AssociateDucr(ValidDucr)).futureValue

      val requestCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      verify(customsExportsMovementConnectorMock).sendConsolidationRequest(requestCaptor.capture())(any(), any())

      assertEqual(XML.loadString(requestCaptor.getValue), exampleAssociateDucrRequestXml)
    }
  }

  "SubmissionService on submitDucrDisassociation" should {

    "return response from CustomsDeclareExportsMovementsConnector" in new Test {

      val CustomHttpResponseCode = 123
      when(customsExportsMovementConnectorMock.sendConsolidationRequest(any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(CustomHttpResponseCode)))

      submissionService.submitDucrDisassociation(DisassociateDucr(ValidDucr)).futureValue must equal(
        CustomHttpResponseCode
      )
    }

    "call CustomsDeclareExportsMovementsConnector, passing correctly built request" in new RequestAcceptedTest {

      submissionService.submitDucrDisassociation(DisassociateDucr(ValidDucr)).futureValue

      val requestCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      verify(customsExportsMovementConnectorMock).sendConsolidationRequest(requestCaptor.capture())(any(), any())

      assertEqual(XML.loadString(requestCaptor.getValue), exampleDisassociateDucrRequestXml)
    }
  }

  "SubmissionService on submitShutMucrRequest" should {

    "return response from CustomsDeclareExportsMovementsConnector" in new Test {

      val CustomHttpResponseCode = 123
      when(customsExportsMovementConnectorMock.sendConsolidationRequest(any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(CustomHttpResponseCode)))

      submissionService.submitShutMucrRequest(ShutMucr(ValidMucr)).futureValue must equal(CustomHttpResponseCode)
    }

    "call CustomsDeclareExportsMovementsConnector, passing correctly built request" in new RequestAcceptedTest {

      submissionService.submitShutMucrRequest(ShutMucr(ValidMucr)).futureValue

      val requestCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      verify(customsExportsMovementConnectorMock).sendConsolidationRequest(requestCaptor.capture())(any(), any())

      assertEqual(XML.loadString(requestCaptor.getValue), exampleShutMucrRequestXml)
    }
  }

  private def assertEqual(actual: Node, expected: Node): Unit = {
    val actualTrimmed = Utility.trim(actual)
    val expectedTrimmed = Utility.trim(expected)
    actualTrimmed must equal(expectedTrimmed)
  }

}
