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
import models.viewmodels.decoder.Decoder
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}

@Singleton
class NotificationPageSingleElementFactory @Inject()(decoder: Decoder) {

  private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy").withZone(ZoneId.systemDefault())
  private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

  def build(submission: SubmissionFrontendModel)(implicit messages: Messages): NotificationsPageSingleElement =
    submission.actionType match {
      case Arrival | Departure | DucrDisassociation | ShutMucr => buildForRequest(submission)
      case DucrAssociation                                     => buildForDucrAssociation(submission)
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
  )(implicit messages: Messages): NotificationsPageSingleElement = {

    val content = notification.actionCode.map { code =>
      s"<p>${messages(decoder.actionCode(code))}</p>"
    }

    NotificationsPageSingleElement(
      title = messages("notifications.elem.title.inventoryLinkingControlResponse"),
      timestampInfo = timestampInfoResponse(notification.timestampReceived),
      content = Html(content.getOrElse(""))
    )
  }

  private def buildForMovementTotalsResponse(
    notification: NotificationFrontendModel
  )(implicit messages: Messages): NotificationsPageSingleElement = {

    val crcCodeContent = notification.crcCode.map(crcCode => messages(decoder.crc(crcCode)))
    val roeContent = notification.masterRoe.map(roe => messages(decoder.roe(roe)))
    val soeContent = notification.masterSoe.map(soe => messages(decoder.soe(soe)))

    val firstLine = crcCodeContent.map { content =>
      s"<p>${messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.crc")} $content</p>"
    }
    val secondLine = roeContent.map { content =>
      s"<p>${messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.roe")} $content</p>"
    }
    val thirdLine = soeContent.map { content =>
      s"<p>${messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.soe")} $content</p>"
    }

    NotificationsPageSingleElement(
      title = messages("notifications.elem.title.inventoryLinkingMovementTotalsResponse"),
      timestampInfo = timestampInfoResponse(notification.timestampReceived),
      content = Html(firstLine.getOrElse("") + secondLine.getOrElse("") + thirdLine.getOrElse(""))
    )
  }

  private def buildForMovementResponse(
    notification: NotificationFrontendModel
  )(implicit messages: Messages): NotificationsPageSingleElement = {

    val crcCodeContent = notification.crcCode.map(crcCode => messages(decoder.crc(crcCode)))
    val content = crcCodeContent.map { content =>
      s"<p>${messages("notifications.elem.content.inventoryLinkingMovementResponse.crc")} $content</p>"
    }

    NotificationsPageSingleElement(
      title = messages("notifications.elem.title.inventoryLinkingMovementResponse"),
      timestampInfo = timestampInfoResponse(notification.timestampReceived),
      content = Html(content.getOrElse(""))
    )
  }

  private def buildForUnspecified(
    responseTimestamp: Instant
  )(implicit messages: Messages): NotificationsPageSingleElement =
    NotificationsPageSingleElement(
      title = messages("notifications.elem.title.unspecified"),
      timestampInfo = timestampInfoResponse(responseTimestamp),
      content = HtmlFormat.empty
    )

  private def timestampInfoResponse(responseTimestamp: Instant)(implicit messages: Messages): String = messages(
    "notifications.elem.timestampInfo.response",
    dateFormatter.format(responseTimestamp),
    timeFormatter.format(responseTimestamp)
  )

}
