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

package models.viewmodels

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}

import base.testdata.CommonTestData._
import base.testdata.MovementsTestData.exampleSubmissionFrontendModel
import base.testdata.NotificationTestData.exampleNotificationFrontendModel
import models.UcrBlock
import models.notifications.ResponseType
import models.submissions.{ActionType, SubmissionFrontendModel}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.Html

import scala.collection.mutable

class NotificationPageSingleElementFactorySpec extends WordSpec with MustMatchers with MockitoSugar {

  private val testTimestampString = "2019-10-23T12:34+00:00"
  private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault())
  private val testTimestamp = ZonedDateTime.parse(testTimestampString, formatter).toInstant

  private trait Test {
    val decoderMock: Decoder = mock[Decoder]
    implicit val messages: Messages = Mockito.spy(stubMessages())
    val factory = new NotificationPageSingleElementFactory(decoderMock)
  }

  "NotificationPageSingleElementFactory" should {

    "return NotificationsPageSingleElement with values returned by Messages" when {

      "provided with Arrival SubmissionFrontendModel" in new Test {

        val input: SubmissionFrontendModel =
          exampleSubmissionFrontendModel(actionType = ActionType.Arrival, requestTimestamp = testTimestamp)
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.Arrival"),
          timestampInfo = messages("notifications.elem.timestampInfo.request", "23 Oct 2019", "12:34"),
          content = Html(
            s"<p>${messages("notifications.elem.content.Arrival")}</p>" +
              s"<p>${messages("notifications.elem.content.footer")}</p>"
          )
        )

        val result = factory.build(input)

        result.title must equal(expectedResult.title)
        result.timestampInfo must equal(expectedResult.timestampInfo)
        result.content must equal(expectedResult.content)
      }

      "provided with Departure SubmissionFrontendModel" in new Test {

        val input: SubmissionFrontendModel =
          exampleSubmissionFrontendModel(actionType = ActionType.Departure, requestTimestamp = testTimestamp)
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.Departure"),
          timestampInfo = messages("notifications.elem.timestampInfo.request", "23 Oct 2019", "12:34"),
          content = Html(
            s"<p>${messages("notifications.elem.content.Departure")}</p>" +
              s"<p>${messages("notifications.elem.content.footer")}</p>"
          )
        )

        val result = factory.build(input)

        result.title must equal(expectedResult.title)
        result.timestampInfo must equal(expectedResult.timestampInfo)
        result.content must equal(expectedResult.content)
      }

      "provided with DucrAssociation SubmissionFrontendModel" in new Test {

        val input: SubmissionFrontendModel = SubmissionFrontendModel(
          eori = validEori,
          conversationId = conversationId,
          actionType = ActionType.DucrAssociation,
          requestTimestamp = testTimestamp,
          ucrBlocks = Seq(
            UcrBlock(ucr = correctUcr, ucrType = "M"),
            UcrBlock(ucr = correctUcr_2, ucrType = "D"),
            UcrBlock(ucr = correctUcr_3, ucrType = "D")
          )
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.DucrAssociation"),
          timestampInfo = messages("notifications.elem.timestampInfo.request", "23 Oct 2019", "12:34"),
          content = Html(
            s"<p>${messages("notifications.elem.content.DucrAssociation")}</p>" +
              s"<p>$correctUcr_2</p>" +
              s"<p>$correctUcr_3</p>" +
              s"<p>${messages("notifications.elem.content.footer")}</p>"
          )
        )

        val result = factory.build(input)

        result.title must equal(expectedResult.title)
        result.timestampInfo must equal(expectedResult.timestampInfo)
        result.content must equal(expectedResult.content)
      }

      "provided with DucrDisassociation SubmissionFrontendModel" in new Test {

        val input: SubmissionFrontendModel = exampleSubmissionFrontendModel(
          actionType = ActionType.DucrDisassociation,
          requestTimestamp = testTimestamp,
          ucr = correctUcr,
          ucrType = "D"
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.DucrDisassociation"),
          timestampInfo = messages("notifications.elem.timestampInfo.request", "23 Oct 2019", "12:34"),
          content = Html(
            s"<p>${messages("notifications.elem.content.DucrDisassociation")}</p>" +
              s"<p>${messages("notifications.elem.content.footer")}</p>"
          )
        )

        val result = factory.build(input)

        result.title must equal(expectedResult.title)
        result.timestampInfo must equal(expectedResult.timestampInfo)
        result.content must equal(expectedResult.content)
      }

      "provided with ShutMucr SubmissionFrontendModel" in new Test {

        val input: SubmissionFrontendModel = exampleSubmissionFrontendModel(
          actionType = ActionType.ShutMucr,
          requestTimestamp = testTimestamp,
          ucr = correctUcr,
          ucrType = "M"
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.ShutMucr"),
          timestampInfo = messages("notifications.elem.timestampInfo.request", "23 Oct 2019", "12:34"),
          content = Html(
            s"<p>${messages("notifications.elem.content.ShutMucr")}</p>" +
              s"<p>${messages("notifications.elem.content.footer")}</p>"
          )
        )

        val result = factory.build(input)

        result.title must equal(expectedResult.title)
        result.timestampInfo must equal(expectedResult.timestampInfo)
        result.content must equal(expectedResult.content)
      }

      "provided with ControlResponse NotificationFrontendModel" which {

        "contains action code = 1" in new Test {

          val input = exampleNotificationFrontendModel(
            responseType = ResponseType.ControlResponse,
            timestampReceived = testTimestamp,
            actionCode = Some("1")
          )
          val expectedResult = NotificationsPageSingleElement(
            title = messages("notifications.elem.title.inventoryLinkingControlResponse"),
            timestampInfo = messages("notifications.elem.timestampInfo.response", "23 Oct 2019", "12:34"),
            content = Html(s"<p>${messages("notifications.elem.content.inventoryLinkingControlResponse.1")}</p>")
          )

          val result: NotificationsPageSingleElement = factory.build(input)

          assertEquality(result, expectedResult)
        }

        "contains action code = 2" in new Test {

          val input = exampleNotificationFrontendModel(
            responseType = ResponseType.ControlResponse,
            timestampReceived = testTimestamp,
            actionCode = Some("2")
          )
          val expectedResult = NotificationsPageSingleElement(
            title = messages("notifications.elem.title.inventoryLinkingControlResponse"),
            timestampInfo = messages("notifications.elem.timestampInfo.response", "23 Oct 2019", "12:34"),
            content = Html(s"<p>${messages("notifications.elem.content.inventoryLinkingControlResponse.2")}</p>")
          )

          val result: NotificationsPageSingleElement = factory.build(input)

          assertEquality(result, expectedResult)
        }

        "contains action code = 3" in new Test {

          val input = exampleNotificationFrontendModel(
            responseType = ResponseType.ControlResponse,
            timestampReceived = testTimestamp,
            actionCode = Some("3")
          )
          val expectedResult = NotificationsPageSingleElement(
            title = messages("notifications.elem.title.inventoryLinkingControlResponse"),
            timestampInfo = messages("notifications.elem.timestampInfo.response", "23 Oct 2019", "12:34"),
            content = Html(s"<p>${messages("notifications.elem.content.inventoryLinkingControlResponse.3")}</p>")
          )

          val result: NotificationsPageSingleElement = factory.build(input)

          assertEquality(result, expectedResult)
        }
      }
    }

  }

  "NotificationPageSingleElementFactory" when {

    "provided with MovementResponse NotificationFrontendModel" should {

      "call Decoder" in new Test {

        val crcCode = "000"
        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementResponse,
          timestampReceived = testTimestamp,
          crcCode = Some(crcCode)
        )

        factory.build(input)

        verify(decoderMock).crc(meq(crcCode))
      }

      "call Messages passing correct keys and arguments" in new Test {

        when(decoderMock.crc(any())).thenReturn("CRC code mapping")
        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementResponse,
          timestampReceived = testTimestamp,
          crcCode = Some("000")
        )

        factory.build(input)

        verify(messages)
          .apply(meq("notifications.elem.title.inventoryLinkingMovementResponse"), meq(mutable.WrappedArray.empty))
        verify(messages).apply(
          meq("notifications.elem.timestampInfo.response"),
          meq(mutable.WrappedArray.make(Array("23 Oct 2019", "12:34")))
        )
        verify(messages).apply(
          meq("notifications.elem.content.inventoryLinkingMovementResponse.crc"),
          meq(mutable.WrappedArray.make(Array("CRC code mapping")))
        )
      }

      "return NotificationsPageSingleElement with values returned by Messages" in new Test {

        val expectedTitle = "notifications.elem.title.inventoryLinkingMovementResponse"
        val expectedTimestamp = "notifications.elem.timestampInfo.response"
        val expectedContent = Html("<p>notifications.elem.content.inventoryLinkingMovementResponse.crc</p>")

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementResponse,
          timestampReceived = testTimestamp,
          crcCode = Some("000")
        )

        val result: NotificationsPageSingleElement = factory.build(input)

        result.title must equal(expectedTitle)
        result.timestampInfo must equal(expectedTimestamp)
        result.content must equal(expectedContent)
      }
    }

    "provided with MovementTotalsResponse NotificationFrontendModel" should {

      "call Decoder" in new Test {

        val crcCode = "000"
        val masterRoe = "6"
        val masterSoe = "1"
        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementTotalsResponse,
          timestampReceived = testTimestamp,
          crcCode = Some(crcCode),
          masterRoe = Some(masterRoe),
          masterSoe = Some(masterSoe)
        )

        factory.build(input)

        verify(decoderMock).crc(meq(crcCode))
        verify(decoderMock).roe(meq(masterRoe))
        verify(decoderMock).soe(meq(masterSoe))
      }

      "call Messages passing correct keys and arguments" in new Test {

        when(decoderMock.crc(any())).thenReturn("CRC code mapping")
        when(decoderMock.roe(any())).thenReturn("ROE mapping")
        when(decoderMock.soe(any())).thenReturn("SOE mapping")
        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementTotalsResponse,
          timestampReceived = testTimestamp,
          crcCode = Some("000"),
          masterRoe = Some("6"),
          masterSoe = Some("1")
        )

        factory.build(input)

        verify(messages)
          .apply(
            meq("notifications.elem.title.inventoryLinkingMovementTotalsResponse"),
            meq(mutable.WrappedArray.empty)
          )
        verify(messages).apply(
          meq("notifications.elem.timestampInfo.response"),
          meq(mutable.WrappedArray.make(Array("23 Oct 2019", "12:34")))
        )
        verify(messages).apply(
          meq("notifications.elem.content.inventoryLinkingMovementTotalsResponse.crc"),
          meq(mutable.WrappedArray.make(Array("CRC code mapping")))
        )
        verify(messages).apply(
          meq("notifications.elem.content.inventoryLinkingMovementTotalsResponse.roe"),
          meq(mutable.WrappedArray.make(Array("ROE mapping")))
        )
        verify(messages).apply(
          meq("notifications.elem.content.inventoryLinkingMovementTotalsResponse.soe"),
          meq(mutable.WrappedArray.make(Array("SOE mapping")))
        )
      }

      "return NotificationsPageSingleElement with values returned by Messages" in new Test {

        val expectedTitle = "notifications.elem.title.inventoryLinkingMovementTotalsResponse"
        val expectedTimestamp = "notifications.elem.timestampInfo.response"
        val expectedContent = Html(
          "<p>notifications.elem.content.inventoryLinkingMovementTotalsResponse.crc</p>" +
            "<p>notifications.elem.content.inventoryLinkingMovementTotalsResponse.roe</p>" +
            "<p>notifications.elem.content.inventoryLinkingMovementTotalsResponse.soe</p>"
        )

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementTotalsResponse,
          timestampReceived = testTimestamp,
          crcCode = Some("000"),
          masterRoe = Some("6"),
          masterSoe = Some("1")
        )

        val result: NotificationsPageSingleElement = factory.build(input)

        result.title must equal(expectedTitle)
        result.timestampInfo must equal(expectedTimestamp)
        result.content must equal(expectedContent)
      }

      "return NotificationsPageSingleElement with values returned by Messages for incomplete input" in new Test {

        val expectedTitle = "notifications.elem.title.inventoryLinkingMovementTotalsResponse"
        val expectedTimestamp = "notifications.elem.timestampInfo.response"
        val expectedContent = Html(
          "<p>notifications.elem.content.inventoryLinkingMovementTotalsResponse.crc</p>" +
            "<p>notifications.elem.content.inventoryLinkingMovementTotalsResponse.soe</p>"
        )

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementTotalsResponse,
          timestampReceived = testTimestamp,
          crcCode = Some("000"),
          masterSoe = Some("1")
        )

        val result: NotificationsPageSingleElement = factory.build(input)

        result.title must equal(expectedTitle)
        result.timestampInfo must equal(expectedTimestamp)
        result.content must equal(expectedContent)
      }
    }
  }

  private def assertEquality(actual: NotificationsPageSingleElement, expected: NotificationsPageSingleElement): Unit = {
    actual.title must equal(expected.title)
    actual.timestampInfo must equal(expected.timestampInfo)
    actual.content must equal(expected.content)
  }

}
