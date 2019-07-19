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

import base.ExportsTestData.cacheMapData
import base.testdata.CommonTestData.ucr
import base.testdata.ConsolidationTestData._
import config.AppConfig
import forms.Choice.AllowedChoiceValues.Arrival
import forms.{Choice, Movement}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}
import play.api.http.{ContentTypes, HeaderNames}
import play.api.libs.json.Json
import play.api.mvc.Codec
import play.api.test.Helpers.OK
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import utils.CustomsHeaderNames

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

    val connector = new CustomsDeclareExportsMovementsConnector(appConfigMock, httpClientMock)
  }

  "CustomsDeclareExportsMovementsConnector on submitMovementDeclaration" should {

    "return response from HttpClient" in new Test {

      val result = connector.submitMovementDeclaration(ucr, Arrival, movementSubmissionRequestXmlString).futureValue

      result must equal(defaultHttpResponse)
    }

    "call HttpClient with URL for movements submission endpoint" in new Test {

      connector.submitMovementDeclaration(ucr, Arrival, movementSubmissionRequestXmlString).futureValue

      val expectedMovementSubmissionUrl =
        s"${appConfigMock.customsDeclareExportsMovements}${appConfigMock.saveMovementSubmission}"
      verify(httpClientMock).POSTString(meq(expectedMovementSubmissionUrl), any(), any())(any(), any(), any())
    }

    "call HttpClient with body provided" in new Test {

      connector.submitMovementDeclaration(ucr, Arrival, movementSubmissionRequestXmlString).futureValue

      verify(httpClientMock).POSTString(any(), meq(movementSubmissionRequestXmlString), any())(any(), any(), any())
    }

    "call HttpClient with correct headers" in new Test {

      connector.submitMovementDeclaration(ucr, Arrival, movementSubmissionRequestXmlString).futureValue

      verify(httpClientMock).POSTString(any(), any(), meq(expectedMovementSubmissionRequestHeaders))(
        any(),
        any(),
        any()
      )
    }
  }

  "CustomsDeclareExportsMovementsConnector on sendConsolidationRequest" should {

    "return response from HttpClient" in new Test {

      val result = connector.sendConsolidationRequest(exampleShutMucrConsolidationRequestXml.toString).futureValue

      result must equal(defaultHttpResponse)
    }

    "call HttpClient with URL for movements consolidation endpoint" in new Test {

      connector.sendConsolidationRequest(exampleShutMucrConsolidationRequestXml.toString).futureValue

      val expectedConsolidationUrl =
        s"${appConfigMock.customsDeclareExportsMovements}${appConfigMock.submitMovementConsolidation}"
      verify(httpClientMock).POSTString(meq(expectedConsolidationUrl), any(), any())(any(), any(), any())
    }

    "call HttpClient with body provided" in new Test {

      connector.sendConsolidationRequest(exampleShutMucrConsolidationRequestXml.toString).futureValue

      verify(httpClientMock).POSTString(any(), meq(exampleShutMucrConsolidationRequestXml.toString), any())(
        any(),
        any(),
        any()
      )
    }

    "call HttpClient with correct headers" in new Test {

      connector.sendConsolidationRequest(exampleShutMucrConsolidationRequestXml.toString).futureValue

      verify(httpClientMock).POSTString(any(), any(), meq(validConsolidationRequestHeaders))(any(), any(), any())
    }
  }

}

object CustomsDeclareExportsMovementsConnectorSpec {

  // TODO: Construct it in a more clear way
  val movementSubmissionRequest =
    Movement.createMovementRequest(CacheMap(Arrival, cacheMapData(Arrival)), "eori1", Choice(Arrival))
  val movementSubmissionRequestXmlString: String = movementSubmissionRequest.toXml

  val expectedMovementSubmissionRequestHeaders: Seq[(String, String)] = Seq(
    HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8),
    HeaderNames.ACCEPT -> ContentTypes.XML(Codec.utf_8),
    CustomsHeaderNames.XUcr -> movementSubmissionRequest.ucrBlock.ucr,
    CustomsHeaderNames.XMovementType -> movementSubmissionRequest.messageCode
  )

}
