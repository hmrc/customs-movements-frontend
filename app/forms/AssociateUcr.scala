/*
 * Copyright 2021 HM Revenue & Customs
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

import forms.UcrType._
import models.UcrBlock
import play.api.data.Forms._
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json._
import uk.gov.voa.play.form.ConditionalMappings._
import utils.validators.forms.FieldValidator._

case class AssociateUcr(kind: UcrType, ucr: String)

object AssociateUcr {
  val formId: String = "AssociateDucr"

  implicit val format = Json.format[AssociateUcr]

  val mapping: Mapping[AssociateUcr] = {
    def bind(associateKind: UcrType, ducr: Option[String], mucr: Option[String]): AssociateUcr =
      associateKind match {
        case Ducr     => AssociateUcr(Ducr, ducr.get.toUpperCase)
        case DucrPart => AssociateUcr(DucrPart, ducr.get.toUpperCase)
        case Mucr     => AssociateUcr(Mucr, mucr.get.toUpperCase)
      }

    def unbind(value: AssociateUcr): Option[(UcrType, Option[String], Option[String])] =
      value.kind match {
        case Ducr | DucrPart => Some((value.kind, Some(value.ucr), None))
        case Mucr            => Some((value.kind, None, Some(value.ucr)))
        case _               => None
      }

    Forms.mapping(
      "kind" -> of[UcrType](UcrType.formatter),
      "ducr" -> mandatoryIfEqual(
        "kind",
        Ducr.formValue,
        text()
          .verifying("associate.ucr.ducr.error.empty", nonEmpty)
          .verifying("associate.ucr.ducr.error.invalid", isEmpty or validDucrIgnoreCase)
      ),
      "mucr" -> mandatoryIfEqual(
        "kind",
        Mucr.formValue,
        text()
          .verifying("associate.ucr.mucr.error.empty", nonEmpty)
          .verifying("associate.ucr.mucr.error.invalid", isEmpty or validMucrIgnoreCase)
      )
    )(bind)(unbind)
  }

  def form: Form[AssociateUcr] = Form(mapping)

  def apply(ucrBlock: UcrBlock): AssociateUcr =
    AssociateUcr(ucr = ucrBlock.ucr, kind = ucrBlock.ucrType match {
      case Mucr.codeValue     => Mucr
      case Ducr.codeValue     => Ducr
      case DucrPart.codeValue => DucrPart
      case _                  => throw new IllegalArgumentException(s"Invalid ucrType: ${ucrBlock.ucrType}")
    })
}
