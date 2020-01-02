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

package testdata

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId, ZonedDateTime}

import models.UcrBlock
import models.notifications.{Entry, Notification, ResponseType}
import testdata.CommonTestData._

object NotificationTestData {

  private val testTimestampString = "2019-10-23T12:34:18Z"
  val testTimestamp = ZonedDateTime.parse(testTimestampString).toInstant

  def exampleNotificationFrontendModel(
    conversationId: String = conversationId,
    responseType: ResponseType = ResponseType.ControlResponse,
    entries: Seq[Entry] = Seq(Entry(ucrBlock = Some(UcrBlock(ucr = correctUcr, ucrType = "D")))),
    crcCode: Option[String] = None,
    actionCode: Option[String] = None,
    timestampReceived: Instant = Instant.now(),
    errorCodes: Seq[String] = Seq.empty,
    messageCode: String = ""
  ): Notification =
    Notification(
      timestampReceived = timestampReceived,
      conversationId = conversationId,
      responseType = responseType,
      entries = entries,
      crcCode = crcCode,
      actionCode = actionCode,
      errorCodes = errorCodes,
      messageCode = messageCode
    )
}
