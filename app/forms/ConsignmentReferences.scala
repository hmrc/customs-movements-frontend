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

import forms.ConsignmentReferences.AllowedReferences.{Ducr, Mucr}
import forms.Mapping.requiredRadio
import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class ConsignmentReferences(reference: String, referenceValue: String)

object ConsignmentReferences {
  implicit val format = Json.format[ConsignmentReferences]

  val formId = "ConsignmentReferences"

  object AllowedReferences {
    val Ducr = "D"
    val Mucr = "M"
  }

  import AllowedReferences._

  val allowedReferenceAnswers: Seq[String] = Seq(Ducr, Mucr)

  val mapping = Forms
    .mapping(
      "reference" -> requiredRadio("consignmentReferences.reference.empty")
        .verifying("consignmentReferences.reference.error", isContainedIn(allowedReferenceAnswers)),
      "referenceValue" -> text()
        .verifying("consignmentReferences.reference.value.empty", nonEmpty)
    )(ConsignmentReferences.apply)(ConsignmentReferences.unapply)

  def form(): Form[ConsignmentReferences] = Form(mapping)

}

object ConsignmentReferencesForm {

  def bindFromRequest(implicit request: play.api.mvc.Request[_]) = {
    val baseForm = ConsignmentReferences.form().bindFromRequest

    if (baseForm.errors.nonEmpty) {
      baseForm
    } else {
      baseForm.value
        .map(
          reference =>
            if (referenceValid(reference)) { baseForm } else {
              baseForm.withError("referenceValue", "consignmentReferences.reference.value.error")
          }
        )
        .getOrElse(baseForm)
    }
  }

  private def referenceValid(ref: ConsignmentReferences): Boolean =
    ref.reference match {
      case Ducr => validDucr(ref.referenceValue)
      case Mucr => validMucr(ref.referenceValue)
      case _    => false
    }
}
