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
import models.viewmodels.decoder.{CrcCode, Decoder}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.Html
import testdata.NotificationTestData.exampleNotificationFrontendModel

class MovementResponseConverterSpec extends BaseSpec with MockitoSugar {

  private val testTimestampString = "2019-10-23T12:34+00:00"
  private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault())
  private val testTimestamp = ZonedDateTime.parse(testTimestampString, formatter).toInstant

  private val crcCodeKeyFromDecoder = CrcCode.Success

  private trait Test {
    implicit val messages: Messages = stubMessages()

    val decoderMock: Decoder = mock[Decoder]
    when(decoderMock.crc(any[String])).thenReturn(Some(crcCodeKeyFromDecoder))

    val converter = new MovementResponseConverter(decoderMock)
  }

  "MovementResponseConverter on canConvertFrom" should {

    "return false" when {
      "provided with NotificationFrontendModel not for MovementResponse" in new Test {

        val input = exampleNotificationFrontendModel(responseType = ResponseType.ControlResponse)

        converter.canConvertFrom(input) mustBe false
      }
    }

    "return true" when {
      "provided with MovementResponse NotificationFrontendModel" in new Test {

        val input = exampleNotificationFrontendModel(responseType = ResponseType.MovementResponse)

        converter.canConvertFrom(input) mustBe true
      }
    }
  }

  "MovementResponseConverter on build" when {

    "provided with NotificationFrontendModel not for MovementResponse" should {
      "throw IllegalArgumentException" in new Test {

        val input = exampleNotificationFrontendModel(responseType = ResponseType.ControlResponse)

        intercept[IllegalArgumentException] { converter.convert(input) }
      }
    }

    "provided with MovementResponse NotificationFrontendModel" should {

      "call Decoder" in new Test {

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementResponse,
          crcCode = Some(crcCodeKeyFromDecoder.code)
        )

        converter.convert(input)

        verify(decoderMock).crc(meq(crcCodeKeyFromDecoder.code))
      }

      "return NotificationsPageSingleElement with values returned by Messages" in new Test {

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementResponse,
          timestampReceived = testTimestamp,
          crcCode = Some(crcCodeKeyFromDecoder.code)
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingMovementResponse"),
          timestampInfo = messages("notifications.elem.timestampInfo.response", "23 Oct 2019 at 12:34"),
          content = Html(
            s"<p>${messages("notifications.elem.content.inventoryLinkingMovementResponse.crc")} ${crcCodeKeyFromDecoder.contentKey}</p>"
          )
        )

        converter.convert(input) mustBe expectedResult
      }
    }

    "provided with MovementResponse with missing codes" should {

      "not call Decoder" in new Test {

        val input = exampleNotificationFrontendModel(responseType = ResponseType.MovementResponse)

        converter.convert(input)

        verify(decoderMock, times(0)).crc(any[String])
      }

      "return NotificationsPageSingleElement without content for missing codes" in new Test {

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementResponse,
          timestampReceived = testTimestamp
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingMovementResponse"),
          timestampInfo = messages("notifications.elem.timestampInfo.response", "23 Oct 2019 at 12:34"),
          content = Html("")
        )

        converter.convert(input) mustBe expectedResult
      }
    }

    "provided with MovementResponse with unknown codes" should {

      "call Decoder" in new Test {

        val crcCode = "123456"
        val input =
          exampleNotificationFrontendModel(responseType = ResponseType.MovementResponse, crcCode = Some(crcCode))

        converter.convert(input)

        verify(decoderMock).crc(meq(crcCode))
      }

      "return NotificationsPageSingleElement without content for unknown codes" in new Test {

        val crcCode = "123456"
        when(decoderMock.crc(meq(crcCode))).thenReturn(None)

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementResponse,
          timestampReceived = testTimestamp,
          crcCode = Some(crcCode)
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingMovementResponse"),
          timestampInfo = messages("notifications.elem.timestampInfo.response", "23 Oct 2019 at 12:34"),
          content = Html("")
        )

        converter.convert(input) mustBe expectedResult
      }
    }
  }

}
