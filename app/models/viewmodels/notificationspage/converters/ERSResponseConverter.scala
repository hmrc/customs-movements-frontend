/*
 * Copyright 2021 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import models.notifications.{Entry, Notification}
import models.viewmodels.decoder.Decoder
import models.viewmodels.notificationspage.NotificationsPageSingleElement
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import views.ViewDates
import views.html.components.code_explanation

@Singleton
class ERSResponseConverter @Inject()(decoder: Decoder, viewDates: ViewDates) extends NotificationPageSingleElementConverter {

  override def convert(notification: Notification)(implicit messages: Messages): NotificationsPageSingleElement = {

    val roeCodeExplanation =
      findDucrEntry(notification.entries).flatMap(_.roe).flatMap(buildRoeCodeExplanation).getOrElse(HtmlFormat.empty)
    val soeCodeExplanation =
      findDucrEntry(notification.entries).flatMap(_.soe).flatMap(buildSoeCodeExplanation).getOrElse(HtmlFormat.empty)
    val icsCodeExplanation =
      findDucrEntry(notification.entries).flatMap(_.ics).flatMap(buildIcsCodeExplanation).getOrElse(HtmlFormat.empty)

    NotificationsPageSingleElement(
      title = messages("notifications.elem.title.inventoryLinkingMovementTotalsResponse"),
      timestampInfo = viewDates.formatDateAtTime(notification.timestampReceived),
      content = new Html(List(roeCodeExplanation, soeCodeExplanation, icsCodeExplanation))
    )
  }

  private def findDucrEntry(entries: Seq[Entry]): Option[Entry] = entries.find(_.ucrType.contains("D"))

  private def buildRoeCodeExplanation(roeCode: String)(implicit messages: Messages): Option[Html] = {
    val RoeCodeHeader = messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.roe")
    val roeCodeExplanationText = decoder.roe(roeCode).map(roe => (roe.code, messages(roe.messageKey)))

    roeCodeExplanationText.map { case (code, explanation) => code_explanation(RoeCodeHeader, code, explanation) }
  }

  private def buildSoeCodeExplanation(soeCode: String)(implicit messages: Messages): Option[Html] = {
    val SoeCodeHeader = messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.soe")
    val soeCodeExplanationText = decoder.ducrSoe(soeCode).map(soe => (soe.code, messages(soe.messageKey)))

    soeCodeExplanationText.map { case (code, explanation) => code_explanation(SoeCodeHeader, code, explanation) }
  }

  private def buildIcsCodeExplanation(icsCode: String)(implicit messages: Messages): Option[Html] = {
    val icsCodeExplanationText = decoder.ics(icsCode).map(ics => (ics.code, messages(ics.messageKey)))

    icsCodeExplanationText.map { case (code, explanation) => code_explanation("", code, explanation) }
  }

}
