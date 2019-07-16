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

package forms

import play.api.data.Forms._
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class MucrOptions(mucr: String)

object MucrOptions {

  val formId = "MucrOptions"

  implicit val format = Json.format[MucrOptions]

  val Create = "create"
  val Add = "add"

  def form2Model: (String, String) => MucrOptions = {
    case (newMucr, existingMucr) => MucrOptions(newMucr + existingMucr)
  }

  def model2Form: MucrOptions => Option[(String, String)] = m => Some((m.mucr, m.mucr))

  val mapping =
    Forms
      .mapping(
        "newMucr" -> text().verifying("mucrOptions.reference.value.error", isEmpty or validDucrOrMucr),
        "existingMucr" -> text().verifying("mucrOptions.reference.value.error", isEmpty or validDucrOrMucr)
      )(form2Model)(model2Form)
      .verifying("mucrOptions.reference.value.empty", _.mucr.nonEmpty)
      .verifying("mucrOptions.reference.value.error", options => validDucrOrMucr(options.mucr))

  val form: Form[MucrOptions] = Form(mapping)

}
