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

package forms

import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator.{isEmpty, nonEmpty, validMucrIgnoreCase, PredicateOpsForFunctions}

case class ShutMucr(mucr: String)

object ShutMucr {
  implicit val format: OFormat[ShutMucr] = Json.format[ShutMucr]

  val formId = "ShutMucr"

  private val form2Data = (mucr: String) => ShutMucr(mucr.trim.toUpperCase)

  val mapping = Forms.mapping(
    "mucr" -> text()
      .verifying("error.mucr.empty", nonEmpty)
      .verifying("error.mucr.format", isEmpty or validMucrIgnoreCase)
  )(form2Data)(ShutMucr.unapply)

  def form(): Form[ShutMucr] = Form(mapping)
}
