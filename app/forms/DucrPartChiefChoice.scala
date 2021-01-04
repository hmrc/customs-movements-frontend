/*
 * Copyright 2021 HM Revenue & Customs
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

package forms

import forms.EnhancedMapping.requiredRadio
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator.isContainedIn

case class DucrPartChiefChoice(choice: String) {
  def isDucrPart = choice == DucrPartChiefChoice.IsDucrPart
}

object DucrPartChiefChoice {

  implicit val format = Json.format[DucrPartChiefChoice]

  val IsDucrPart = "ducr_part_yes"
  val NotDucrPart = "ducr_part_no"

  val allChoices = Seq(IsDucrPart, NotDucrPart)

  val mapping: Mapping[DucrPartChiefChoice] =
    Forms.mapping(
      "choice" -> requiredRadio("ducrPartChief.input.error.empty")
        .verifying("ducrPartChief.input.error.incorrectValue", isContainedIn(allChoices))
    )(DucrPartChiefChoice.apply)(DucrPartChiefChoice.unapply)

  def form(): Form[DucrPartChiefChoice] = Form(mapping)
}
