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
import models.notifications.NotificationFrontendModel
import models.viewmodels.decoder.Decoder
import models.viewmodels.notificationspage.NotificationsPageSingleElement
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import views.html.components.paragraph

@Singleton
class ControlResponseAcknowledgedConverter @Inject()(decoder: Decoder, dateTimeFormatter: DateTimeFormatter)
    extends NotificationPageSingleElementConverter {

  private val TitleMessagesKey = "notifications.elem.title.inventoryLinkingControlResponse.AcknowledgedAndProcessed"

  override def convert(
    notification: NotificationFrontendModel
  )(implicit messages: Messages): NotificationsPageSingleElement = NotificationsPageSingleElement(
    title = messages(TitleMessagesKey),
    timestampInfo = dateTimeFormatter.format(notification.timestampReceived),
    content = buildContent(notification)
  )

  private def buildContent(notification: NotificationFrontendModel)(implicit messages: Messages): Html =
    notification.actionCode
      .flatMap(decoder.actionCode)
      .map(actionCode => paragraph(messages(actionCode.messageKey)))
      .getOrElse(HtmlFormat.empty)

}
