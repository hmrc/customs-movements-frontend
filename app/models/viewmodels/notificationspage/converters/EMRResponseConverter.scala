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
import models.notifications.{Entry, NotificationFrontendModel}
import models.viewmodels.decoder.Decoder
import models.viewmodels.notificationspage.NotificationsPageSingleElement
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import views.html.components.code_explanation

@Singleton
class EMRResponseConverter @Inject()(decoder: Decoder, dateTimeFormatter: DateTimeFormatter) extends NotificationPageSingleElementConverter {

  override def convert(notification: NotificationFrontendModel)(implicit messages: Messages): NotificationsPageSingleElement = {
    val crcCodeExplanation = notification.crcCode.flatMap(buildCrcCodeExplanation).getOrElse(HtmlFormat.empty)
    val roeCodeExplanation =
      findMucrEntry(notification.entries).flatMap(_.roe).flatMap(buildRoeCodeExplanation).getOrElse(HtmlFormat.empty)
    val soeCodeExplanation =
      findMucrEntry(notification.entries).flatMap(_.soe).flatMap(buildSoeCodeExplanation).getOrElse(HtmlFormat.empty)

    NotificationsPageSingleElement(
      title = messages("notifications.elem.title.inventoryLinkingMovementTotalsResponse"),
      timestampInfo = dateTimeFormatter.format(notification.timestampReceived),
      content = new Html(List(crcCodeExplanation, roeCodeExplanation, soeCodeExplanation))
    )
  }

  private def findMucrEntry(entries: Seq[Entry]): Option[Entry] = entries.find(_.ucrType.contains("M"))

  private def buildCrcCodeExplanation(crcCode: String)(implicit messages: Messages): Option[Html] = {
    val crcCodeExplanationText = decoder.crc(crcCode).map(code => messages(code.messageKey))

    crcCodeExplanationText.map(explanation => Html(paragraph(explanation)))
  }

  private def buildRoeCodeExplanation(roeCode: String)(implicit messages: Messages): Option[Html] = {
    val RoeCodeHeader = messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.roe")
    val roeCodeExplanationText = decoder.roe(roeCode).map(code => messages(code.messageKey))

    roeCodeExplanationText.map(explanation => code_explanation(RoeCodeHeader, explanation))
  }

  private def buildSoeCodeExplanation(soeCode: String)(implicit messages: Messages): Option[Html] = {
    val SoeCodeHeader = messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.soe")
    val soeCodeExplanationText = decoder.mucrSoe(soeCode).map(code => messages(code.messageKey))

    soeCodeExplanationText.map(explanation => code_explanation(SoeCodeHeader, explanation))
  }

  private val paragraph: String => String = (text: String) => s"<p>$text</p>"

}
