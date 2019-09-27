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
import models.notifications.ResponseType
import models.viewmodels.decoder.{ActionCode, CHIEFError, Decoder, ILEError}
import models.viewmodels.notificationspage.NotificationsPageSingleElement
import modules.DateTimeFormatterModule.NotificationsPageFormatter
import org.mockito.ArgumentMatchers.{any, anyString, eq => meq}
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
  private val InvalidUcrFormatILEError = ILEError("01", "error.ile.InvalidUcrFormat")
  private val NoPriorArrivalFoundAtDepartureLocationILEError =
    ILEError("13", "error.ile.NoPriorArrivalFoundAtDepartureLocation")
  private val MucrAlreadyDepartedILEError = ILEError("29", "error.ile.MucrAlreadyDeparted")

  private val UcrNotExistCHIEFError = CHIEFError("E408", "error.chief.UcrNotExist")

  private trait Test {
    implicit val messages: Messages = stubMessages()

    val decoderMock: Decoder = mock[Decoder]
    when(decoderMock.actionCode(anyString)).thenReturn(Some(AcknowledgedAndProcessedActionCode))
    when(decoderMock.error(anyString)).thenReturn(None)
    when(decoderMock.error(anyString)).thenReturn(None)

    val contentBuilder = new ControlResponseConverter(decoderMock, NotificationsPageFormatter)
  }

  "ControlResponseConverter on convert" when {

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

        verify(decoderMock, times(0)).error(any[String])
      }

      "return NotificationsPageSingleElement with values returned by Messages" in new Test {

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.ControlResponse,
          timestampReceived = testTimestamp,
          actionCode = Some(AcknowledgedAndProcessedActionCode.code)
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingControlResponse"),
          timestampInfo = "23 Oct 2019 at 12:34",
          content = Html(s"<p>${messages(AcknowledgedAndProcessedActionCode.messageKey)}</p>")
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
          errorCodes = Seq("01", "29", "13", "E408", "E3305")
        )

        contentBuilder.convert(input)

        verify(decoderMock).error(meq("01"))
        verify(decoderMock).error(meq("29"))
        verify(decoderMock).error(meq("13"))
        verify(decoderMock).error(meq("E408"))
        verify(decoderMock).error(meq("E3305"))
      }

      "return NotificationsPageSingleElement with values returned by Messages" in new Test {

        when(decoderMock.actionCode(any[String])).thenReturn(Some(ActionCode.Rejected))
        when(decoderMock.error(meq(InvalidUcrFormatILEError.code))).thenReturn(Some(InvalidUcrFormatILEError))
        when(decoderMock.error(meq(NoPriorArrivalFoundAtDepartureLocationILEError.code)))
          .thenReturn(Some(NoPriorArrivalFoundAtDepartureLocationILEError))
        when(decoderMock.error(meq(MucrAlreadyDepartedILEError.code)))
          .thenReturn(Some(MucrAlreadyDepartedILEError))
        when(decoderMock.error(meq(UcrNotExistCHIEFError.code))).thenReturn(Some(UcrNotExistCHIEFError))

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.ControlResponse,
          timestampReceived = testTimestamp,
          actionCode = Some(ActionCode.Rejected.code),
          errorCodes = Seq("01", "29", "13", "E408")
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingControlResponse"),
          timestampInfo = "23 Oct 2019 at 12:34",
          content = Html(
            s"<p>${messages(ActionCode.Rejected.messageKey)}</p>" +
              s"<p>${messages(InvalidUcrFormatILEError.messageKey)}</p>" +
              s"<p>${messages(MucrAlreadyDepartedILEError.messageKey)}</p>" +
              s"<p>${messages(NoPriorArrivalFoundAtDepartureLocationILEError.messageKey)}</p>" +
              s"<p>${messages(UcrNotExistCHIEFError.messageKey)}</p>"
          )
        )

        contentBuilder.convert(input) mustBe expectedResult
      }

      "return NotificationsPageSingleElement with empty content for unknown error" in new Test {

        when(decoderMock.actionCode(any[String])).thenReturn(Some(ActionCode.Rejected))
        when(decoderMock.error(any[String])).thenReturn(None)
        when(decoderMock.error(meq("01"))).thenReturn(Some(InvalidUcrFormatILEError))
        when(decoderMock.error(meq("13"))).thenReturn(Some(NoPriorArrivalFoundAtDepartureLocationILEError))

        val input = exampleNotificationFrontendModel(
          responseType = ResponseType.ControlResponse,
          timestampReceived = testTimestamp,
          actionCode = Some(ActionCode.Rejected.code),
          errorCodes = Seq("01", "123", "13")
        )
        val expectedResult = NotificationsPageSingleElement(
          title = messages("notifications.elem.title.inventoryLinkingControlResponse"),
          timestampInfo = "23 Oct 2019 at 12:34",
          content = Html(
            s"<p>${messages(ActionCode.Rejected.messageKey)}</p>" +
              s"<p>${messages("error.ile.InvalidUcrFormat")}</p>" +
              s"<p>${messages("error.ile.NoPriorArrivalFoundAtDepartureLocation")}</p>"
          )
        )

        contentBuilder.convert(input) mustBe expectedResult
      }
    }
  }
}
