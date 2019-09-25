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
import models.submissions.ActionType._
import models.submissions.SubmissionFrontendModel
import models.viewmodels.decoder.Decoder
import models.viewmodels.notificationspage.converters._
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}

@Singleton
class NotificationPageSingleElementFactory @Inject()(
  decoder: Decoder,
  controlResponseConverter: ControlResponseConverter,
  ersResponseConverter: ERSResponseConverter,
  emrResponseConverter: EMRResponseConverter,
  movementResponseConverter: MovementResponseConverter
) {

  private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy 'at' HH:mm").withZone(ZoneId.systemDefault())

  def build(submission: SubmissionFrontendModel)(implicit messages: Messages): NotificationsPageSingleElement =
    submission.actionType match {
      case Arrival | Departure | DucrDisassociation | ShutMucr => buildForRequest(submission)
      case DucrAssociation                                     => buildForDucrAssociation(submission)
    }

  private def buildForRequest(
    submission: SubmissionFrontendModel
  )(implicit messages: Messages): NotificationsPageSingleElement = {

    val ucrMessage = if (submission.hasMucr) "MUCR" else "DUCR"

    val content = Html(
      s"<p>${messages(s"notifications.elem.content.${submission.actionType.value}", ucrMessage)}</p>" +
        s"<p>${messages("notifications.elem.content.footer")}</p>"
    )

    NotificationsPageSingleElement(
      title = messages(s"notifications.elem.title.${submission.actionType.value}"),
      timestampInfo = dateTimeFormatter.format(submission.requestTimestamp),
      content = content
    )
  }

  private def buildForDucrAssociation(
    submission: SubmissionFrontendModel
  )(implicit messages: Messages): NotificationsPageSingleElement = {

    val ducrs = submission.ucrBlocks.filter(_.ucrType == "D")
    val content = Html(
      s"<p>${messages(s"notifications.elem.content.${submission.actionType.value}")}</p>" +
        ducrs.map(block => s"<p>${block.ucr}</p>").mkString +
        s"<p>${messages("notifications.elem.content.footer")}</p>"
    )

    buildForRequest(submission).copy(content = content)
  }

  private val converters =
    Set(controlResponseConverter, ersResponseConverter, emrResponseConverter, movementResponseConverter)

  def build(notification: NotificationFrontendModel)(implicit messages: Messages): NotificationsPageSingleElement =
    converters
      .find(_.canConvertFrom(notification))
      .map(_.convert(notification))
      .getOrElse(buildForUnspecified(notification.timestampReceived))

  private def buildForUnspecified(
    responseTimestamp: Instant
  )(implicit messages: Messages): NotificationsPageSingleElement =
    NotificationsPageSingleElement(
      title = messages("notifications.elem.title.unspecified"),
      timestampInfo = dateTimeFormatter.format(responseTimestamp),
      content = HtmlFormat.empty
    )

}
