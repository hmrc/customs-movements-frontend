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

package models.viewmodels

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}

import javax.inject.{Inject, Singleton}
import models.notifications.NotificationFrontendModel
import models.notifications.ResponseType._
import models.submissions.ActionType._
import models.submissions.SubmissionFrontendModel
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}

@Singleton
class NotificationPageSingleElementFactory @Inject()(decoder: Decoder) {

  private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy").withZone(ZoneId.systemDefault())
  private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

  def build(submission: SubmissionFrontendModel)(implicit messages: Messages): NotificationsPageSingleElement =
    submission.actionType match {
      case Arrival            => buildForRequest(submission)
      case Departure          => buildForRequest(submission)
      case DucrAssociation    => buildForDucrAssociation(submission)
      case DucrDisassociation => buildForRequest(submission)
      case ShutMucr           => buildForRequest(submission)
    }

  private def buildForRequest(
    submission: SubmissionFrontendModel
  )(implicit messages: Messages): NotificationsPageSingleElement =
    NotificationsPageSingleElement(
      title = messages(s"notifications.elem.title.${submission.actionType.value}"),
      timestampInfo = messages(
        "notifications.elem.timestampInfo.request",
        dateFormatter.format(submission.requestTimestamp),
        timeFormatter.format(submission.requestTimestamp)
      ),
      content = Html(
        s"<p>${messages(s"notifications.elem.content.${submission.actionType.value}")}</p>" +
          s"<p>${messages("notifications.elem.content.footer")}</p>"
      )
    )

  private def buildForDucrAssociation(
    submission: SubmissionFrontendModel
  )(implicit messages: Messages): NotificationsPageSingleElement = {
    val ducrs = submission.ucrBlocks.filter(_.ucrType == "D")

    buildForRequest(submission).copy(
      content = Html(
        s"<p>${messages(s"notifications.elem.content.${submission.actionType.value}")}</p>" +
          ducrs.map(block => s"<p>${block.ucr}</p>").mkString +
          s"<p>${messages("notifications.elem.content.footer")}</p>"
      )
    )
  }

  def build(notification: NotificationFrontendModel)(implicit messages: Messages): NotificationsPageSingleElement =
    notification.responseType match {
      case ControlResponse        => buildForControlResponse(notification)
      case MovementTotalsResponse => buildForMovementTotalsResponse(notification)
      case MovementResponse       => buildForMovementResponse(notification)
      case _                      => buildForUnspecified(notification.timestampReceived)
    }

  private def buildForControlResponse(
    notification: NotificationFrontendModel
  )(implicit messages: Messages): NotificationsPageSingleElement =
    NotificationsPageSingleElement(
      title = messages(s"notifications.elem.title.inventoryLinkingControlResponse"),
      timestampInfo = messages(
        "notifications.elem.timestampInfo.response",
        dateFormatter.format(notification.timestampReceived),
        timeFormatter.format(notification.timestampReceived)
      ),
      content = Html(notification.actionCode.map { code =>
        s"<p>${messages(s"notifications.elem.content.inventoryLinkingControlResponse.$code")}</p>"
      }.getOrElse(""))
    )

  private def buildForMovementTotalsResponse(
    notification: NotificationFrontendModel
  )(implicit messages: Messages): NotificationsPageSingleElement =
    NotificationsPageSingleElement(
      title = messages(s"notifications.elem.title.inventoryLinkingMovementTotalsResponse"),
      timestampInfo = messages(
        "notifications.elem.timestampInfo.response",
        dateFormatter.format(notification.timestampReceived),
        timeFormatter.format(notification.timestampReceived)
      ),
      content = {
        val firstLine = notification.crcCode.map { crcCode =>
          s"<p>${messages(s"notifications.elem.content.inventoryLinkingMovementTotalsResponse.crc", decoder.crc(crcCode))}</p>"
        }
        val secondLine = notification.masterRoe.map { roe =>
          s"<p>${messages(s"notifications.elem.content.inventoryLinkingMovementTotalsResponse.roe", decoder.roe(roe))}</p>"
        }
        val thirdLine = notification.masterSoe.map { soe =>
          s"<p>${messages(s"notifications.elem.content.inventoryLinkingMovementTotalsResponse.soe", decoder.soe(soe))}</p>"
        }

        Html(firstLine.getOrElse("") + secondLine.getOrElse("") + thirdLine.getOrElse(""))
      }
    )

  private def buildForMovementResponse(
    notification: NotificationFrontendModel
  )(implicit messages: Messages): NotificationsPageSingleElement =
    NotificationsPageSingleElement(
      title = messages(s"notifications.elem.title.inventoryLinkingMovementResponse"),
      timestampInfo = messages(
        "notifications.elem.timestampInfo.response",
        dateFormatter.format(notification.timestampReceived),
        timeFormatter.format(notification.timestampReceived)
      ),
      content = Html(notification.crcCode.map { crcCode =>
        s"<p>${messages(s"notifications.elem.content.inventoryLinkingMovementResponse.crc", decoder.crc(crcCode))}</p>"
      }.getOrElse(""))
    )

  private def buildForUnspecified(
    responseTimestamp: Instant
  )(implicit messages: Messages): NotificationsPageSingleElement =
    NotificationsPageSingleElement(
      title = messages("notifications.elem.title.unspecified"),
      timestampInfo = messages(
        "notifications.elem.timestampInfo.request",
        dateFormatter.format(responseTimestamp),
        timeFormatter.format(responseTimestamp)
      ),
      content = HtmlFormat.empty
    )

}
