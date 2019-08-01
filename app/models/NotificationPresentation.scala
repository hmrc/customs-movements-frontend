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

package models

import java.time.Instant

import play.api.libs.json.Json

final case class NotificationPresentation(
  timestampReceived: Instant = Instant.now(),
  conversationId: String,
  ucrBlocks: Seq[UcrBlock],
  roe: Option[String],
  soe: Option[String]
) extends Ordered[NotificationPresentation] {

  override def compare(other: NotificationPresentation): Int =
    this.timestampReceived.compareTo(other.timestampReceived)
}

object NotificationPresentation {
  implicit val format = Json.format[NotificationPresentation]
}
