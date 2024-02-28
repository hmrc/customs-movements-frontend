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
import play.api.Logger
import play.api.i18n.Messages
import play.twirl.api.Html
import views.helpers.ViewDates
import views.html.components.{notification_errors, paragraph}

import javax.inject.{Inject, Singleton}

@Singleton
class ControlResponseRejectedConverter @Inject() (decoder: Decoder) extends NotificationPageSingleElementConverter {

  private val logger = Logger(this.getClass)

  private val TitleMessagesKey = "notifications.elem.title.inventoryLinkingControlResponse.Rejected"
  private val ContentHeaderMessagesKey = "notifications.elem.content.inventoryLinkingControlResponse.Rejected"

  override def convert(data: ConverterData)(implicit messages: Messages): NotificationsPageSingleElement =
    NotificationsPageSingleElement(
      title = messages(TitleMessagesKey),
      timestampInfo = ViewDates.formatDateAtTime(data.notification.timestampReceived),
      content = buildContent(data.notification)
    )

  private def buildContent(notification: Notification)(implicit messages: Messages): Html = {
    val contentHeader = paragraph(messages(ContentHeaderMessagesKey + contentHeaderSuffix(notification)))
    val errorsExplanations = buildErrorsExplanations(notification.errorCodes)

    new Html(List(contentHeader, errorsExplanations))
  }

  private def contentHeaderSuffix(notification: Notification): String =
    if (notification.errorCodes.length < 2) ".singleError" else ".multiError"

  private def buildErrorsExplanations(errorCodes: Seq[String])(implicit messages: Messages): Html = {
    val errorsExplanationsText = errorCodes.map(getErrorExplanationText).flatten
    notification_errors(errorsExplanationsText)
  }

  // TODO move logging for missing error codes to backend
  private def getErrorExplanationText(errorCode: String)(implicit messages: Messages): Option[String] =
    decoder
      .error(errorCode)
      .map(code => messages(code.messageKey))
      .orElse {
        logger.info(s"Received inventoryLinkingControlResponse with unknown error code: $errorCode")
        Some(errorCode)
      }

}
