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
import models.viewmodels.decoder.ActionCode
import modules.DateTimeFormatterModule.NotificationsPageFormatter
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import testdata.NotificationTestData
import testdata.NotificationTestData.exampleNotificationFrontendModel

class ControlResponseAcknowledgedConverterSpec extends BaseSpec with MockitoSugar {

  import ControlResponseAcknowledgedConverterSpec._

  private trait Test {
    implicit val messages: Messages = stubMessages()
    val converter = new ControlResponseAcknowledgedConverter(NotificationsPageFormatter)
  }

  "ControlResponseAcknowledgedConverter on convert" should {

    "return NotificationsPageSingleElement with correct title" in new Test {

      val input = AcknowledgedControlResponse
      val expectedTitle = messages("notifications.elem.title.inventoryLinkingControlResponse.AcknowledgedAndProcessed")

      val result = converter.convert(input)

      result.title mustBe expectedTitle
    }

    "return NotificationsPageSingleElement with correct timestampInfo" in new Test {

      val input = AcknowledgedControlResponse
      val expectedTimestampInfo = "23 Oct 2019 at 12:34"

      val result = converter.convert(input)

      result.timestampInfo mustBe expectedTimestampInfo
    }

    "return NotificationsPageSingleElement with correct content" in new Test {

      val input = AcknowledgedControlResponse
      val expectedContent =
        messages("notifications.elem.content.inventoryLinkingControlResponse.AcknowledgedAndProcessed")

      val result = converter.convert(input)

      result.content.toString must include(expectedContent)
    }
  }

}

object ControlResponseAcknowledgedConverterSpec {

  val AcknowledgedControlResponse = exampleNotificationFrontendModel(
    responseType = ResponseType.ControlResponse,
    timestampReceived = NotificationTestData.testTimestamp,
    actionCode = Some(ActionCode.AcknowledgedAndProcessed.code)
  )

}
