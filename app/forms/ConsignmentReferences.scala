/*
 * Copyright 2023 HM Revenue & Customs
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

import forms.EnhancedMapping.requiredRadio
import models.UcrBlock
import models.cache.JourneyType.JourneyType
import play.api.data.Forms.text
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.Json
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual
import utils.validators.forms.FieldValidator._

case class ConsignmentReferences(reference: String, referenceValue: String) {

  def is(ucrType: UcrType): Boolean = this.reference.equals(ucrType.codeValue)
}

object ConsignmentReferences {
  implicit val format = Json.format[ConsignmentReferences]

  val formId = "ConsignmentReferences"

  def apply(reference: UcrType, referenceValue: String): ConsignmentReferences =
    new ConsignmentReferences(reference.codeValue, referenceValue)

  def apply(ucrBlock: UcrBlock): ConsignmentReferences =
    new ConsignmentReferences(ucrBlock.ucrType, ucrBlock.ucr)

  import UcrType._

  private val allowedReferenceAnswers: Seq[String] = Seq(Ducr, Mucr).map(_.codeValue)

  private def form2Model: (String, Option[String], Option[String]) => ConsignmentReferences = { case (reference, ducrValue, mucrValue) =>
    (reference: @unchecked) match {
      case Ducr.codeValue => ConsignmentReferences(Ducr, ducrValue.map(_.toUpperCase).getOrElse(""))
      case Mucr.codeValue => ConsignmentReferences(Mucr, mucrValue.map(_.toUpperCase).getOrElse(""))
    }
  }

  private def model2Form: ConsignmentReferences => Option[(String, Option[String], Option[String])] =
    model =>
      model.reference match {
        case Ducr.codeValue => Some((model.reference, Some(model.referenceValue), None))
        case Mucr.codeValue => Some((model.reference, None, Some(model.referenceValue)))
        case _              => Some((model.reference, None, None))
      }

  def mapping(goodsDirection: JourneyType): Mapping[ConsignmentReferences] =
    Forms
      .mapping(
        "reference" -> requiredRadio(s"consignment.references.empty.${goodsDirection.toString.toLowerCase}")
          .verifying("consignment.references.error", isContainedIn(allowedReferenceAnswers)),
        "ducrValue" -> mandatoryIfEqual(
          "reference",
          Ducr.codeValue,
          text()
            .verifying("consignment.references.ducrValue.empty", nonEmpty)
            .verifying("consignment.references.ducrValue.error", isEmpty or validDucrIgnoreCase)
        ),
        "mucrValue" -> mandatoryIfEqual(
          "reference",
          Mucr.codeValue,
          text()
            .verifying("consignment.references.mucrValue.empty", nonEmpty)
            .verifying("consignment.references.mucrValue.error", isEmpty or validMucrIgnoreCase)
        )
      )(form2Model)(model2Form)

  def form(goodsDirection: JourneyType): Form[ConsignmentReferences] = Form(mapping(goodsDirection))
}
