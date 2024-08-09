/*
 * Copyright 2024 HM Revenue & Customs
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

import models.viewmodels.notificationspage.NotificationsPageSingleElement
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import views.helpers.ViewDates

import javax.inject.Singleton

@Singleton
class UnknownResponseConverter extends NotificationPageSingleElementConverter {

  override def convert(data: ConverterData)(implicit messages: Messages): NotificationsPageSingleElement =
    NotificationsPageSingleElement(
      title = messages("notifications.elem.title.unknown"),
      timestampInfo = ViewDates.formatDateAtTime(data.notification.timestampReceived),
      content = HtmlFormat.empty
    )
}
