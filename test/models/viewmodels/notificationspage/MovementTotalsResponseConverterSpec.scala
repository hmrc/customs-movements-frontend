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

import base.BaseSpec
import models.notifications.ResponseType
import models.viewmodels.decoder._
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.Html
import testdata.NotificationTestData.exampleNotificationFrontendModel

class MovementTotalsResponseConverterSpec extends BaseSpec with MockitoSugar {

  private val testTimestampString = "2019-10-23T12:34+00:00"
  private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault())
  private val testTimestamp = ZonedDateTime.parse(testTimestampString, formatter).toInstant

  private val crcCodeKeyFromDecoder = CrcCode.Success
  private val roeKeyFromDecoder = RoeCode.DocumentaryControl
  private val soeKeyFromDecoder = SoeCode.DeclarationAcceptance

  private trait Test {
    implicit val messages: Messages = Mockito.spy(stubMessages())

    val decoderMock: Decoder = mock[Decoder]
    when(decoderMock.crc(any[String])).thenReturn(Some(crcCodeKeyFromDecoder))
    when(decoderMock.roe(any[String])).thenReturn(Some(roeKeyFromDecoder))
    when(decoderMock.soe(any[String])).thenReturn(Some(soeKeyFromDecoder))

    val contentBuilder = new MovementTotalsResponseConverter(decoderMock)
  }

  "MovementTotalsResponseConverter on canConvertFrom" should {

    "return false" when {
      "provided with NotificationFrontendModel not for MovementTotalsResponse" in new Test {

        val input = exampleNotificationFrontendModel(responseType = ResponseType.ControlResponse)

        contentBuilder.canConvertFrom(input) mustBe false
      }
    }

    "return true" when {
      "provided with MovementTotalsResponse NotificationFrontendModel" in new Test {

        val input = exampleNotificationFrontendModel(responseType = ResponseType.MovementTotalsResponse)

        contentBuilder.canConvertFrom(input) mustBe true
      }
    }
  }

  "MovementTotalsResponseConverter on build" when {

    "provided with NotificationFrontendModel not for MovementTotalsResponse" should {
      "throw IllegalArgumentException" in new Test {

        val input = exampleNotificationFrontendModel(responseType = ResponseType.ControlResponse)

        intercept[IllegalArgumentException] { contentBuilder.convert(input) }
      }
    }

    "provided with MovementTotalsResponse NotificationFrontendModel" should {

      "call Decoder" in new Test {

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementTotalsResponse,
          crcCode = Some(crcCodeKeyFromDecoder.code),
          masterRoe = Some(roeKeyFromDecoder.code),
          masterSoe = Some(soeKeyFromDecoder.code)
        )

        contentBuilder.convert(input)

        verify(decoderMock).crc(meq(crcCodeKeyFromDecoder.code))
        verify(decoderMock).roe(meq(roeKeyFromDecoder.code))
        verify(decoderMock).soe(meq(soeKeyFromDecoder.code))
      }

      "return NotificationsPageSingleElement with values returned by Messages" in new Test {

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementTotalsResponse,
          timestampReceived = testTimestamp,
          crcCode = Some(crcCodeKeyFromDecoder.code),
          masterRoe = Some(roeKeyFromDecoder.code),
          masterSoe = Some(soeKeyFromDecoder.code)
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingMovementTotalsResponse"),
          timestampInfo = messages("notifications.elem.timestampInfo.response", "23 Oct 2019 at 12:34"),
          content = Html(
            s"<p>${messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.crc")} ${crcCodeKeyFromDecoder.contentKey}</p>" +
              s"<p>${messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.roe")} ${roeKeyFromDecoder.contentKey}</p>" +
              s"<p>${messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.soe")} ${soeKeyFromDecoder.contentKey}</p>"
          )
        )

        contentBuilder.convert(input) mustBe expectedResult
      }
    }

    "provided with MovementTotalsResponse NotificationFrontendModel with missing codes" should {

      "call Decoder only for existing codes" in new Test {

        val masterSoe = "1"
        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementTotalsResponse,
          masterSoe = Some(masterSoe)
        )

        contentBuilder.convert(input)

        verify(decoderMock, times(0)).crc(any[String])
        verify(decoderMock, times(0)).roe(any[String])
        verify(decoderMock).soe(meq(masterSoe))
      }

      "return NotificationsPageSingleElement without content for missing codes" in new Test {

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementTotalsResponse,
          timestampReceived = testTimestamp,
          masterSoe = Some(soeKeyFromDecoder.code)
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingMovementTotalsResponse"),
          timestampInfo = messages("notifications.elem.timestampInfo.response", "23 Oct 2019 at 12:34"),
          content = Html(
            s"<p>${messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.soe")} ${soeKeyFromDecoder.contentKey}</p>"
          )
        )

        contentBuilder.convert(input) mustBe expectedResult
      }
    }

    "provided with MovementTotalsResponse NotificationFrontendModel with unknown codes" should {

      "call Decoder for all codes" in new Test {

        val crcCode = "1234"
        val masterRoe = "6"
        val masterSoe = "123"
        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementTotalsResponse,
          crcCode = Some(crcCode),
          masterRoe = Some(masterRoe),
          masterSoe = Some(masterSoe)
        )

        contentBuilder.convert(input)

        verify(decoderMock).crc(meq(crcCode))
        verify(decoderMock).roe(meq(masterRoe))
        verify(decoderMock).soe(meq(masterSoe))
      }

      "return NotificationsPageSingleElement without content for unknown codes" in new Test {

        when(decoderMock.crc(meq("1234"))).thenReturn(None)
        when(decoderMock.roe(any[String])).thenReturn(Some(roeKeyFromDecoder))
        when(decoderMock.soe(meq("123"))).thenReturn(None)

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementTotalsResponse,
          timestampReceived = testTimestamp,
          crcCode = Some("1234"),
          masterRoe = Some(roeKeyFromDecoder.code),
          masterSoe = Some("123")
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingMovementTotalsResponse"),
          timestampInfo = messages("notifications.elem.timestampInfo.response", "23 Oct 2019 at 12:34"),
          content = Html(
            s"<p>${messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.roe")} ${roeKeyFromDecoder.contentKey}</p>"
          )
        )

        contentBuilder.convert(input) mustBe expectedResult
      }
    }
  }

}
