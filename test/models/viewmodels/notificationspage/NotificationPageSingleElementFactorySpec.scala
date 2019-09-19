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

package models.viewmodels.notificationspage

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}

import models.UcrBlock
import models.notifications.{Entry, EntryStatus, NotificationFrontendModel, ResponseType}
import models.submissions.{ActionType, SubmissionFrontendModel}
import models.viewmodels.decoder._
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{verify, when}
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.{Html, HtmlFormat}
import testdata.CommonTestData._
import testdata.MovementsTestData.exampleSubmissionFrontendModel
import testdata.NotificationTestData.exampleNotificationFrontendModel

class NotificationPageSingleElementFactorySpec extends WordSpec with MustMatchers with MockitoSugar {

  private val testTimestampString = "2019-10-23T12:34+00:00"
  private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault())
  private val testTimestamp = ZonedDateTime.parse(testTimestampString, formatter).toInstant

  private val crcCodeKeyFromDecoder = CrcCode.Success
  private val roeKeyFromDecoder = RoeCode.DocumentaryControl
  private val soeKeyFromDecoder = SoeCode.DeclarationAcceptance
  private val AcknowledgedAndProcessedActionCode = ActionCode.AcknowledgedAndProcessed
  private val MucrNotShutConsolidationErrorCode = ErrorCode.MucrNotShutConsolidation

  private trait Test {
    implicit val messages: Messages = stubMessages()

    val decoderMock: Decoder = mock[Decoder]
    when(decoderMock.crc(any[String])).thenReturn(Some(crcCodeKeyFromDecoder))
    when(decoderMock.roe(any[String])).thenReturn(Some(roeKeyFromDecoder))
    when(decoderMock.soe(any[String])).thenReturn(Some(soeKeyFromDecoder))
    when(decoderMock.actionCode(any[String])).thenReturn(Some(AcknowledgedAndProcessedActionCode))
    when(decoderMock.errorCode(any[String])).thenReturn(Some(MucrNotShutConsolidationErrorCode))

    val emptyNotificationPageElement = NotificationsPageSingleElement("", "", HtmlFormat.empty)

    val controlResponseConverterMock: ControlResponseConverter = mock[ControlResponseConverter]
    when(controlResponseConverterMock.canConvertFrom(any[NotificationFrontendModel])).thenReturn(true)
    when(controlResponseConverterMock.convert(any[NotificationFrontendModel])(any()))
      .thenReturn(emptyNotificationPageElement)

    val movementTotalsResponseContentBuilderMock: MovementTotalsResponseConverter =
      mock[MovementTotalsResponseConverter]
    when(movementTotalsResponseContentBuilderMock.canConvertFrom(any[NotificationFrontendModel])).thenReturn(true)
    when(movementTotalsResponseContentBuilderMock.convert(any[NotificationFrontendModel])(any()))
      .thenReturn(emptyNotificationPageElement)

    val movementResponseConverterMock: MovementResponseConverter = mock[MovementResponseConverter]
    when(movementResponseConverterMock.canConvertFrom(any[NotificationFrontendModel])).thenReturn(true)
    when(movementResponseConverterMock.convert(any[NotificationFrontendModel])(any()))
      .thenReturn(emptyNotificationPageElement)

    val factory = new NotificationPageSingleElementFactory(
      decoderMock,
      controlResponseConverterMock,
      movementTotalsResponseContentBuilderMock,
      movementResponseConverterMock
    )
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
    }
  }

  "NotificationPageSingleElementFactory" when {

    "provided with MovementResponse NotificationFrontendModel" should {

      "call MovementResponseConverter" in new Test {

        val input = exampleNotificationFrontendModel(responseType = ResponseType.MovementResponse)

        factory.build(input)

        verify(movementResponseConverterMock).convert(meq(input))(any[Messages])
      }

      "return NotificationsPageSingleElement returned by MovementResponseConverter" in new Test {

        val exampleNotificationPageElement = NotificationsPageSingleElement(
          title = "TITLE",
          timestampInfo = "TIMESTAMP",
          content = Html("<test>HTML</test>")
        )
        when(movementResponseConverterMock.convert(any[NotificationFrontendModel])(any()))
          .thenReturn(exampleNotificationPageElement)

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementResponse,
          timestampReceived = testTimestamp,
          crcCode = Some(crcCodeKeyFromDecoder.code)
        )

        val result = factory.build(input)

        result mustBe exampleNotificationPageElement
      }
    }

    "provided with MovementTotalsResponse NotificationFrontendModel" should {

      "call MovementTotalsResponseConverter" in new Test {

        val input = exampleNotificationFrontendModel(responseType = ResponseType.MovementTotalsResponse)

        factory.build(input)

        verify(movementTotalsResponseContentBuilderMock).convert(meq(input))(any[Messages])
      }

      "return NotificationsPageSingleElement returned by MovementTotalsResponseConverter" in new Test {

        val exampleNotificationPageElement = NotificationsPageSingleElement(
          title = "TITLE",
          timestampInfo = "TIMESTAMP",
          content = Html("<test>HTML</test>")
        )
        when(movementTotalsResponseContentBuilderMock.convert(any[NotificationFrontendModel])(any()))
          .thenReturn(exampleNotificationPageElement)

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementTotalsResponse,
          timestampReceived = testTimestamp,
          crcCode = Some(crcCodeKeyFromDecoder.code),
          entries = Seq(
            Entry(
              entryStatus = Some(EntryStatus(roe = Some(roeKeyFromDecoder.code), soe = Some(soeKeyFromDecoder.code)))
            )
          )
        )

        val result = factory.build(input)

        result mustBe exampleNotificationPageElement
      }
    }

    "provided with ControlResponse NotificationFrontendModel" should {

      "call ControlResponseConverter" in new Test {

        val input = exampleNotificationFrontendModel(responseType = ResponseType.ControlResponse)

        factory.build(input)

        verify(controlResponseConverterMock).convert(meq(input))(any[Messages])
      }

      "return NotificationsPageSingleElement returned by ControlResponseConverter" in new Test {

        val exampleNotificationPageElement = NotificationsPageSingleElement(
          title = "TITLE",
          timestampInfo = "TIMESTAMP",
          content = Html("<test>HTML</test>")
        )
        when(controlResponseConverterMock.convert(any[NotificationFrontendModel])(any()))
          .thenReturn(exampleNotificationPageElement)

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.ControlResponse,
          timestampReceived = testTimestamp,
          actionCode = Some(AcknowledgedAndProcessedActionCode.code)
        )

        val result = factory.build(input)

        result mustBe exampleNotificationPageElement
      }
    }
  }

  private def assertEquality(actual: NotificationsPageSingleElement, expected: NotificationsPageSingleElement): Unit = {
    actual.title must equal(expected.title)
    actual.timestampInfo must equal(expected.timestampInfo)
    actual.content must equal(expected.content)
  }

}
