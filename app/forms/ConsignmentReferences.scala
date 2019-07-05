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
import play.api.data.Forms.{optional, text}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class ConsignmentReferences(eori: Option[String], reference: String, referenceValue: String)

object ConsignmentReferences {
  implicit val format = Json.format[ConsignmentReferences]

  val formId = "ConsignmentReferences"

  object AllowedReferences {
    val Ducr = "D"
    val Mucr = "M"
  }

  import AllowedReferences._

  val allowedReferenceAnswers: Seq[String] = Seq(Ducr, Mucr)

  val mapping = Forms.mapping(
    "eori" -> optional(text().verifying("consignmentReferences.eori.error", validEori)),
    "reference" -> text()
      .verifying("consignmentReferences.reference.empty", nonEmpty)
      .verifying("consignmentReferences.reference.error", isEmpty or isContainedIn(allowedReferenceAnswers)),
    "referenceValue" -> text()
      .verifying("consignmentReferences.reference.value.empty", nonEmpty)
      .verifying("consignmentReferences.reference.value.error", isEmpty or validDucrOrMucr)
  )(ConsignmentReferences.apply)(ConsignmentReferences.unapply)

  def form(): Form[ConsignmentReferences] = Form(mapping)

}
