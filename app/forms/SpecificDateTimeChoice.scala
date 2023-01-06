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

package forms

import forms.EnhancedMapping.requiredRadio
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator.isContainedIn

case class SpecificDateTimeChoice(choice: String)

object SpecificDateTimeChoice {

  implicit val format = Json.format[SpecificDateTimeChoice]

  val UserDateTime = "userDateTime"
  val CurrentDateTime = "currentDateTime"

  val allChoices = Seq(UserDateTime, CurrentDateTime)

  val mapping: Mapping[SpecificDateTimeChoice] =
    Forms.mapping(
      "choice" -> requiredRadio("specific.datetime.input.error.empty")
        .verifying("specific.datetime.input.error.incorrectValue", isContainedIn(allChoices))
    )(SpecificDateTimeChoice.apply)(SpecificDateTimeChoice.unapply)

  def form(): Form[SpecificDateTimeChoice] = Form(mapping)
}
