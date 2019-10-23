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

package unit.connectors

import config.AppConfig
import connectors.CustomsDeclareExportsMovementsConnector
import forms.Choice
import forms.Choice.{Arrival, Departure}
import models.external.requests.ConsolidationRequest
import models.notifications.NotificationFrontendModel
import models.requests.MovementRequest
import models.submissions.SubmissionFrontendModel
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{verify, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.Json
import play.api.test.Helpers.OK
import testdata.CommonTestData._
import testdata.ConsolidationTestData._
import testdata.MovementsTestData
import testdata.MovementsTestData.exampleSubmissionFrontendModel
import testdata.NotificationTestData.exampleNotificationFrontendModel
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import unit.base.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CustomsDeclareExportsMovementsConnectorSpec extends UnitSpec with ScalaFutures {

  import CustomsDeclareExportsMovementsConnectorSpec._

  private trait Test {
    implicit val headerCarrierMock: HeaderCarrier = mock[HeaderCarrier]
    val appConfigMock: AppConfig = mock[AppConfig]
    val httpClientMock: HttpClient = mock[HttpClient]
    val defaultHttpResponse = HttpResponse(OK, Some(Json.toJson("Success")))

    when(httpClientMock.POST[MovementRequest, HttpResponse](any(), any(), any())(any(), any(), any(), any()))
      .thenReturn(Future.successful(defaultHttpResponse))
    when(httpClientMock.GET(any())(any(), any(), any())).thenReturn(Future.failed(new NotImplementedError()))

    val connector = new CustomsDeclareExportsMovementsConnector(appConfigMock, httpClientMock)
  }

  "CustomsDeclareExportsMovementsConnector on sendArrivalDeclaration" should {

    "call HttpClient, passing URL for Arrival submission endpoint" in new Test {

      connector.sendArrivalDeclaration(movementSubmissionRequest(Arrival)).futureValue

      val expectedSubmissionUrl =
        s"${appConfigMock.customsDeclareExportsMovements}${appConfigMock.movementsSubmissionUri}"
      verify(httpClientMock).POST(meq(expectedSubmissionUrl), any(), any())(any(), any(), any(), any())
    }

    "call HttpClient, passing body provided" in new Test {

      connector.sendArrivalDeclaration(movementSubmissionRequest(Arrival)).futureValue

      verify(httpClientMock).POST(any(), meq(movementSubmissionRequest(Arrival)), any())(any(), any(), any(), any())
    }

    "call HttpClient, passing correct headers" in new Test {

      connector.sendArrivalDeclaration(movementSubmissionRequest(Arrival)).futureValue

      verify(httpClientMock).POST(any(), any(), any())(any(), any(), any(), any())
    }

    "return response from HttpClient" in new Test {

      val result = connector.sendArrivalDeclaration(movementSubmissionRequest(Arrival)).futureValue

      result must equal(defaultHttpResponse)
    }
  }

  "CustomsDeclareExportsMovementsConnector on sendDepartureDeclaration" should {

    "call HttpClient, passing URL for Departure submission endpoint" in new Test {

      connector.sendDepartureDeclaration(movementSubmissionRequest(Departure)).futureValue

      val expectedSubmissionUrl =
        s"${appConfigMock.customsDeclareExportsMovements}${appConfigMock.movementsSubmissionUri}"
      verify(httpClientMock).POST(meq(expectedSubmissionUrl), any(), any())(any(), any(), any(), any())
    }

    "call HttpClient, passing body provided" in new Test {

      connector
        .sendDepartureDeclaration(movementSubmissionRequest(Departure))
        .futureValue

      verify(httpClientMock).POST(any(), meq(movementSubmissionRequest(Departure)), any())(any(), any(), any(), any())
    }

    "call HttpClient, passing correct headers" in new Test {

      connector
        .sendDepartureDeclaration(movementSubmissionRequest(Departure))
        .futureValue

      verify(httpClientMock).POST(any(), any(), any())(any(), any(), any(), any())
    }

    "return response from HttpClient" in new Test {

      val result = connector
        .sendDepartureDeclaration(movementSubmissionRequest(Departure))
        .futureValue

      result must equal(defaultHttpResponse)
    }
  }

  "CustomsDeclareExportsMovementsConnector on sendConsolidation" should {

    "call HttpClient for Association" in new Test {

      when(httpClientMock.POST[ConsolidationRequest, ConsolidationRequest](any(), any(), any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(exampleAssociateDucrRequest))

      val result = connector.sendConsolidationRequest(exampleAssociateDucrRequest).futureValue

      val expectedUrl =
        s"${appConfigMock.customsDeclareExportsMovements}${appConfigMock.movementConsolidationUri}"

      result must equal(exampleAssociateDucrRequest)

      verify(httpClientMock).POST(meq(expectedUrl), meq(exampleAssociateDucrRequest), meq(validConsolidationRequestHeaders))(
        any(),
        any(),
        any(),
        any()
      )
    }

    "call HttpClient for Disassociation" in new Test {

      when(httpClientMock.POST[ConsolidationRequest, ConsolidationRequest](any(), any(), any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(exampleDisassociateDucrRequest))

      val result = connector.sendConsolidationRequest(exampleDisassociateDucrRequest).futureValue

      val expectedUrl =
        s"${appConfigMock.customsDeclareExportsMovements}${appConfigMock.movementConsolidationUri}"

      result must equal(exampleDisassociateDucrRequest)

      verify(httpClientMock).POST(meq(expectedUrl), meq(exampleDisassociateDucrRequest), meq(validConsolidationRequestHeaders))(
        any(),
        any(),
        any(),
        any()
      )
    }

    "call HttpClient for Shut Mucr " in new Test {

      when(httpClientMock.POST[ConsolidationRequest, ConsolidationRequest](any(), any(), any())(any(), any(), any(), any()))
        .thenReturn(Future.successful(exampleShutMucrRequest))

      val result = connector.sendConsolidationRequest(exampleShutMucrRequest).futureValue

      val expectedUrl =
        s"${appConfigMock.customsDeclareExportsMovements}${appConfigMock.movementConsolidationUri}"

      result must equal(exampleShutMucrRequest)

      verify(httpClientMock).POST(meq(expectedUrl), meq(exampleShutMucrRequest), meq(validConsolidationRequestHeaders))(any(), any(), any(), any())
    }
  }

  "CustomsDeclareExportsMovementsConnector on fetchNotifications" should {

    "call HttpClient, passing EORI and URL and query params for fetch Notifications endpoint" in new Test {

      when(httpClientMock.GET[Seq[NotificationFrontendModel]](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Seq.empty))

      connector.fetchNotifications(conversationId, validEori).futureValue

      val expectedUrl =
        s"${appConfigMock.customsDeclareExportsMovements}${appConfigMock.fetchNotifications}/$conversationId"
      val expectedQueryParameters = Seq("eori" -> validEori)
      verify(httpClientMock).GET(meq(expectedUrl), meq(expectedQueryParameters))(any(), any(), any())
    }

    "return response from HttpClient" in new Test {

      val expectedResponseContent = Seq(
        exampleNotificationFrontendModel(conversationId = conversationId),
        exampleNotificationFrontendModel(conversationId = conversationId),
        exampleNotificationFrontendModel(conversationId = conversationId)
      )
      when(httpClientMock.GET[Seq[NotificationFrontendModel]](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(expectedResponseContent))

      val result: Seq[NotificationFrontendModel] = connector.fetchNotifications(conversationId, validEori).futureValue

      result.length must equal(expectedResponseContent.length)
      result must equal(expectedResponseContent)
    }
  }

  "CustomsDeclareExportsMovementsConnector on fetchAllSubmissions" should {

    "call HttpClient, passing EORI and URL and query params for fetch all Submissions endpoint" in new Test {

      when(httpClientMock.GET[Seq[SubmissionFrontendModel]](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Seq.empty))

      connector.fetchAllSubmissions(validEori).futureValue

      val expectedUrl = s"${appConfigMock.customsDeclareExportsMovements}${appConfigMock.fetchAllSubmissions}"
      val expectedQueryParameters = Seq("eori" -> validEori)
      verify(httpClientMock).GET(meq(expectedUrl), meq(expectedQueryParameters))(any(), any(), any())
    }

    "return response from HttpClient" in new Test {

      val expectedResponseContent = Seq(
        exampleSubmissionFrontendModel(conversationId = conversationId),
        exampleSubmissionFrontendModel(conversationId = conversationId_2),
        exampleSubmissionFrontendModel(conversationId = conversationId_3)
      )
      when(httpClientMock.GET[Seq[SubmissionFrontendModel]](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(expectedResponseContent))

      val result: Seq[SubmissionFrontendModel] = connector.fetchAllSubmissions(validEori).futureValue

      result.length must equal(expectedResponseContent.length)
      result must equal(expectedResponseContent)
    }
  }

  "CustomsDeclareExportsMovementsConnector on fetchSingleSubmission" should {

    "call HttpClient, passing EORI and URL for fetch single Submission endpoint" in new Test {

      when(httpClientMock.GET[Option[SubmissionFrontendModel]](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(None))

      connector.fetchSingleSubmission(conversationId, validEori).futureValue

      val expectedUrl =
        s"${appConfigMock.customsDeclareExportsMovements}${appConfigMock.fetchSingleSubmission}/$conversationId"
      val expectedQueryParameters = Seq("eori" -> validEori)
      verify(httpClientMock).GET(meq(expectedUrl), meq(expectedQueryParameters))(any(), any(), any())
    }

    "return response from HttpClient" in new Test {

      val expectedResponseContent = Some(exampleSubmissionFrontendModel(conversationId = conversationId))
      when(httpClientMock.GET[Option[SubmissionFrontendModel]](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(expectedResponseContent))

      val result: Option[SubmissionFrontendModel] = connector.fetchSingleSubmission(conversationId, validEori).futureValue

      result must equal(expectedResponseContent)
    }
  }

}

object CustomsDeclareExportsMovementsConnectorSpec {
  def movementSubmissionRequest(movementType: Choice): MovementRequest =
    MovementsTestData.validMovementRequest(movementType)
}
