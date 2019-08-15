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

case class MucrOptions(mucr: String, createOrAdd: String = MucrOptions.Create)

object MucrOptions {

  val formId = "MucrOptions"

  implicit val format = Json.format[MucrOptions]

  val Create = "create"
  val Add = "add"

  def form2Model: (String, String, String) => MucrOptions = {
    case (createOrAdd, newMucr, existingMucr) =>
      createOrAdd match {
        case Create => MucrOptions(newMucr, Create)
        case Add => MucrOptions(existingMucr, Add)
      }
  }

  def model2Form: MucrOptions => Option[(String, String, String)] = m => Some((m.createOrAdd, m.mucr, m.mucr))

  val mapping =
    Forms
      .mapping(
        "createOrAdd" -> text().verifying("mucrOptions.createAdd.value.empty", nonEmpty),
        "newMucr" -> text(),
        "existingMucr" -> text()
      )(form2Model)(model2Form)

  val form: Form[MucrOptions] = Form(mapping)

  def validateForm(form:Form[MucrOptions]): Form[MucrOptions] = {
    if(form.value.exists(op => validDucrOrMucr(op.mucr))) {
      form
    } else {
      val fieldName = form.value.map(_.createOrAdd) match {
        case Some(Create) => "newMucr"
        case _ => "existingMucr"
      }
      form
        .withError(fieldName, "mucrOptions.reference.value.error")
    }
  }

}
