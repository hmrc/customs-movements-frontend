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
import models.viewmodels.decoder.ErrorCode._
import models.viewmodels.decoder.{ActionCode, Decoder}
import modules.DateTimeFormatterModule.NotificationsPageFormatter
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.Html
import testdata.NotificationTestData.exampleNotificationFrontendModel

class ControlResponseConverterSpec extends BaseSpec with MockitoSugar {

  private val testTimestampString = "2019-10-23T12:34+00:00"
  private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault())
  private val testTimestamp = ZonedDateTime.parse(testTimestampString, formatter).toInstant

  private val AcknowledgedAndProcessedActionCode = ActionCode.AcknowledgedAndProcessed

  private trait Test {
    implicit val messages: Messages = stubMessages()

    val decoderMock: Decoder = mock[Decoder]
    when(decoderMock.actionCode(any[String])).thenReturn(Some(AcknowledgedAndProcessedActionCode))
    when(decoderMock.errorCode(any[String])).thenReturn(None)

    val contentBuilder = new ControlResponseConverter(decoderMock, NotificationsPageFormatter)
  }

  "ControlResponseConverter on canConvertFrom" should {

    "return false" when {
      "provided with NotificationFrontendModel not for ControlResponse" in new Test {

        val input = exampleNotificationFrontendModel(responseType = ResponseType.MovementTotalsResponse)

        contentBuilder.canConvertFrom(input) mustBe false
      }
    }

    "return true" when {
      "provided with ControlResponse NotificationFrontendModel" in new Test {

        val input = exampleNotificationFrontendModel(responseType = ResponseType.ControlResponse)

        contentBuilder.canConvertFrom(input) mustBe true
      }
    }
  }

  "ControlResponseConverter on convert" when {

    "provided with NotificationFrontendModel not for ControlResponse" should {
      "throw IllegalArgumentException" in new Test {

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.MovementTotalsResponse,
          actionCode = Some(AcknowledgedAndProcessedActionCode.code)
        )

        intercept[IllegalArgumentException] { contentBuilder.convert(input) }
      }
    }

    "provided with ControlResponse NotificationFrontendModel without errors" should {

      "call Decoder for ActionCode" in new Test {

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.ControlResponse,
          actionCode = Some(AcknowledgedAndProcessedActionCode.code)
        )

        contentBuilder.convert(input)

        verify(decoderMock).actionCode(meq(AcknowledgedAndProcessedActionCode.code))
      }

      "not call Decoder for ErrorCode" in new Test {

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.ControlResponse,
          actionCode = Some(AcknowledgedAndProcessedActionCode.code)
        )

        contentBuilder.convert(input)

        verify(decoderMock, times(0)).errorCode(any[String])
      }

      "return NotificationsPageSingleElement with values returned by Messages" in new Test {

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.ControlResponse,
          timestampReceived = testTimestamp,
          actionCode = Some(AcknowledgedAndProcessedActionCode.code)
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingControlResponse"),
          timestampInfo = messages("notifications.elem.timestampInfo.response", "23 Oct 2019 at 12:34"),
          content = Html(s"<p>${messages(AcknowledgedAndProcessedActionCode.contentKey)}</p>")
        )

        contentBuilder.convert(input) mustBe expectedResult
      }
    }

    "provided with ControlResponse NotificationFrontendModel with errors" should {

      "call Decoder for ActionCode" in new Test {

        when(decoderMock.actionCode(any[String])).thenReturn(Some(ActionCode.Rejected))

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.ControlResponse,
          actionCode = Some(ActionCode.Rejected.code),
          errorCodes = Seq("01", "29", "13")
        )

        contentBuilder.convert(input)

        verify(decoderMock).actionCode(meq(ActionCode.Rejected.code))
      }

      "call Decoder for every ErrorCode" in new Test {

        when(decoderMock.actionCode(any[String])).thenReturn(Some(ActionCode.Rejected))

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.ControlResponse,
          actionCode = Some(ActionCode.Rejected.code),
          errorCodes = Seq("01", "29", "13")
        )

        contentBuilder.convert(input)

        verify(decoderMock).errorCode(meq("01"))
        verify(decoderMock).errorCode(meq("29"))
        verify(decoderMock).errorCode(meq("13"))
      }

      "return NotificationsPageSingleElement with values returned by Messages" in new Test {

        when(decoderMock.actionCode(any[String])).thenReturn(Some(ActionCode.Rejected))
        when(decoderMock.errorCode(meq("01"))).thenReturn(Some(InvalidUcrFormat))
        when(decoderMock.errorCode(meq("13"))).thenReturn(Some(NoPriorArrivalFoundAtDepartureLocation))
        when(decoderMock.errorCode(meq("29"))).thenReturn(Some(MucrAlreadyDeparted))

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.ControlResponse,
          timestampReceived = testTimestamp,
          actionCode = Some(ActionCode.Rejected.code),
          errorCodes = Seq("01", "29", "13")
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingControlResponse"),
          timestampInfo = messages("notifications.elem.timestampInfo.response", "23 Oct 2019 at 12:34"),
          content = Html(
            s"<p>${messages(ActionCode.Rejected.contentKey)}</p>" +
              s"<p>${messages("decoder.errorCode.InvalidUcrFormat")}</p>" +
              s"<p>${messages("decoder.errorCode.MucrAlreadyDeparted")}</p>" +
              s"<p>${messages("decoder.errorCode.NoPriorArrivalFoundAtDepartureLocation")}</p>"
          )
        )

        contentBuilder.convert(input) mustBe expectedResult
      }

      "return NotificationsPageSingleElement with empty content for unknown error" in new Test {

        when(decoderMock.actionCode(any[String])).thenReturn(Some(ActionCode.Rejected))
        when(decoderMock.errorCode(any[String])).thenReturn(None)
        when(decoderMock.errorCode(meq("01"))).thenReturn(Some(InvalidUcrFormat))
        when(decoderMock.errorCode(meq("13"))).thenReturn(Some(NoPriorArrivalFoundAtDepartureLocation))

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.ControlResponse,
          timestampReceived = testTimestamp,
          actionCode = Some(ActionCode.Rejected.code),
          errorCodes = Seq("01", "123", "13")
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingControlResponse"),
          timestampInfo = messages("notifications.elem.timestampInfo.response", "23 Oct 2019 at 12:34"),
          content = Html(
            s"<p>${messages(ActionCode.Rejected.contentKey)}</p>" +
              s"<p>${messages("decoder.errorCode.InvalidUcrFormat")}</p>" +
              s"<p>${messages("decoder.errorCode.NoPriorArrivalFoundAtDepartureLocation")}</p>"
          )
        )

        contentBuilder.convert(input) mustBe expectedResult
      }
    }
  }

}
