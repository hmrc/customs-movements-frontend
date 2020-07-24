/*
 * Copyright 2020 HM Revenue & Customs
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
import javax.inject.{Inject, Singleton}
import models.UcrBlock
import models.notifications.Notification
import models.submissions.Submission
import models.viewmodels.notificationspage.converters._
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import views.ViewDates
import views.html.components.paragraph

@Singleton
class NotificationPageSingleElementFactory @Inject()(responseConverterProvider: ResponseConverterProvider, viewDates: ViewDates) {

  def build(submission: Submission)(implicit messages: Messages): NotificationsPageSingleElement =
    submission.actionType match {
      case Arrival | Departure | DucrDisassociation | DucrPartDisassociation | MucrAssociation | MucrDisassociation | ShutMucr =>
        buildForRequest(submission)
      case DucrAssociation | DucrPartAssociation => buildForDucrAssociation(submission)
    }

  private def buildForRequest(submission: Submission)(implicit messages: Messages): NotificationsPageSingleElement = {

    val ucrMessage = if (submission.hasMucr) "MUCR" else if (submission.hasDucrPart) "DUCR Part" else "DUCR"

    val content = HtmlFormat.fill(
      List(
        paragraph(messages(s"notifications.elem.content.${submission.actionType.typeName}", ucrMessage)),
        paragraph(messages("notifications.elem.content.footer"))
      )
    )

    NotificationsPageSingleElement(
      title = messages(s"notifications.elem.title.${submission.actionType.typeName}"),
      timestampInfo = viewDates.formatDateAtTime(submission.requestTimestamp),
      content = content
    )
  }

  private def buildForDucrAssociation(submission: Submission)(implicit messages: Messages): NotificationsPageSingleElement = {
    val ducrs: List[UcrBlock] = submission.ucrBlocks.filter(_.ucrType == "D").toList
    val content = HtmlFormat.fill(
      paragraph(messages(s"notifications.elem.content.${submission.actionType.typeName}")) +:
        ducrs.map(block => paragraph(block.ucr)) :+
        paragraph(messages("notifications.elem.content.footer"))
    )

    buildForRequest(submission).copy(content = content)
  }

  def build(notification: Notification)(implicit messages: Messages): NotificationsPageSingleElement = {
    val responseConverter = responseConverterProvider.provideResponseConverter(notification)
    responseConverter.convert(notification)
  }

}
