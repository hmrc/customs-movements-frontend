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

package connectors

import testdata.CommonTestData._
import testdata.ConsolidationTestData._
import testdata.MovementsTestData
import testdata.MovementsTestData.exampleSubmissionFrontendModel
import testdata.NotificationTestData.exampleNotificationFrontendModel
import config.AppConfig
import forms.Choice.AllowedChoiceValues.{Arrival, Departure}
import models.notifications.NotificationFrontendModel
import models.submissions.SubmissionFrontendModel
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}
import play.api.http.{ContentTypes, HeaderNames}
import play.api.libs.json.Json
import play.api.mvc.Codec
import play.api.test.Helpers.OK
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CustomsDeclareExportsMovementsConnectorSpec
    extends WordSpec with MustMatchers with MockitoSugar with ScalaFutures {

  import CustomsDeclareExportsMovementsConnectorSpec._

  private trait Test {
    implicit val headerCarrierMock: HeaderCarrier = mock[HeaderCarrier]
    val appConfigMock: AppConfig = mock[AppConfig]
    val httpClientMock: HttpClient = mock[HttpClient]
    val defaultHttpResponse = HttpResponse(OK, Some(Json.toJson("Success")))

    when(httpClientMock.POSTString[HttpResponse](any(), any(), any())(any(), any(), any()))
      .thenReturn(Future.successful(defaultHttpResponse))
    when(httpClientMock.GET(any())(any(), any(), any())).thenReturn(Future.failed(new NotImplementedError()))

    val connector = new CustomsDeclareExportsMovementsConnector(appConfigMock, httpClientMock)
  }

  "CustomsDeclareExportsMovementsConnector on sendArrivalDeclaration" should {

    "call HttpClient, passing URL for Arrival submission endpoint" in new Test {

      connector.sendArrivalDeclaration(movementSubmissionRequestXmlString(Arrival)).futureValue

      val expectedSubmissionUrl =
        s"${appConfigMock.customsDeclareExportsMovements}${appConfigMock.movementArrivalSubmissionUri}"
      verify(httpClientMock).POSTString(meq(expectedSubmissionUrl), any(), any())(any(), any(), any())
    }

    "call HttpClient, passing body provided" in new Test {

      connector.sendArrivalDeclaration(movementSubmissionRequestXmlString(Arrival)).futureValue

      verify(httpClientMock).POSTString(any(), meq(movementSubmissionRequestXmlString(Arrival)), any())(
        any(),
        any(),
        any()
      )
    }

    "call HttpClient, passing correct headers" in new Test {

      connector.sendArrivalDeclaration(movementSubmissionRequestXmlString(Arrival)).futureValue

      verify(httpClientMock).POSTString(any(), any(), meq(expectedMovementSubmissionRequestHeaders))(
        any(),
        any(),
        any()
      )
    }

    "return response from HttpClient" in new Test {

      val result =
        connector.sendArrivalDeclaration(movementSubmissionRequestXmlString(Arrival)).futureValue

      result must equal(defaultHttpResponse)
    }
  }

  "CustomsDeclareExportsMovementsConnector on sendDepartureDeclaration" should {

    "call HttpClient, passing URL for Departure submission endpoint" in new Test {

      connector
        .sendDepartureDeclaration(movementSubmissionRequestXmlString(Departure))
        .futureValue

      val expectedSubmissionUrl =
        s"${appConfigMock.customsDeclareExportsMovements}${appConfigMock.movementDepartureSubmissionUri}"
      verify(httpClientMock).POSTString(meq(expectedSubmissionUrl), any(), any())(any(), any(), any())
    }

    "call HttpClient, passing body provided" in new Test {

      connector
        .sendDepartureDeclaration(movementSubmissionRequestXmlString(Departure))
        .futureValue

      verify(httpClientMock).POSTString(any(), meq(movementSubmissionRequestXmlString(Departure)), any())(
        any(),
        any(),
        any()
      )
    }

    "call HttpClient, passing correct headers" in new Test {

      connector
        .sendDepartureDeclaration(movementSubmissionRequestXmlString(Departure))
        .futureValue

      verify(httpClientMock).POSTString(any(), any(), meq(expectedMovementSubmissionRequestHeaders))(
        any(),
        any(),
        any()
      )
    }

    "return response from HttpClient" in new Test {

      val result = connector
        .sendDepartureDeclaration(movementSubmissionRequestXmlString(Departure))
        .futureValue

      result must equal(defaultHttpResponse)
    }
  }

  "CustomsDeclareExportsMovementsConnector on sendAssociationRequest" should {

    "call HttpClient, passing URL for Association endpoint" in new Test {

      connector.sendAssociationRequest(exampleAssociateDucrRequestXml.toString).futureValue

      val expectedUrl =
        s"${appConfigMock.customsDeclareExportsMovements}${appConfigMock.movementConsolidationAssociateUri}"
      verify(httpClientMock).POSTString(meq(expectedUrl), any(), any())(any(), any(), any())
    }

    "call HttpClient, passing body provided" in new Test {

      connector.sendAssociationRequest(exampleAssociateDucrRequestXml.toString).futureValue

      verify(httpClientMock).POSTString(any(), meq(exampleAssociateDucrRequestXml.toString), any())(any(), any(), any())
    }

    "call HttpClient, passing correct headers" in new Test {

      connector.sendAssociationRequest(exampleAssociateDucrRequestXml.toString).futureValue

      verify(httpClientMock).POSTString(any(), any(), meq(validConsolidationRequestHeaders))(any(), any(), any())
    }

    "return response from HttpClient" in new Test {

      val result = connector.sendAssociationRequest(exampleAssociateDucrRequestXml.toString).futureValue

      result must equal(defaultHttpResponse)
    }
  }

  "CustomsDeclareExportsMovementsConnector on sendDisassociationRequest" should {

    "call HttpClient, passing URL for Disassociation endpoint" in new Test {

      connector.sendDisassociationRequest(exampleDisassociateDucrRequestXml.toString).futureValue

      val expectedUrl =
        s"${appConfigMock.customsDeclareExportsMovements}${appConfigMock.movementConsolidationDisassociateUri}"
      verify(httpClientMock).POSTString(meq(expectedUrl), any(), any())(any(), any(), any())
    }

    "call HttpClient, passing body provided" in new Test {

      connector.sendDisassociationRequest(exampleDisassociateDucrRequestXml.toString).futureValue

      verify(httpClientMock).POSTString(any(), meq(exampleDisassociateDucrRequestXml.toString), any())(
        any(),
        any(),
        any()
      )
    }

    "call HttpClient, passing correct headers" in new Test {

      connector.sendDisassociationRequest(exampleDisassociateDucrRequestXml.toString).futureValue

      verify(httpClientMock).POSTString(any(), any(), meq(validConsolidationRequestHeaders))(any(), any(), any())
    }

    "return response from HttpClient" in new Test {

      val result = connector.sendDisassociationRequest(exampleDisassociateDucrRequestXml.toString).futureValue

      result must equal(defaultHttpResponse)
    }
  }

  "CustomsDeclareExportsMovementsConnector on sendShutMucrRequest" should {

    "call HttpClient, passing URL for Shut Mucr endpoint" in new Test {

      connector.sendShutMucrRequest(exampleShutMucrRequestXml.toString).futureValue

      val expectedUrl =
        s"${appConfigMock.customsDeclareExportsMovements}${appConfigMock.movementConsolidationShutMucrUri}"
      verify(httpClientMock).POSTString(meq(expectedUrl), any(), any())(any(), any(), any())
    }

    "call HttpClient, passing body provided" in new Test {

      connector.sendShutMucrRequest(exampleShutMucrRequestXml.toString).futureValue

      verify(httpClientMock).POSTString(any(), meq(exampleShutMucrRequestXml.toString), any())(any(), any(), any())
    }

    "call HttpClient, passing correct headers" in new Test {

      connector.sendShutMucrRequest(exampleShutMucrRequestXml.toString).futureValue

      verify(httpClientMock).POSTString(any(), any(), meq(validConsolidationRequestHeaders))(any(), any(), any())
    }

    "return response from HttpClient" in new Test {

      val result = connector.sendShutMucrRequest(exampleShutMucrRequestXml.toString).futureValue

      result must equal(defaultHttpResponse)
    }
  }

  "CustomsDeclareExportsMovementsConnector on fetchNotifications" should {

    "call HttpClient, passing URL for fetch Notifications endpoint" in new Test {

      when(httpClientMock.GET[Seq[NotificationFrontendModel]](any())(any(), any(), any()))
        .thenReturn(Future.successful(Seq.empty))

      connector.fetchNotifications(conversationId).futureValue

      val expectedUrl =
        s"${appConfigMock.customsDeclareExportsMovements}${appConfigMock.fetchNotifications}/$conversationId"
      verify(httpClientMock).GET(meq(expectedUrl))(any(), any(), any())
    }

    "return response from HttpClient" in new Test {

      val expectedResponseContent = Seq(
        exampleNotificationFrontendModel(conversationId = conversationId),
        exampleNotificationFrontendModel(conversationId = conversationId, masterRoe = None),
        exampleNotificationFrontendModel(conversationId = conversationId, masterSoe = None)
      )
      when(httpClientMock.GET[Seq[NotificationFrontendModel]](any())(any(), any(), any()))
        .thenReturn(Future.successful(expectedResponseContent))

      val result: Seq[NotificationFrontendModel] = connector.fetchNotifications(conversationId).futureValue

      result.length must equal(expectedResponseContent.length)
      result must equal(expectedResponseContent)
    }
  }

  "CustomsDeclareExportsMovementsConnector on fetchAllSubmissions" should {

    "call HttpClient, passing URL for fetch all Submissions endpoint" in new Test {

      when(httpClientMock.GET[Seq[SubmissionFrontendModel]](any())(any(), any(), any()))
        .thenReturn(Future.successful(Seq.empty))

      connector.fetchAllSubmissions().futureValue

      val expectedUrl = s"${appConfigMock.customsDeclareExportsMovements}${appConfigMock.fetchAllSubmissions}"
      verify(httpClientMock).GET(meq(expectedUrl))(any(), any(), any())
    }

    "return response from HttpClient" in new Test {

      val expectedResponseContent = Seq(
        exampleSubmissionFrontendModel(conversationId = conversationId),
        exampleSubmissionFrontendModel(conversationId = conversationId_2),
        exampleSubmissionFrontendModel(conversationId = conversationId_3)
      )
      when(httpClientMock.GET[Seq[SubmissionFrontendModel]](any())(any(), any(), any()))
        .thenReturn(Future.successful(expectedResponseContent))

      val result: Seq[SubmissionFrontendModel] = connector.fetchAllSubmissions().futureValue

      result.length must equal(expectedResponseContent.length)
      result must equal(expectedResponseContent)
    }
  }

  "CustomsDeclareExportsMovementsConnector on fetchSingleSubmission" should {

    "call HttpClient, passing URL for fetch single Submission endpoint" in new Test {

      when(httpClientMock.GET[Option[SubmissionFrontendModel]](any())(any(), any(), any()))
        .thenReturn(Future.successful(None))

      connector.fetchSingleSubmission(conversationId).futureValue

      val expectedUrl =
        s"${appConfigMock.customsDeclareExportsMovements}${appConfigMock.fetchSingleSubmission}/$conversationId"
      verify(httpClientMock).GET(meq(expectedUrl))(any(), any(), any())
    }

    "return response from HttpClient" in new Test {

      val expectedResponseContent = Some(exampleSubmissionFrontendModel(conversationId = conversationId))
      when(httpClientMock.GET[Option[SubmissionFrontendModel]](any())(any(), any(), any()))
        .thenReturn(Future.successful(expectedResponseContent))

      val result: Option[SubmissionFrontendModel] = connector.fetchSingleSubmission(conversationId).futureValue

      result must equal(expectedResponseContent)
    }
  }

}

object CustomsDeclareExportsMovementsConnectorSpec {

  def movementSubmissionRequest(movementType: String): InventoryLinkingMovementRequest =
    MovementsTestData.validMovementRequest(movementType)
  def movementSubmissionRequestXmlString(movementType: String): String = movementSubmissionRequest(movementType).toXml

  val expectedMovementSubmissionRequestHeaders: Seq[(String, String)] =
    Seq(HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8), HeaderNames.ACCEPT -> ContentTypes.XML(Codec.utf_8))

}
