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
import models.notifications.ResponseType.ControlResponse
import models.viewmodels.decoder.Decoder
import play.api.Logger
import play.api.i18n.Messages
import play.twirl.api.Html

@Singleton
private[notificationspage] class ControlResponseConverter @Inject()(decoder: Decoder)
    extends NotificationPageSingleElementConverter {

  private val logger = Logger(this.getClass)
  private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy 'at' HH:mm").withZone(ZoneId.systemDefault())

  override def canConvertFrom(notification: NotificationFrontendModel): Boolean =
    notification.responseType == ControlResponse

  override def convert(
    notification: NotificationFrontendModel
  )(implicit messages: Messages): NotificationsPageSingleElement =
    if (canConvertFrom(notification)) {
      val actionCodeExplanation = for {
        code <- notification.actionCode
        actionCode <- decoder.actionCode(code)
        content = s"<p>${messages(actionCode.contentKey)}</p>"
      } yield content

      val errorExplanation = (for {
        code <- notification.errorCodes
        errorCode <- {
          val errorCode = decoder.errorCode(code)
          if (errorCode.isEmpty) logger.warn(s"Received inventoryLinkingControlResponse with unknown error code: $code")

          errorCode
        }

        content = s"<p>${messages(errorCode.contentKey)}</p>"
      } yield content).foldLeft("")(_ + _)

      NotificationsPageSingleElement(
        title = messages("notifications.elem.title.inventoryLinkingControlResponse"),
        timestampInfo = timestampInfoResponse(notification.timestampReceived),
        content = Html(actionCodeExplanation.getOrElse("") + errorExplanation)
      )
    } else {
      throw new IllegalArgumentException(s"Cannot build content for ${notification.responseType}")
    }

  private def timestampInfoResponse(responseTimestamp: Instant)(implicit messages: Messages): String =
    messages("notifications.elem.timestampInfo.response", dateTimeFormatter.format(responseTimestamp))

}
