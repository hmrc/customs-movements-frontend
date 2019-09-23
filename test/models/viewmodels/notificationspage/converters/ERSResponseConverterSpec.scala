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

package models.viewmodels.notificationspage.converters

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId, ZonedDateTime}

import base.BaseSpec
import models.UcrBlock
import models.notifications.ResponseType._
import models.notifications.{Entry, EntryStatus, ResponseType}
import models.viewmodels.decoder.{Decoder, ICSCode, ROECode, SOECode}
import models.viewmodels.notificationspage.MovementTotalsResponseType.{EMR, ERS}
import models.viewmodels.notificationspage.NotificationsPageSingleElement
import modules.DateTimeFormatterModule.NotificationsPageFormatter
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.Html
import testdata.CommonTestData._
import testdata.NotificationTestData.exampleNotificationFrontendModel

class ERSResponseConverterSpec extends BaseSpec with MockitoSugar {

  import ERSResponseConverterSpec._

  private trait Test {
    implicit val messages: Messages = stubMessages()

    val decoderMock: Decoder = mock[Decoder]
    when(decoderMock.ics(any[String])).thenReturn(Some(icsKeyFromDecoder))
    when(decoderMock.roe(any[String])).thenReturn(Some(roeKeyFromDecoder))
    when(decoderMock.ducrSoe(any[String])).thenReturn(Some(soeKeyFromDecoder))

    val contentBuilder = new ERSResponseConverter(decoderMock, NotificationsPageFormatter)
  }

  "ERSResponseConverter on canConvertFrom" should {

    "return false" when {

      "provided EMR response" in new Test {

        val input =
          exampleNotificationFrontendModel(responseType = MovementTotalsResponse, messageCode = EMR.code)

        contentBuilder.canConvertFrom(input) mustBe false
      }

      "provided with MovementTotalsResponse but empty messageCode" in new Test {

        val input = exampleNotificationFrontendModel(responseType = MovementTotalsResponse)

        contentBuilder.canConvertFrom(input) mustBe false
      }

      "provided with different response type" in new Test {

        val input = exampleNotificationFrontendModel(responseType = ControlResponse, messageCode = "EDL")

        contentBuilder.canConvertFrom(input) mustBe false
      }
    }

    "return true" when {
      "provided with ERS response" in new Test {

        val input =
          exampleNotificationFrontendModel(responseType = MovementTotalsResponse, messageCode = ERS.code)

        contentBuilder.canConvertFrom(input) mustBe true
      }
    }
  }

