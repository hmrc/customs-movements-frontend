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

import play.api.data.{Form, Forms}
import play.api.data.Forms.text
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class AssociateDucr(ducr: String)

object AssociateDucr {
  val formId: String = "AssociateDucr"

  implicit val format = Json.format[AssociateDucr]

  val mapping =
    Forms.mapping(
      "ducr" -> text()
        .verifying("mucrOptions.reference.value.empty", nonEmpty)
        .verifying("mucrOptions.reference.value.error", isEmpty or validDucrOrMucr)
    )(AssociateDucr.apply)(AssociateDucr.unapply)

  val form: Form[AssociateDucr] = Form(mapping)
}
