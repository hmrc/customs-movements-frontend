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

import models.viewmodels.notificationspage.NotificationsPageSingleElement
import play.twirl.api.Html

object CommonTestData {

  val validEori: String = "GB12345678"
  val correctUcr: String = "GB/1UZYBD3XE-1J8MEBF9N6X65B"
  val correctUcr_2: String = "GB/1UZYBD3XE-1J8MEBF9N6X78C"
  val correctUcr_3: String = "GB/1UZYBD3XE-1J8MEBF9N6XABC"
  val conversationId: String = "93feaae9-5043-4569-9fc5-ff04bfea0d11"
  val conversationId_2: String = "93feaae9-5043-4569-9fc5-ff04bfea0d22"
  val conversationId_3: String = "93feaae9-5043-4569-9fc5-ff04bfea0d33"

  def exampleNotificationPageSingleElement(
    title: String = "TITLE",
    timestampInfo: String = "TIMESTAMP",
    content: Html = Html("<p>CONTENT</p>")
  ): NotificationsPageSingleElement =
    NotificationsPageSingleElement(title = title, timestampInfo = timestampInfo, content = content)

}
