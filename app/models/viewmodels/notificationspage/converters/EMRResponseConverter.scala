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
import models.notifications.ResponseType.MovementTotalsResponse
import models.notifications.{Entry, NotificationFrontendModel}
import models.viewmodels.decoder.Decoder
import models.viewmodels.notificationspage.MovementTotalsResponseType.EMR
import models.viewmodels.notificationspage.NotificationsPageSingleElement
import play.api.i18n.Messages
import play.twirl.api.Html

@Singleton
class EMRResponseConverter @Inject()(decoder: Decoder, dateTimeFormatter: DateTimeFormatter)
    extends NotificationPageSingleElementConverter {

  override def canConvertFrom(notification: NotificationFrontendModel): Boolean =
    (notification.responseType == MovementTotalsResponse) && (notification.messageCode == EMR.code)

  override def convert(
    notification: NotificationFrontendModel
  )(implicit messages: Messages): NotificationsPageSingleElement =
    if (canConvertFrom(notification)) {

      val crcCodeExplanation = notification.crcCode.flatMap(buildCrcCodeExplanation)
      val roeCodeExplanation = findMucrEntry(notification.entries).flatMap(_.roe).flatMap(buildRoeCodeExplanation)
      val soeCodeExplanation = findMucrEntry(notification.entries).flatMap(_.soe).flatMap(buildSoeCodeExplanation)

      NotificationsPageSingleElement(
        title = messages("notifications.elem.title.inventoryLinkingMovementTotalsResponse"),
        timestampInfo = dateTimeFormatter.format(notification.timestampReceived),
        content =
          Html(crcCodeExplanation.getOrElse("") + roeCodeExplanation.getOrElse("") + soeCodeExplanation.getOrElse(""))
      )
    } else {
      throw new IllegalArgumentException(s"Cannot build content for ${notification.responseType}")
    }

  private def findMucrEntry(entries: Seq[Entry]): Option[Entry] = entries.find(_.ucrType.contains("M"))

  private def buildCrcCodeExplanation(crcCode: String)(implicit messages: Messages): Option[String] = {
    val crcCodeExplanationText = decoder.crc(crcCode).map(code => messages(code.contentKey))

    crcCodeExplanationText.map(explanation => paragraph(explanation))
  }

  private def buildRoeCodeExplanation(roeCode: String)(implicit messages: Messages): Option[String] = {
    val RoeCodeHeader = messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.roe")
    val roeCodeExplanationText = decoder.roe(roeCode).map(code => messages(code.contentKey))

    roeCodeExplanationText.map(explanation => paragraph(s"$RoeCodeHeader $explanation"))
  }

  private def buildSoeCodeExplanation(soeCode: String)(implicit messages: Messages): Option[String] = {
    val SoeCodeHeader = messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.soe")
    val soeCodeExplanationText = decoder.mucrSoe(soeCode).map(code => messages(code.contentKey))

    soeCodeExplanationText.map(explanation => paragraph(s"$SoeCodeHeader $explanation"))
  }

  private val paragraph: String => String = (text: String) => s"<p>$text</p>"

}
