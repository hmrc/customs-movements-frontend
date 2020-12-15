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

import models.UcrBlock
import play.api.data.Forms._
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class MucrOptions(newMucr: String = "", existingMucr: String = "", createOrAdd: String = MucrOptions.Create) {
  def mucr: String = createOrAdd match {
    case MucrOptions.Create => newMucr
    case MucrOptions.Add    => existingMucr
  }
}

object MucrOptions {

  val formId = "MucrOptions"

  implicit val format = Json.format[MucrOptions]

  val Create = "create"
  val Add = "add"

  def apply(ucrBlock: UcrBlock): MucrOptions =
    ucrBlock.ucrType match {
      case UcrType.Mucr.codeValue => MucrOptions("", ucrBlock.ucr, MucrOptions.Add)
      case _                      => throw new IllegalArgumentException(s"Invalid ucrType: ${ucrBlock.ucrType}")
    }

  def form2Model: (String, String, String) => MucrOptions = {
    case (createOrAdd, newMucr, existingMucr) =>
      createOrAdd match {
        case Create => MucrOptions(newMucr.toUpperCase, "", Create)
        case Add    => MucrOptions("", existingMucr.toUpperCase, Add)
      }
  }

  def model2Form: MucrOptions => Option[(String, String, String)] =
    m => Some((m.createOrAdd, m.newMucr, m.existingMucr))

  val mapping =
    Forms
      .mapping("createOrAdd" -> text().verifying("mucrOptions.createAdd.value.empty", nonEmpty), "newMucr" -> text(), "existingMucr" -> text())(
        form2Model
      )(model2Form)

  val form: Form[MucrOptions] = Form(mapping)

  def validateForm(form: Form[MucrOptions]): Form[MucrOptions] =
    if (form.value.exists(op => validMucrIgnoreCase(op.mucr))) {
      form
    } else {
      val errorField = form.value.map(_.createOrAdd) match {
        case Some(Create) => "newMucr"
        case _            => "existingMucr"
      }
      form.withError(errorField, "mucrOptions.reference.value.error")
    }
}