  "ERSResponseConverter on convert" when {

    "provided with response of type different than ERS" should {
      "throw IllegalArgumentException" in new Test {

        val input = exampleNotificationFrontendModel(responseType = ResponseType.ControlResponse, messageCode = "EDL")

        intercept[IllegalArgumentException] { contentBuilder.convert(input) }
      }
    }

    "provided with ERS MovementTotalsResponse with all codes" should {

      "call Decoder" in new Test {

        val input = ersResponseAllCodes

        contentBuilder.convert(input)

        verify(decoderMock).ics(meq(icsKeyFromDecoder.code))
        verify(decoderMock).roe(meq(roeKeyFromDecoder.code))
        verify(decoderMock).ducrSoe(meq(soeKeyFromDecoder.code))
        verify(decoderMock, times(0)).crc(any())
        verify(decoderMock, times(0)).mucrSoe(any())
      }

      "return NotificationsPageSingleElement with values returned by Messages" in new Test {

        val input = ersResponseAllCodes
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingMovementTotalsResponse"),
          timestampInfo = messages("notifications.elem.timestampInfo.response", "23 Oct 2019 at 12:34"),
          content = Html(
            s"<p>${messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.roe")} ${messages(roeKeyFromDecoder.contentKey)}</p>" +
              s"<p>${messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.soe")} ${messages(soeKeyFromDecoder.contentKey)}</p>" +
              s"<p>${messages(icsKeyFromDecoder.contentKey)}</p>"
          )
        )

        contentBuilder.convert(input) mustBe expectedResult
      }
    }

    "provided with ERS MovementTotalsResponse with empty codes" should {

      "call Decoder only for existing codes" in new Test {

        val input = ersResponseMissingCodes

        contentBuilder.convert(input)

        verify(decoderMock, times(0)).ics(any())
        verify(decoderMock, times(0)).roe(any())
        verify(decoderMock).ducrSoe(meq(soeKeyFromDecoder.code))
      }

      "return NotificationsPageSingleElement without content for missing codes" in new Test {

        val input = ersResponseMissingCodes
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingMovementTotalsResponse"),
          timestampInfo = messages("notifications.elem.timestampInfo.response", "23 Oct 2019 at 12:34"),
          content = Html(
            s"<p>${messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.soe")} ${messages(soeKeyFromDecoder.contentKey)}</p>"
          )
        )

        contentBuilder.convert(input) mustBe expectedResult
      }
    }

    "provided with ERS MovementTotalsResponse with unknown codes" should {

      "call Decoder for all codes" in new Test {

        val input = ersResponseUnknownCodes

        contentBuilder.convert(input)

        verify(decoderMock).ics(meq(UnknownIcsCode))
        verify(decoderMock).roe(meq(UnknownRoeCode))
        verify(decoderMock).ducrSoe(meq(UnknownSoeCode))
      }

      "return NotificationsPageSingleElement without content for unknown codes" in new Test {

        when(decoderMock.ics(meq(UnknownIcsCode))).thenReturn(None)
        when(decoderMock.roe(meq(UnknownRoeCode))).thenReturn(None)
        when(decoderMock.ducrSoe(meq(UnknownSoeCode))).thenReturn(None)

        val input = ersResponseUnknownCodes
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingMovementTotalsResponse"),
          timestampInfo = messages("notifications.elem.timestampInfo.response", "23 Oct 2019 at 12:34"),
          content = Html("")
        )

        contentBuilder.convert(input) mustBe expectedResult
      }
    }
  }

}

object ERSResponseConverterSpec {

  private val testTimestampString = "2019-10-23T12:34+00:00"
  private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault())
  val testTimestamp: Instant = ZonedDateTime.parse(testTimestampString, formatter).toInstant

  val icsKeyFromDecoder = ICSCode.InvalidationByCustoms
  val roeKeyFromDecoder = ROECode.DocumentaryControl
  val soeKeyFromDecoder = SOECode.DeclarationAcceptance

  val ersResponseAllCodes = exampleNotificationFrontendModel(
    responseType = ResponseType.MovementTotalsResponse,
    messageCode = ERS.code,
    timestampReceived = testTimestamp,
    entries = Seq(
      Entry(
        ucrBlock = Some(UcrBlock(ucr = correctUcr, ucrType = "D")),
        entryStatus = Some(
          EntryStatus(
            ics = Some(icsKeyFromDecoder.code),
            roe = Some(roeKeyFromDecoder.code),
            soe = Some(soeKeyFromDecoder.code)
          )
        )
      )
    )
  )

  val ersResponseMissingCodes = exampleNotificationFrontendModel(
    responseType = ResponseType.MovementTotalsResponse,
    messageCode = ERS.code,
    timestampReceived = testTimestamp,
    entries = Seq(
      Entry(
        ucrBlock = Some(UcrBlock(ucr = correctUcr, ucrType = "D")),
        entryStatus = Some(EntryStatus(soe = Some(soeKeyFromDecoder.code)))
      )
    )
  )

  val UnknownIcsCode = "123"
  val UnknownRoeCode = "456"
  val UnknownSoeCode = "7890"

  val ersResponseUnknownCodes = exampleNotificationFrontendModel(
    responseType = ResponseType.MovementTotalsResponse,
    messageCode = ERS.code,
    timestampReceived = testTimestamp,
    entries = Seq(
      Entry(
        ucrBlock = Some(UcrBlock(ucr = correctUcr, ucrType = "D")),
        entryStatus =
          Some(EntryStatus(ics = Some(UnknownIcsCode), roe = Some(UnknownRoeCode), soe = Some(UnknownSoeCode)))
      )
    )
  )
}
