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
import java.time.{ZoneId, ZonedDateTime}

import base.BaseSpec
import models.UcrBlock
import models.notifications.ResponseType.{ControlResponse, MovementTotalsResponse}
import models.notifications.{Entry, EntryStatus, ResponseType}
import models.viewmodels.decoder.{CRCCode, Decoder, ROECode, SOECode}
import models.viewmodels.notificationspage.MovementTotalsResponseType.{EMR, ERS}
import models.viewmodels.notificationspage.NotificationsPageSingleElement
import modules.DateTimeFormatterModule.NotificationsPageFormatter
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.Html
import testdata.CommonTestData.correctUcr
import testdata.NotificationTestData.exampleNotificationFrontendModel

class EMRResponseConverterSpec extends BaseSpec with MockitoSugar {

  import EMRResponseConverterSpec._

  private trait Test {
    implicit val messages: Messages = stubMessages()

    val decoderMock: Decoder = mock[Decoder]
    when(decoderMock.crc(any[String])).thenReturn(Some(crcKeyFromDecoder))
    when(decoderMock.roe(any[String])).thenReturn(Some(roeKeyFromDecoder))
    when(decoderMock.mucrSoe(any[String])).thenReturn(Some(mucrSoeKeyFromDecoder))

    val contentBuilder = new EMRResponseConverter(decoderMock, NotificationsPageFormatter)
  }

  "EMRResponseConverter on canConvertFrom" should {

    "return false" when {

      "provided ERS response" in new Test {

        val input =
          exampleNotificationFrontendModel(responseType = MovementTotalsResponse, messageCode = ERS.code)

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
      "provided with EMR response" in new Test {

        val input =
          exampleNotificationFrontendModel(responseType = MovementTotalsResponse, messageCode = EMR.code)

        contentBuilder.canConvertFrom(input) mustBe true
      }
    }
  }

  "EMRResponseConverter on convert" when {

    "provided with response of type different than EMR" should {
      "throw IllegalArgumentException" in new Test {

        val input = exampleNotificationFrontendModel(responseType = ResponseType.ControlResponse, messageCode = "EDL")

        intercept[IllegalArgumentException] { contentBuilder.convert(input) }
      }
    }

    "provided with EMR MovementTotalsResponse with all codes" should {

      "call Decoder" in new Test {

        val input = emrResponseAllCodes

        contentBuilder.convert(input)

        verify(decoderMock).crc(meq(crcKeyFromDecoder.code))
        verify(decoderMock).roe(meq(roeKeyFromDecoder.code))
        verify(decoderMock).mucrSoe(meq(mucrSoeKeyFromDecoder.code))
        verify(decoderMock, times(0)).ics(any())
        verify(decoderMock, times(0)).ducrSoe(any())
      }

      "return NotificationsPageSingleElement with values returned by Messages" in new Test {

        val input = emrResponseAllCodes
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingMovementTotalsResponse"),
          timestampInfo = "23 Oct 2019 at 12:34",
          content = Html(
            s"<p>${messages(crcKeyFromDecoder.contentKey)}</p>" +
              s"<p>${messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.roe")} ${messages(roeKeyFromDecoder.contentKey)}</p>" +
              s"<p>${messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.soe")} ${messages(mucrSoeKeyFromDecoder.contentKey)}</p>"
          )
        )

        contentBuilder.convert(input) mustBe expectedResult
      }
    }

    "provided with EMR MovementTotalsResponse with empty codes" should {

      "call Decoder only for existing codes" in new Test {

        val input = emrResponseMissingCodes

        contentBuilder.convert(input)

        verify(decoderMock).roe(meq(roeKeyFromDecoder.code))
        verify(decoderMock, times(0)).crc(any())
        verify(decoderMock, times(0)).mucrSoe(any())
      }

      "return NotificationsPageSingleElement without content for missing codes" in new Test {

        val input = emrResponseMissingCodes
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingMovementTotalsResponse"),
          timestampInfo = "23 Oct 2019 at 12:34",
          content = Html(
            s"<p>${messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.roe")} ${messages(roeKeyFromDecoder.contentKey)}</p>"
          )
        )

        contentBuilder.convert(input) mustBe expectedResult
      }
    }

    "provided with EMR MovementTotalsResponse with unknown codes" should {

      "call Decoder for all codes" in new Test {

        val input = emrResponseUnknownCodes

        contentBuilder.convert(input)

        verify(decoderMock).crc(meq(UnknownCrcCode))
        verify(decoderMock).roe(meq(UnknownRoeCode))
        verify(decoderMock).mucrSoe(meq(UnknownMucrSoeCode))
      }

      "return NotificationsPageSingleElement without content for unknown codes" in new Test {

        when(decoderMock.crc(meq(UnknownCrcCode))).thenReturn(None)
        when(decoderMock.roe(meq(UnknownRoeCode))).thenReturn(None)
        when(decoderMock.mucrSoe(meq(UnknownMucrSoeCode))).thenReturn(None)

        val input = emrResponseUnknownCodes
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingMovementTotalsResponse"),
          timestampInfo = "23 Oct 2019 at 12:34",
          content = Html("")
        )

        contentBuilder.convert(input) mustBe expectedResult
      }
    }
  }

}

object EMRResponseConverterSpec {

  private val testTimestampString = "2019-10-23T12:34+00:00"
  private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault())
  val testTimestamp = ZonedDateTime.parse(testTimestampString, formatter).toInstant

  val crcKeyFromDecoder = CRCCode.Success
  val roeKeyFromDecoder = ROECode.DocumentaryControl
  val mucrSoeKeyFromDecoder = SOECode.ConsolidationOpen

  val emrResponseAllCodes = exampleNotificationFrontendModel(
    responseType = ResponseType.MovementTotalsResponse,
    messageCode = EMR.code,
    timestampReceived = testTimestamp,
    crcCode = Some(crcKeyFromDecoder.code),
    entries = Seq(
      Entry(
        ucrBlock = Some(UcrBlock(ucr = correctUcr, ucrType = "M")),
        entryStatus = Some(EntryStatus(roe = Some(roeKeyFromDecoder.code), soe = Some(mucrSoeKeyFromDecoder.code)))
      )
    )
  )

  val emrResponseMissingCodes = exampleNotificationFrontendModel(
    responseType = ResponseType.MovementTotalsResponse,
    messageCode = EMR.code,
    timestampReceived = testTimestamp,
    entries = Seq(
      Entry(
        ucrBlock = Some(UcrBlock(ucr = correctUcr, ucrType = "M")),
        entryStatus = Some(EntryStatus(roe = Some(roeKeyFromDecoder.code)))
      )
    )
  )

  val UnknownCrcCode = "1234"
  val UnknownRoeCode = "456"
  val UnknownMucrSoeCode = "7890"

  val emrResponseUnknownCodes = exampleNotificationFrontendModel(
    responseType = ResponseType.MovementTotalsResponse,
    messageCode = EMR.code,
    timestampReceived = testTimestamp,
    crcCode = Some(UnknownCrcCode),
    entries = Seq(
      Entry(
        ucrBlock = Some(UcrBlock(ucr = correctUcr, ucrType = "M")),
        entryStatus = Some(EntryStatus(roe = Some(UnknownRoeCode), soe = Some(UnknownMucrSoeCode)))
      )
    )
  )
}
