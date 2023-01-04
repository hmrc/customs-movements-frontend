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

import models.notifications.Entry
import models.viewmodels.decoder.Decoder
import play.api.i18n.Messages
import play.twirl.api.Html
import views.html.components.code_explanation

trait CommonResponseConverter {
  val decoder: Decoder

  protected def findDucrEntry(entries: Seq[Entry]): Option[Entry] = entries.find(_.ucrType.contains("D"))

  protected def buildRoeCodeExplanation(roeCode: String)(implicit messages: Messages): Option[Html] = {
    val RoeCodeHeader = messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.roe")
    val roeCodeExplanationText = decoder.roe(roeCode).map(roe => (roe.code, messages(roe.messageKey)))

    roeCodeExplanationText.map { case (code, explanation) => code_explanation(RoeCodeHeader, code, explanation) }
  }

  protected def buildSoeCodeExplanation(soeCode: String)(implicit messages: Messages): Option[Html] = {
    val SoeCodeHeader = messages("notifications.elem.content.inventoryLinkingMovementTotalsResponse.soe")
    val soeCodeExplanationText = decoder.ducrSoe(soeCode).map(soe => (soe.code, messages(soe.messageKey)))

    soeCodeExplanationText.map { case (code, explanation) => code_explanation(SoeCodeHeader, code, explanation) }
  }

  protected def buildIcsCodeExplanation(icsCode: String)(implicit messages: Messages): Option[Html] = {
    val icsCodeExplanationText = decoder.ics(icsCode).map(ics => (ics.code, messages(ics.messageKey)))

    icsCodeExplanationText.map { case (code, explanation) => code_explanation("", code, explanation) }
  }
}
