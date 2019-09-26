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
import models.notifications.ResponseType.ControlResponse
import models.viewmodels.decoder.Decoder
import models.viewmodels.notificationspage.NotificationsPageSingleElement
import play.api.Logger
import play.api.i18n.Messages
import play.twirl.api.Html

import scala.language.implicitConversions

@Singleton
class ControlResponseConverter @Inject()(decoder: Decoder, dateTimeFormatter: DateTimeFormatter)
    extends NotificationPageSingleElementConverter {

  private val logger = Logger(this.getClass)

  override def canConvertFrom(notification: NotificationFrontendModel): Boolean =
    notification.responseType == ControlResponse

  override def convert(
    notification: NotificationFrontendModel
  )(implicit messages: Messages): NotificationsPageSingleElement =
    if (canConvertFrom(notification)) {

      val actionCodeExplanation = notification.actionCode.flatMap(buildActionCodeExplanation)
      val errorsExplanation = notification.errorCodes.map(buildErrorExplanation).flatten.foldLeft("")(_ + _)

      NotificationsPageSingleElement(
        title = messages("notifications.elem.title.inventoryLinkingControlResponse"),
        timestampInfo = dateTimeFormatter.format(notification.timestampReceived),
        content = Html(actionCodeExplanation.getOrElse("") + errorsExplanation)
      )
    } else {
      throw new IllegalArgumentException(s"Cannot build content for ${notification.responseType}")
    }

  private def buildActionCodeExplanation(actionCode: String)(implicit messages: Messages): Option[String] =
    decoder.actionCode(actionCode).map(code => paragraph(messages(code.messageKey)))

  // TODO move logging for missing error codes to backend
  private def buildErrorExplanation(errorCode: String)(implicit messages: Messages): Option[String] =
    decoder.error(errorCode).map(error => paragraph(messages(error.messageKey))).orElse {
      logger.info(s"Received inventoryLinkingControlResponse with unknown error code: $errorCode")
      None
    }

  private val paragraph: String => String = (text: String) => s"<p>$text</p>"

}
