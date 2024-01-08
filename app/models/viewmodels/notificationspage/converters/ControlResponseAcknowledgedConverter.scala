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
import models.viewmodels.notificationspage.NotificationsPageSingleElement
import play.api.i18n.Messages
import play.twirl.api.Html
import views.helpers.ViewDates
import views.html.components.paragraph

import javax.inject.Singleton

@Singleton
class ControlResponseAcknowledgedConverter extends NotificationPageSingleElementConverter {

  private val TitleMessagesKey = "notifications.elem.title.inventoryLinkingControlResponse.AcknowledgedAndProcessed"
  private val ContentHeaderMessagesKey = "notifications.elem.content.inventoryLinkingControlResponse.AcknowledgedAndProcessed"

  override def convert(notification: Notification)(implicit messages: Messages): NotificationsPageSingleElement =
    NotificationsPageSingleElement(
      title = messages(TitleMessagesKey),
      timestampInfo = ViewDates.formatDateAtTime(notification.timestampReceived),
      content = buildContent()
    )

  private def buildContent()(implicit messages: Messages): Html =
    paragraph(messages(ContentHeaderMessagesKey), "govuk-body")
}
