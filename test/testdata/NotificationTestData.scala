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

package testdata

import java.time.Instant

import testdata.CommonTestData._
import models.UcrBlock
import models.notifications.{NotificationFrontendModel, ResponseType}

object NotificationTestData {

  def exampleNotificationFrontendModel(
    conversationId: String = conversationId,
    responseType: ResponseType = ResponseType.ControlResponse,
    ucrBlocks: Seq[UcrBlock] = Seq(UcrBlock(ucr = correctUcr, ucrType = "D")),
    masterRoe: Option[String] = None,
    masterSoe: Option[String] = None,
    actionCode: Option[String] = None,
    crcCode: Option[String] = None,
    timestampReceived: Instant = Instant.now()
  ): NotificationFrontendModel =
    NotificationFrontendModel(
      timestampReceived = timestampReceived,
      conversationId = conversationId,
      responseType = responseType,
      ucrBlocks = ucrBlocks,
      masterRoe = masterRoe,
      masterSoe = masterSoe,
      actionCode = actionCode,
      crcCode = crcCode
    )
}
