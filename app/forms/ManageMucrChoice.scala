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

package forms
import forms.EnhancedMapping.requiredRadio
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator.isContainedIn

case class ManageMucrChoice(choice: String)

object ManageMucrChoice {

  implicit val format = Json.format[ManageMucrChoice]

  val AssociateThisMucr = "thisMucr"
  val AssociateAnotherMucr = "anotherMucr"

  val allChoices = Seq(AssociateThisMucr, AssociateAnotherMucr)

  val mapping: Mapping[ManageMucrChoice] =
    Forms.mapping(
      "choice" -> requiredRadio("manageMucr.input.error.empty")
        .verifying("manageMucr.input.error.incorrectValue", isContainedIn(allChoices))
    )(ManageMucrChoice.apply)(ManageMucrChoice.unapply)

  def form(): Form[ManageMucrChoice] = Form(mapping)
}
