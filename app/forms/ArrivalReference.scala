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

import play.api.data.{Form, Forms}
import play.api.data.Forms.{optional, text}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class ArrivalReference(reference: Option[String])

object ArrivalReference {
  val formId: String = "ArrivalReference"

  implicit val format = Json.format[ArrivalReference]

  val mapping = Forms.mapping("reference" -> optional(text().verifying("arrivalReference.error", isAlphanumeric and noLongerThan(25))))(
    ArrivalReference.apply
  )(ArrivalReference.unapply)

  val form: Form[ArrivalReference] = Form(mapping)
}
