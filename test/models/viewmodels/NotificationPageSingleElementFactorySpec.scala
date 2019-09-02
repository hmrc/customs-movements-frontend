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
import models.viewmodels.decoder.Decoder
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito
import org.mockito.Mockito.{verify, when}
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.Html

class NotificationPageSingleElementFactorySpec extends WordSpec with MustMatchers with MockitoSugar {

  private val testTimestampString = "2019-10-23T12:34+00:00"
  private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault())
  private val testTimestamp = ZonedDateTime.parse(testTimestampString, formatter).toInstant

  private trait Test {
    val decoderMock: Decoder = mock[Decoder]
    implicit val messages: Messages = Mockito.spy(stubMessages())
    val factory = new NotificationPageSingleElementFactory(decoderMock)

    val crcCodeKeyFromDecoder = "CRC code mapping"
    val roeKeyFromDecoder = "ROE code mapping"
    val soeKeyFromDecoder = "SOE code mapping"
    val actionCodeKeyFromDecoder = "Action code mapping"
    when(decoderMock.crc(any())).thenReturn(crcCodeKeyFromDecoder)
    when(decoderMock.roe(any())).thenReturn(roeKeyFromDecoder)
    when(decoderMock.soe(any())).thenReturn(soeKeyFromDecoder)
    when(decoderMock.actionCode(any())).thenReturn(actionCodeKeyFromDecoder)
  }

  "NotificationPageSingleElementFactory" should {

    "return NotificationsPageSingleElement with values returned by Messages" when {

      "provided with Arrival SubmissionFrontendModel" in new Test {

        val input: SubmissionFrontendModel =
          exampleSubmissionFrontendModel(actionType = ActionType.Arrival, requestTimestamp = testTimestamp)
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.Arrival"),
          timestampInfo = messages("notifications.elem.timestampInfo.request", "23 Oct 2019 at 12:34"),
          content = Html(
            s"<p>${messages("notifications.elem.content.Arrival")}</p>" +
              s"<p>${messages("notifications.elem.content.footer")}</p>"
          )
        )

        val result = factory.build(input)

        assertEquality(result, expectedResult)
      }

      "provided with Departure SubmissionFrontendModel" in new Test {

        val input: SubmissionFrontendModel =
          exampleSubmissionFrontendModel(actionType = ActionType.Departure, requestTimestamp = testTimestamp)
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.Departure"),
          timestampInfo = messages("notifications.elem.timestampInfo.request", "23 Oct 2019 at 12:34"),
          content = Html(
            s"<p>${messages("notifications.elem.content.Departure")}</p>" +
              s"<p>${messages("notifications.elem.content.footer")}</p>"
          )
        )

        val result = factory.build(input)

        assertEquality(result, expectedResult)
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
          timestampInfo = messages("notifications.elem.timestampInfo.request", "23 Oct 2019 at 12:34"),
          content = Html(
            s"<p>${messages("notifications.elem.content.DucrAssociation")}</p>" +
              s"<p>$correctUcr_2</p>" +
              s"<p>$correctUcr_3</p>" +
              s"<p>${messages("notifications.elem.content.footer")}</p>"
          )
        )

        val result = factory.build(input)

        assertEquality(result, expectedResult)
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
          timestampInfo = messages("notifications.elem.timestampInfo.request", "23 Oct 2019 at 12:34"),
          content = Html(
            s"<p>${messages("notifications.elem.content.DucrDisassociation")}</p>" +
              s"<p>${messages("notifications.elem.content.footer")}</p>"
          )
        )

        val result = factory.build(input)

        assertEquality(result, expectedResult)
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
          timestampInfo = messages("notifications.elem.timestampInfo.request", "23 Oct 2019 at 12:34"),
          content = Html(
            s"<p>${messages("notifications.elem.content.ShutMucr")}</p>" +
              s"<p>${messages("notifications.elem.content.footer")}</p>"
          )
        )

        val result = factory.build(input)

        assertEquality(result, expectedResult)
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
            timestampInfo = messages("notifications.elem.timestampInfo.response", "23 Oct 2019 at 12:34"),
            content = Html(s"<p>${messages(actionCodeKeyFromDecoder)}</p>")
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
            timestampInfo = messages("notifications.elem.timestampInfo.response", "23 Oct 2019 at 12:34"),
            content = Html(s"<p>${messages(actionCodeKeyFromDecoder)}</p>")
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
            timestampInfo = messages("notifications.elem.timestampInfo.response", "23 Oct 2019 at 12:34"),
            content = Html(s"<p>${messages(actionCodeKeyFromDecoder)}</p>")
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

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementResponse,
          timestampReceived = testTimestamp,
          crcCode = Some("000")
        )

        factory.build(input)

        verifyMessages("notifications.elem.title.inventoryLinkingMovementResponse")
        verifyMessages("notifications.elem.timestampInfo.response", "23 Oct 2019 at 12:34")
        verifyMessages(crcCodeKeyFromDecoder)
        verifyMessages("notifications.elem.content.inventoryLinkingMovementResponse.crc")
      }

      "return NotificationsPageSingleElement with values returned by Messages" in new Test {

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementResponse,
          timestampReceived = testTimestamp,
          crcCode = Some("000")
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingMovementResponse"),
          timestampInfo = messages("notifications.elem.timestampInfo.response", "23 Oct 2019 at 12:34"),
          content = Html(
            s"<p>${messages("notifications.elem.content.inventoryLinkingMovementResponse.crc")} $crcCodeKeyFromDecoder</p>"
          )
        )

        val result: NotificationsPageSingleElement = factory.build(input)

        assertEquality(result, expectedResult)
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

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementTotalsResponse,
          timestampReceived = testTimestamp,
          crcCode = Some("000"),
          masterRoe = Some("6"),
          masterSoe = Some("1")
        )

        factory.build(input)

        verifyMessages("notifications.elem.title.inventoryLinkingMovementTotalsResponse")
        verifyMessages("notifications.elem.timestampInfo.response", "23 Oct 2019 at 12:34")

        verifyMessages(crcCodeKeyFromDecoder)
        verifyMessages(roeKeyFromDecoder)
        verifyMessages(soeKeyFromDecoder)

        verifyMessages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.crc")
        verifyMessages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.roe")
        verifyMessages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.soe")
      }

      "return NotificationsPageSingleElement with values returned by Messages" in new Test {

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementTotalsResponse,
          timestampReceived = testTimestamp,
          crcCode = Some("000"),
          masterRoe = Some("6"),
          masterSoe = Some("1")
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingMovementTotalsResponse"),
          timestampInfo = messages("notifications.elem.timestampInfo.response", "23 Oct 2019 at 12:34"),
          content = Html(
            s"<p>${messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.crc")} $crcCodeKeyFromDecoder</p>" +
              s"<p>${messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.roe")} $roeKeyFromDecoder</p>" +
              s"<p>${messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.soe")} $soeKeyFromDecoder</p>"
          )
        )

        val result: NotificationsPageSingleElement = factory.build(input)

        assertEquality(result, expectedResult)
      }

      "return NotificationsPageSingleElement with values returned by Messages for incomplete input" in new Test {

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementTotalsResponse,
          timestampReceived = testTimestamp,
          crcCode = Some("000"),
          masterSoe = Some("1")
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingMovementTotalsResponse"),
          timestampInfo = messages("notifications.elem.timestampInfo.response", "23 Oct 2019 at 12:34"),
          content = Html(
            s"<p>${messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.crc")} $crcCodeKeyFromDecoder</p>" +
              s"<p>${messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.soe")} $soeKeyFromDecoder</p>"
          )
        )

        val result: NotificationsPageSingleElement = factory.build(input)

        assertEquality(result, expectedResult)
      }
    }
  }

  private def assertEquality(actual: NotificationsPageSingleElement, expected: NotificationsPageSingleElement): Unit = {
    actual.title must equal(expected.title)
    actual.timestampInfo must equal(expected.timestampInfo)
    actual.content must equal(expected.content)
  }

  private def verifyMessages(key: String, args: String*)(implicit messages: Messages): Unit =
    verify(messages).apply(meq(key), meq(args))

}
