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

import forms.Mapping.requiredRadio
import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual
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

  private def form2Model: (String, Option[String], Option[String]) => ConsignmentReferences = {
    case (reference, ducrValue, mucrValue) =>
      reference match {
        case Ducr => ConsignmentReferences(Ducr, ducrValue.getOrElse(""))
        case Mucr => ConsignmentReferences(Mucr, mucrValue.getOrElse(""))
      }
  }

  private def model2Form: ConsignmentReferences => Option[(String, Option[String], Option[String])] =
    model =>
      model.reference match {
        case Ducr => Some((model.reference, Some(model.referenceValue), None))
        case Mucr => Some((model.reference, None, Some(model.referenceValue)))
        case _    => Some(model.reference, None, None)
    }

  val mapping = Forms
    .mapping(
      "reference" -> requiredRadio("consignmentReferences.reference.empty")
        .verifying("consignmentReferences.reference.error", isContainedIn(allowedReferenceAnswers)),
      "ducrValue" -> mandatoryIfEqual(
        "reference",
        Ducr,
        text()
          .verifying("consignmentReferences.reference.ducrValue.empty", nonEmpty)
          .verifying("consignmentReferences.reference.ducrValue.error", isEmpty or validDucr)
      ),
      "mucrValue" -> mandatoryIfEqual(
        "reference",
        Mucr,
        text()
          .verifying("consignmentReferences.reference.mucrValue.empty", nonEmpty)
          .verifying("consignmentReferences.reference.mucrValue.error", isEmpty or validMucr)
      )
    )(form2Model)(model2Form)

  def form(): Form[ConsignmentReferences] = Form(mapping)

}
