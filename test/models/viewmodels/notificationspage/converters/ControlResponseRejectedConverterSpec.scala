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

import base.BaseSpec
import models.notifications.ResponseType
import models.viewmodels.decoder.{ActionCode, Decoder, ILEError}
import modules.DateTimeFormatterModule.NotificationsPageFormatter
import org.mockito.ArgumentMatchers.{anyString, eq => meq}
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import testdata.NotificationTestData
import testdata.NotificationTestData.exampleNotificationFrontendModel

class ControlResponseRejectedConverterSpec extends BaseSpec with MockitoSugar {

  import ControlResponseRejectedConverterSpec._

  private trait Test {
    implicit val messages: Messages = stubMessages()

    val decoder: Decoder = mock[Decoder]
    when(decoder.actionCode(anyString)).thenReturn(Some(ActionCode.Rejected))
    when(decoder.error(anyString)).thenReturn(Some(ILEError("CODE", "Messages.Key")))

    val converter = new ControlResponseRejectedConverter(decoder, NotificationsPageFormatter)
  }

  "ControlResponseRejectedConverter on convert" should {

    "call Decoder for ActionCode" in new Test {

      val input = RejectedControlResponseSingleError

      converter.convert(input)

      verify(decoder).actionCode(meq(input.actionCode.get))
    }

    "return NotificationsPageSingleElement with correct title" in new Test {

      val input = RejectedControlResponseSingleError
      val expectedTitle = messages("notifications.elem.title.inventoryLinkingControlResponse.Rejected")

      val result = converter.convert(input)

      result.title mustBe expectedTitle
    }

    "return NotificationsPageSingleElement with correct timestampInfo" in new Test {

      val input = RejectedControlResponseSingleError
      val expectedTimestampInfo = "23 Oct 2019 at 12:34"

      val result = converter.convert(input)

      result.timestampInfo mustBe expectedTimestampInfo
    }
  }

  "ControlResponseBlockedConverter on convert" when {

    "response contains single error" should {

      "call Decoder for Error once" in new Test {

        val input = RejectedControlResponseSingleError

        converter.convert(input)

        verify(decoder).error(meq(input.errorCodes.head))
      }

      "return NotificationsPageSingleElement with correct content" in new Test {

        val input = RejectedControlResponseSingleError
        val expectedContentHeader =
          messages("notifications.elem.content.inventoryLinkingControlResponse.Rejected.singleError")
        val expectedErrorExplanation = messages("Messages.Key")

        val result = converter.convert(input)

        val contentAsString = result.content.toString
        contentAsString must include(expectedContentHeader)
        contentAsString must include(expectedErrorExplanation)
      }
    }

    "response contains multiple errors" should {

      "call Decoder for every Error" in new Test {

        val input = RejectedControlResponseMultipleErrors

        converter.convert(input)

        input.errorCodes.foreach { errorCode =>
          verify(decoder).error(meq(errorCode))
        }
      }

      "return NotificationsPageSingleElement with correct content" in new Test {

        val input = RejectedControlResponseMultipleErrors
        val expectedContentHeader =
          messages("notifications.elem.content.inventoryLinkingControlResponse.Rejected.multiError")
        val expectedErrorExplanations = List.fill(input.errorCodes.length)(messages("Messages.Key"))

        val result = converter.convert(input)

        val contentAsString = result.content.toString
        contentAsString must include(expectedContentHeader)
        expectedErrorExplanations.foreach { errorExplanation =>
          contentAsString must include(errorExplanation)
        }
      }
    }
  }

}

object ControlResponseRejectedConverterSpec {

  private val RejectedControlResponse = exampleNotificationFrontendModel(
    responseType = ResponseType.ControlResponse,
    timestampReceived = NotificationTestData.testTimestamp,
    actionCode = Some(ActionCode.Rejected.code)
  )

  val RejectedControlResponseSingleError = RejectedControlResponse.copy(errorCodes = Seq("07"))

  val RejectedControlResponseMultipleErrors =
    RejectedControlResponse.copy(errorCodes = Seq("07", "E3481", "29", "E607"))

}