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

import javax.inject.{Inject, Singleton}
import models.notifications.Notification
import models.viewmodels.decoder.Decoder
import models.viewmodels.notificationspage.NotificationsPageSingleElement
import play.api.i18n.Messages
import play.twirl.api.Html

@Singleton
class MovementResponseConverter @Inject()(decoder: Decoder, dateTimeFormatter: DateTimeFormatter) extends NotificationPageSingleElementConverter {

  override def convert(notification: Notification)(implicit messages: Messages): NotificationsPageSingleElement = {

    val crcCodeExplanation = notification.crcCode.flatMap(buildCrcCodeExplanation)

    NotificationsPageSingleElement(
      title = messages("notifications.elem.title.inventoryLinkingMovementResponse"),
      timestampInfo = dateTimeFormatter.format(notification.timestampReceived),
      content = Html(crcCodeExplanation.getOrElse(""))
    )
  }

  private def buildCrcCodeExplanation(crcCode: String)(implicit messages: Messages): Option[String] = {
    val CrcCodeHeader = messages("notifications.elem.content.inventoryLinkingMovementResponse.crc")

    decoder.crc(crcCode).map(code => paragraph(s"$CrcCodeHeader ${messages(code.messageKey)}"))
  }

  private val paragraph: String => String = (text: String) => s"<p>$text</p>"

}
