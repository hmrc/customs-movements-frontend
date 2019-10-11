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
import models.notifications.ResponseType.MovementTotalsResponse
import modules.DateTimeFormatterModule.NotificationsPageFormatter
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.HtmlFormat
import testdata.NotificationTestData.exampleNotificationFrontendModel

class UnknownResponseConverterSpec extends BaseSpec {

  private val testTimestampString = "2019-10-23T12:34+00:00"
  private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault())
  private val testTimestamp = ZonedDateTime.parse(testTimestampString, formatter).toInstant

  private implicit val messages: Messages = stubMessages()
  private val converter = new UnknownResponseConverter(NotificationsPageFormatter)

  "UnknownResponseConverter on convert" should {

    "return generic NotificationsPageSingleElement" in {

      val input = exampleNotificationFrontendModel(responseType = MovementTotalsResponse, messageCode = "UNKNOWN", timestampReceived = testTimestamp)

      val result = converter.convert(input)

      result.title mustBe messages("notifications.elem.title.unknown")
      result.timestampInfo mustBe "23 Oct 2019 at 12:34"
      result.content mustBe HtmlFormat.empty
    }
  }

}
