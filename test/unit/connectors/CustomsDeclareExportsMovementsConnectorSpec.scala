/*
 * Copyright 2023 HM Revenue & Customs
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

import com.github.tomakehurst.wiremock.client.WireMock._
import config.AppConfig
import connectors.exception.MovementsConnectorException
import connectors.exchanges.ActionType.MovementType
import connectors.exchanges.{DisassociateDUCRRequest, MovementDetailsRequest, MovementRequest}
import forms.ConsignmentReferences
import models.UcrBlock
import models.notifications.ResponseType.ControlResponse
import models.notifications.queries.DucrInfo
import models.notifications.queries.IleQueryResponseExchangeData.SuccessfulResponseExchangeData
import models.submissions.Submission
import org.mockito.BDDMockito.given
import org.mockito.MockitoSugar.mock
import play.api.http.Status
import play.api.libs.json.{Format, Json}
import play.api.test.Helpers._
import testdata.CommonTestData._
import testdata.MovementsTestData.exampleSubmission
import testdata.NotificationTestData.exampleNotificationFrontendModel
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

class CustomsDeclareExportsMovementsConnectorSpec extends ConnectorSpec {

  implicit private val formatInstant: Format[Instant] = MongoJavatimeFormats.instantFormat
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
            equalToJson(
              """{"eori":"eori","choice":"Arrival","consignmentReference":{"reference":"ref","referenceValue":"value"},"movementDetails":{"dateTime":"datetime"}}"""
            )
          )
      )
    }

    "Handle failure from back end" in {
      stubFor(
        post("/movements")
          .willReturn(
            aResponse()
              .withStatus(Status.INTERNAL_SERVER_ERROR)
          )
      )
      val request =
        MovementRequest("eori", MovementType.Arrival, ConsignmentReferences("ref", "value"), MovementDetailsRequest("datetime"))

      intercept[MovementsConnectorException] {
        await(connector.submit(request))
      }
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
          .withRequestBody(equalToJson("""{"ucr":"ucr","consolidationType":"DucrDisassociation","eori":"eori"}"""))
      )
    }

    "Handle failure from back end" in {
      stubFor(
        post("/consolidation")
          .willReturn(
            aResponse()
              .withStatus(Status.INTERNAL_SERVER_ERROR)
          )
      )

      val request = DisassociateDUCRRequest("eori", "ucr")

      intercept[MovementsConnectorException] {
        await(connector.submit(request))
      }
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
           |    "requestTimestamp": ${Json.toJson(expectedSubmission.requestTimestamp)}
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
      val submission = Submission(
        eori = "eori",
        conversationId = conversationId,
        ucrBlocks = Seq(UcrBlock(ucr = "ucr", ucrType = "type")),
        actionType = MovementType.Arrival,
        requestTimestamp = Instant.EPOCH
      )
      val submissionJson =
        s"""
           |  {
           |    "uuid":"${submission.uuid}",
           |    "eori":"eori",
           |    "conversationId":"$conversationId",
           |    "ucrBlocks":[
           |      {
           |        "ucr":"ucr",
           |        "ucrType":"type"
           |      }
           |    ],
           |    "actionType":"Arrival",
           |    "requestTimestamp":${Json.toJson(submission.requestTimestamp)}
           |  }
           |""".stripMargin

      stubFor(
        get(s"/submissions/$conversationId?eori=eori")
          .willReturn(aResponse().withStatus(OK).withBody(submissionJson))
      )

      val response = connector.fetchSingleSubmission(conversationId, "eori").futureValue

      verify(getRequestedFor(urlEqualTo(s"/submissions/$conversationId?eori=eori")))

      response mustBe Some(submission)
    }
  }

  "fetch Notifications" should {
    "send GET request to the backend" in {
      val expectedNotification = exampleNotificationFrontendModel()
      val notificationsJson =
        s"""[
          |   {
          |     "timestampReceived":${Json.toJson(expectedNotification.timestampReceived)},
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
        get(s"/notifications/$conversationId?eori=eori")
          .willReturn(aResponse().withStatus(OK).withBody(notificationsJson))
      )

      val response = connector.fetchNotifications(conversationId, "eori").futureValue

      verify(getRequestedFor(urlEqualTo(s"/notifications/$conversationId?eori=eori")))

      response mustBe Seq(expectedNotification)
    }
  }

  "fetch Query Notifications" when {
    val expectedDucrInfo = DucrInfo(ucr = correctUcr, declarationId = "declarationId")
    val expectedNotification = SuccessfulResponseExchangeData(queriedDucr = Some(expectedDucrInfo))
    val notificationJson =
      s"""
         |  {
         |    "queriedDucr": {
         |      "ucr":"$correctUcr",
         |      "declarationId":"declarationId",
         |      "movements":[],
         |      "goodsItem":[]
         |    },
         |    "childDucrs":[],
         |    "childMucrs":[]
         |  }
         |""".stripMargin

    "everything works correctly" should {

      "send GET request to the backend" in {
        stubFor(
          get(s"/consignment-query/$conversationId?eori=eori")
            .willReturn(aResponse().withStatus(OK).withBody(notificationJson))
        )

        connector.fetchQueryNotifications(conversationId, "eori").futureValue

        val expectedUrl = s"/consignment-query/$conversationId?eori=eori"
        verify(getRequestedFor(urlEqualTo(expectedUrl)))
      }

      "return HttpResponse with Ok (200) status and Notification in body" in {
        stubFor(
          get(s"/consignment-query/$conversationId?eori=eori")
            .willReturn(aResponse().withStatus(OK).withBody(notificationJson))
        )

        val response = connector.fetchQueryNotifications(conversationId, "eori").futureValue

        response.status mustBe OK
        Json.parse(response.body).as[SuccessfulResponseExchangeData] mustBe expectedNotification
      }
    }

    "received FailedDependency (424) response" should {
      "return HttpResponse with FailedDependency status" in {
        stubFor(
          get(s"/consignment-query/$conversationId?eori=eori")
            .willReturn(aResponse().withStatus(FAILED_DEPENDENCY))
        )

        val response = connector.fetchQueryNotifications(conversationId, "eori").futureValue

        response.status mustBe FAILED_DEPENDENCY
      }
    }

    "received InternalServerError (500) response" should {
      "return Internal server error" in {
        stubFor(
          get(s"/consignment-query/$conversationId?eori=eori")
            .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR))
        )

        val response = connector.fetchQueryNotifications(conversationId, "eori").futureValue

        response.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
