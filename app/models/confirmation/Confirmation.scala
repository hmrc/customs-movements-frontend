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

package models.confirmation

import forms.ConsignmentReferences
import models.cache.JourneyType
import models.cache.JourneyType.JourneyType
import models.confirmation.FlashKeys._
import play.api.mvc.Request

case class Confirmation(journeyType: JourneyType, conversationId: String, consignmentRefs: Option[ConsignmentReferences], mucr: Option[String])

object Confirmation {

  def apply()(implicit request: Request[_]): Option[Confirmation] = {
    def extractValue(key: String): Option[String] =
      request.session.get(key)

    val consignmentReferences =
      for {
        ucr <- extractValue(UCR)
        ucrType <- extractValue(UCR_TYPE)
      } yield ConsignmentReferences(ucrType, ucr)

    for {
      journeyType <- extractValue(JOURNEY_TYPE).map(JourneyType.withName)
      conversationId <- extractValue(CONVERSATION_ID)
    } yield new Confirmation(journeyType, conversationId, consignmentReferences, extractValue(MUCR))
  }
}
