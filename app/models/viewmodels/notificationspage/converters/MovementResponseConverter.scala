/*
 * Copyright 2023 HM Revenue & Customs
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

import models.notifications.Notification
import models.viewmodels.decoder.Decoder
import models.viewmodels.notificationspage.NotificationsPageSingleElement
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import views.ViewDates
import views.html.components.gds.paragraphBody

import javax.inject.{Inject, Singleton}

@Singleton
class MovementResponseConverter @Inject() (val decoder: Decoder) extends NotificationPageSingleElementConverter with CommonResponseConverter {

  override def convert(notification: Notification)(implicit messages: Messages): NotificationsPageSingleElement = {

    val crcCodeExplanation = notification.crcCode.flatMap(buildCrcCodeExplanation).getOrElse(HtmlFormat.empty)

    val roeCodeExplanation =
      findDucrEntry(notification.entries).flatMap(_.roe).flatMap(buildRoeCodeExplanation).getOrElse(HtmlFormat.empty)
    val soeCodeExplanation =
      findDucrEntry(notification.entries).flatMap(_.soe).flatMap(buildSoeCodeExplanation).getOrElse(HtmlFormat.empty)
    val icsCodeExplanation =
      findDucrEntry(notification.entries).flatMap(_.ics).flatMap(buildIcsCodeExplanation).getOrElse(HtmlFormat.empty)

    NotificationsPageSingleElement(
      title = messages("notifications.elem.title.inventoryLinkingMovementResponse"),
      timestampInfo = ViewDates.formatDateAtTime(notification.timestampReceived),
      content = new Html(List(crcCodeExplanation, roeCodeExplanation, soeCodeExplanation, icsCodeExplanation))
    )
  }

  private def buildCrcCodeExplanation(crcCode: String)(implicit messages: Messages): Option[Html] = {
    val CrcCodeHeader = messages("notifications.elem.content.inventoryLinkingMovementResponse.crc")

    decoder.crc(crcCode).map(code => paragraphBody(s"$CrcCodeHeader ${messages(code.messageKey)}"))
  }
}
