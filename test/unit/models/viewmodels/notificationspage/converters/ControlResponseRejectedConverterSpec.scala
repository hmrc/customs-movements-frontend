/*
 * Copyright 2020 HM Revenue & Customs
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

import base.{BaseSpec, OverridableInjector}
import com.google.inject.{AbstractModule, Guice}
import models.notifications.ResponseType
import models.viewmodels.decoder.{ActionCode, Decoder, ILEError}
import modules.DateTimeModule
import org.mockito.ArgumentMatchers.{anyString, eq => meq}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.Helpers.stubMessages
import testdata.NotificationTestData
import testdata.NotificationTestData.exampleNotificationFrontendModel
import utils.DateTimeTestModule

class ControlResponseRejectedConverterSpec extends BaseSpec with MockitoSugar with BeforeAndAfterEach {

  import ControlResponseRejectedConverterSpec._

  private implicit val messages: Messages = stubMessages()

  private val decoder: Decoder = mock[Decoder]
  private val injector = new OverridableInjector(bind[DateTimeModule].toInstance(new DateTimeTestModule), bind[Decoder].toInstance(decoder))
  private val converter = injector.instanceOf[ControlResponseRejectedConverter]

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(decoder)
    when(decoder.error(anyString)).thenReturn(Some(ILEError("CODE", "Messages.Key")))
  }

  "ControlResponseRejectedConverter on convert" should {

    "return NotificationsPageSingleElement with correct title" in {

      val input = RejectedControlResponseSingleError
      val expectedTitle = messages("notifications.elem.title.inventoryLinkingControlResponse.Rejected")

      val result = converter.convert(input)

      result.title mustBe expectedTitle
    }

    "return NotificationsPageSingleElement with correct timestampInfo" in {

      val input = RejectedControlResponseSingleError
      val expectedTimestampInfo = "23 October 2019 at 12:34pm"

      val result = converter.convert(input)

      result.timestampInfo mustBe expectedTimestampInfo
    }
  }

  "ControlResponseBlockedConverter on convert" when {

    "response contains single error" should {

      "call Decoder for Error once" in {

        val input = RejectedControlResponseSingleError

        converter.convert(input)

        verify(decoder).error(meq(input.errorCodes.head))
      }

      "return NotificationsPageSingleElement with correct content" in {

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

      "call Decoder for every Error" in {

        val input = RejectedControlResponseMultipleErrors

        converter.convert(input)

        input.errorCodes.foreach { errorCode =>
          verify(decoder).error(meq(errorCode))
        }
      }

      "return NotificationsPageSingleElement with correct content" in {

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
