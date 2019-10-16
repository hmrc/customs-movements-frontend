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
import play.api.data.{Form, FormError, Forms}
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Request}
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

  private def form2Model: (String, String, String) => ConsignmentReferences = {
    case (reference, ducrValue, mucrValue) =>
      reference match {
        case Ducr => ConsignmentReferences(Ducr, ducrValue)
        case Mucr => ConsignmentReferences(Mucr, mucrValue)
      }
  }

  private def model2Form: ConsignmentReferences => Option[(String, String, String)] =
    model =>
      if (model.reference == Ducr)
        Some((model.reference, model.referenceValue, ""))
      else
        Some((model.reference, "", model.referenceValue))

  val mapping = Forms
    .mapping(
      "reference" -> requiredRadio("consignmentReferences.reference.empty")
        .verifying("consignmentReferences.reference.error", isContainedIn(allowedReferenceAnswers)),
      "ducrValue" -> text(),
      "mucrValue" -> text()
    )(form2Model)(model2Form)

  def form(): Form[ConsignmentReferences] = Form(mapping)

}

object ConsignmentReferencesForm {

  def bindFromRequest(implicit request: Request[AnyContent]): Form[ConsignmentReferences] = {
    val baseForm: Form[ConsignmentReferences] = ConsignmentReferences.form().bindFromRequest

    if (baseForm.errors.nonEmpty) {
      baseForm
    } else {
      baseForm.value
        .flatMap(
          form =>
            form.reference match {
              case Ducr => validReference(form.referenceValue, "ducrValue", validDucr)
              case Mucr => validReference(form.referenceValue, "mucrValue", validMucr)
          }
        )
        .map(error => baseForm.withError(error))
        .getOrElse(baseForm)
    }
  }

  private def validReference(value: String, field: String, validator: String => Boolean): Option[FormError] =
    if (isEmpty(value))
      Some(FormError(s"$field", s"consignmentReferences.reference.$field.empty"))
    else if (!validator(value)) Some(FormError(field, s"consignmentReferences.reference.$field.error"))
    else None
}
