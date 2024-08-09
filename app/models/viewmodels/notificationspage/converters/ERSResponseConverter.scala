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

import models.viewmodels.decoder.Decoder
import models.viewmodels.notificationspage.NotificationsPageSingleElement
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import views.helpers.ViewDates

import javax.inject.{Inject, Singleton}

@Singleton
class ERSResponseConverter @Inject() (val decoder: Decoder) extends NotificationPageSingleElementConverter with CommonResponseConverter {

  override def convert(data: ConverterData)(implicit messages: Messages): NotificationsPageSingleElement = {

    val roeCodeExplanation =
      findDucrEntry(data.notification.entries).flatMap(_.roe).flatMap(buildRoeCodeExplanation).getOrElse(HtmlFormat.empty)
    val soeCodeExplanation =
      findDucrEntry(data.notification.entries).flatMap(_.soe).flatMap(buildSoeCodeExplanation).getOrElse(HtmlFormat.empty)
    val icsCodeExplanation =
      findDucrEntry(data.notification.entries).flatMap(_.ics).flatMap(buildIcsCodeExplanation).getOrElse(HtmlFormat.empty)

    NotificationsPageSingleElement(
      title = messages("notifications.elem.title.inventoryLinkingMovementTotalsResponse"),
      timestampInfo = ViewDates.formatDateAtTime(data.notification.timestampReceived),
      content = new Html(List(roeCodeExplanation, soeCodeExplanation, icsCodeExplanation))
    )
  }
}
