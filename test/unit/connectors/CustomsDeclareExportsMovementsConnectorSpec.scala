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
import connectors.exchanges.DisassociateDUCRRequest
import forms.ConsignmentReferences
import models.notifications.ResponseType.ControlResponse
import models.requests.{MovementDetailsRequest, MovementRequest, MovementType}
import org.mockito.BDDMockito.given
import play.api.http.Status
import play.api.test.Helpers._
import testdata.CommonTestData._
import testdata.MovementsTestData.exampleSubmission
import testdata.NotificationTestData.exampleNotificationFrontendModel
import com.github.tomakehurst.wiremock.client.WireMock.{post, aResponse, get, verify, postRequestedFor, getRequestedFor, urlEqualTo, equalTo}

class CustomsDeclareExportsMovementsConnectorSpec extends ConnectorSpec {

  private val config = mock[AppConfig]
  given(config.customsDeclareExportsMovements).willReturn(downstreamURL)

  private val connector = new CustomsDeclareExportsMovementsConnector(config, httpClient)

  "Submit Movement" should {

    "POST to the Back End" in {
      stubFor(
        post("/movements")
          .willReturn(
            aResponse()
              .withStatus(Status.ACCEPTED)
          )
      )

      val request =
        MovementRequest("eori", MovementType.Arrival, ConsignmentReferences("ref", "value"), MovementDetailsRequest("datetime"))
      connector.submit(request).futureValue

      verify(
        postRequestedFor(urlEqualTo("/movements"))
          .withRequestBody(
            equalTo(
              """{"eori":"eori","choice":"EAL","consignmentReference":{"reference":"ref","referenceValue":"value"},"movementDetails":{"dateTime":"datetime"}}"""
            )
          )
      )
    }
  }

  "Submit Consolidation" should {

    "POST to the Back End" in {
      stubFor(
        post("/consolidation")
          .willReturn(
            aResponse()
              .withStatus(Status.ACCEPTED)
          )
      )

      val request = DisassociateDUCRRequest("eori", "ucr")
      connector.submit(request).futureValue

      verify(
        postRequestedFor(urlEqualTo("/consolidation"))
          .withRequestBody(equalTo("""{"eori":"eori","ucr":"ucr","consolidationType":"DISASSOCIATE_DUCR"}"""))
      )
    }
  }

  "fetch all Submissions" should {

    "send GET request to the backend" in {

      val expectedSubmission = exampleSubmission()
      val submissionsJson =
        s"""[
           |  {
           |    "uuid":"${expectedSubmission.uuid}",
           |    "eori":"$validEori",
           |    "conversationId":"$conversationId",
           |    "ucrBlocks":[
           |      {
           |        "ucr":"$correctUcr",
           |        "ucrType":"D"
           |      }
           |    ],
           |    "actionType":"Arrival",
           |    "requestTimestamp":"${expectedSubmission.requestTimestamp}"
           |  }
           |]""".stripMargin

      stubFor(
        get("/submissions?eori=eori")
          .willReturn(aResponse().withStatus(OK).withBody(submissionsJson))
      )

      connector.fetchAllSubmissions("eori").futureValue mustBe Seq(expectedSubmission)

      verify(getRequestedFor(urlEqualTo("/submissions?eori=eori")))
    }
  }

  "fetch single Submission" should {

    "send GET request to the backend" in {

      val expectedSubmission = exampleSubmission()
      val submissionJson =
        s"""
           |  {
           |    "uuid":"${expectedSubmission.uuid}",
           |    "eori":"eori",
           |    "conversationId":"$conversationId",
           |    "ucrBlocks":[
           |      {
           |        "ucr":"$correctUcr",
           |        "ucrType":"D"
           |      }
           |    ],
           |    "actionType":"Arrival",
           |    "requestTimestamp":"${expectedSubmission.requestTimestamp}"
           |  }
           |""".stripMargin

      stubFor(
        get(s"/submissions/$conversationId?eori=eori")
          .willReturn(aResponse().withStatus(OK).withBody(submissionJson))
      )

      val response = connector.fetchSingleSubmission(conversationId, "eori").futureValue

      verify(getRequestedFor(urlEqualTo("/submissions/$conversationId?eori=eori")))

      response mustBe Some(expectedSubmission)
    }
  }

  "fetch Notifications" should {

    "send GET request to the backend" in {

      val expectedNotification = exampleNotificationFrontendModel()
      val notificationsJson =
        s"""[
          |   {
          |     "timestampReceived":"${expectedNotification.timestampReceived}",
          |     "conversationId":"$conversationId",
          |     "responseType":"${ControlResponse.value}",
          |     "entries":[
          |       {
          |         "ucrBlock":{
          |           "ucr":"$correctUcr",
          |           "ucrType":"D"
          |         },
          |         "goodsItem":[]
          |       }
          |     ],
          |     "errorCodes":[],
          |     "messageCode":""
          |   }
          |]""".stripMargin

      stubFor(
        get("/notifications/$conversationId?eori=eori")
          .willReturn(aResponse().withStatus(OK).withBody(notificationsJson))
      )

      val response = connector.fetchNotifications(conversationId, "eori").futureValue

      verify(getRequestedFor(urlEqualTo(s"/notifications/$conversationId?eori=eori")))

      response mustBe Seq(expectedNotification)
    }
  }
}
