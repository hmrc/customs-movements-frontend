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

package models.viewmodels.notificationspage

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}

import javax.inject.{Inject, Singleton}
import models.notifications.NotificationFrontendModel
import models.notifications.ResponseType.MovementTotalsResponse
import models.viewmodels.decoder.Decoder
import play.api.i18n.Messages
import play.twirl.api.Html

@Singleton
private[notificationspage] class MovementTotalsResponseConverter @Inject()(decoder: Decoder)
    extends NotificationPageSingleElementConverter {

  private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy 'at' HH:mm").withZone(ZoneId.systemDefault())

  override def canConvertFrom(notification: NotificationFrontendModel): Boolean =
    notification.responseType == MovementTotalsResponse

  override def convert(
    notification: NotificationFrontendModel
  )(implicit messages: Messages): NotificationsPageSingleElement =
    if (canConvertFrom(notification)) {

      val crcCodeContent = getContentForCrcCode(notification)
      val roeContent =
        notification.masterRoe.flatMap(roe => decoder.roe(roe).map(decodedRoe => messages(decodedRoe.contentKey)))
      val soeContent =
        notification.masterSoe.flatMap(soe => decoder.soe(soe).map(decodedSoe => messages(decodedSoe.contentKey)))

      val firstLine = crcCodeContent.map { content =>
        paragraph(s"${messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.crc")} $content")
      }
      val secondLine = roeContent.map { content =>
        paragraph(s"${messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.roe")} $content")
      }
      val thirdLine = soeContent.map { content =>
        paragraph(s"${messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.soe")} $content")
      }

      NotificationsPageSingleElement(
        title = messages("notifications.elem.title.inventoryLinkingMovementTotalsResponse"),
        timestampInfo = timestampInfoResponse(notification.timestampReceived),
        content = Html(firstLine.getOrElse("") + secondLine.getOrElse("") + thirdLine.getOrElse(""))
      )
    } else {
      throw new IllegalArgumentException(s"Cannot build content for ${notification.responseType}")
    }

  private def getContentForCrcCode(
    notification: NotificationFrontendModel
  )(implicit messages: Messages): Option[String] =
    for {
      code <- notification.crcCode
      decodedCrcCode <- decoder.crc(code)
      content = messages(decodedCrcCode.contentKey)
    } yield content

  private val paragraph: String => String = (text: String) => s"<p>$text</p>"

  private def timestampInfoResponse(responseTimestamp: Instant)(implicit messages: Messages): String =
    messages("notifications.elem.timestampInfo.response", dateTimeFormatter.format(responseTimestamp))

}
