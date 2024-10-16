/*
 * Copyright 2024 HM Revenue & Customs
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

import connectors.exchanges.ActionType.ConsolidationType._
import connectors.exchanges.ActionType.MovementType._
import models.UcrBlock
import models.notifications.Notification
import models.submissions.Submission
import models.viewmodels.notificationspage.converters._
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import views.helpers.ViewDates
import views.html.components.gds.paragraphBody

import javax.inject.{Inject, Singleton}

@Singleton
class NotificationPageSingleElementFactory @Inject() (responseConverterProvider: ResponseConverterProvider) {

  def build(submission: Submission, notificationsCount: Int)(implicit messages: Messages): NotificationsPageSingleElement =
    (submission.actionType: @unchecked) match {
      case Arrival | Departure | DucrDisassociation | DucrPartDisassociation | MucrAssociation | MucrDisassociation | ShutMucr =>
        buildForRequest(submission, notificationsCount)

      case DucrAssociation     => buildForDucrAssociation(submission, notificationsCount)
      case DucrPartAssociation => buildForDucrPartAssociation(submission, notificationsCount)
    }

  private def buildForRequest(submission: Submission, notificationsCount: Int)(implicit messages: Messages): NotificationsPageSingleElement = {

    val ucrMessage = if (submission.hasMucr) "MUCR" else if (submission.hasDucrPart) "DUCR Part" else "DUCR"

    val content = HtmlFormat.fill(
      List(
        paragraphBody(messages(s"notifications.elem.content.${submission.actionType.typeName}", ucrMessage)),
        if (notificationsCount < 1) paragraphBody(messages("notifications.elem.content.footer")) else HtmlFormat.empty
      )
    )

    NotificationsPageSingleElement(
      title = messages(s"notifications.elem.title.${submission.actionType.typeName}"),
      timestampInfo = ViewDates.formatDateAtTime(submission.requestTimestamp),
      content = content
    )
  }

  private def buildForDucrAssociation(submission: Submission, notificationsCount: Int)(
    implicit messages: Messages
  ): NotificationsPageSingleElement = {
    val ducrs: List[UcrBlock] = submission.ucrBlocks.filter(_.ucrType == "D").toList
    val content = HtmlFormat.fill(
      (paragraphBody(messages(s"notifications.elem.content.${submission.actionType.typeName}")) +:
        ducrs.map(block => paragraphBody(block.ucr))) ++
        (if (notificationsCount < 1) Seq(paragraphBody(messages("notifications.elem.content.footer"))) else Seq.empty)
    )

    buildForRequest(submission, notificationsCount).copy(content = content)
  }

  private def buildForDucrPartAssociation(submission: Submission, notificationsCount: Int)(
    implicit messages: Messages
  ): NotificationsPageSingleElement = {
    val ducrs: List[UcrBlock] = submission.ucrBlocks.filter(_.ucrType == "DP").toList
    val content = HtmlFormat.fill(
      (paragraphBody(messages(s"notifications.elem.content.${submission.actionType.typeName}")) +:
        ducrs.map(block => paragraphBody(block.ucr))) ++
        (if (notificationsCount < 1) Seq(paragraphBody(messages("notifications.elem.content.footer"))) else Seq.empty)
    )

    buildForRequest(submission, notificationsCount).copy(content = content)
  }

  def build(notification: Notification, submission: Submission)(implicit messages: Messages): NotificationsPageSingleElement = {
    val responseConverter = responseConverterProvider.provideResponseConverter(notification)
    responseConverter.convert(ConverterData(notification, Some(submission)))
  }
}
