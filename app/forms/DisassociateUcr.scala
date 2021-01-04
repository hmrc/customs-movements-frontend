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

import forms.UcrType.{Ducr, DucrPart, Mucr}
import models.{ReturnToStartException, UcrBlock}
import play.api.data.Forms._
import play.api.data.{Form, Forms}
import play.api.libs.json._
import uk.gov.voa.play.form.ConditionalMappings._
import utils.validators.forms.FieldValidator._

case class DisassociateUcr(kind: UcrType, ducr: Option[String], mucr: Option[String]) {
  def ucr: String = ducr.orElse(mucr).getOrElse(throw ReturnToStartException)
}

object DisassociateUcr {
  val formId: String = "DisassociateUcr"

  implicit val format = Json.format[DisassociateUcr]

  val mapping =
    Forms.mapping(
      "kind" -> of[UcrType](UcrType.formatter),
      "ducr" -> mandatoryIfEqual(
        "kind",
        Ducr.formValue,
        text().verifying("disassociate.ucr.ducr.empty", nonEmpty).verifying("disassociate.ucr.ducr.error", isEmpty or validDucrIgnoreCase)
      ),
      "mucr" -> mandatoryIfEqual(
        "kind",
        Mucr.formValue,
        text()
          .verifying("disassociate.ucr.mucr.empty", nonEmpty)
          .verifying("disassociate.ucr.mucr.error", isEmpty or validMucrIgnoreCase)
      )
    )(form2Data)(DisassociateUcr.unapply)

  def form2Data(kind: UcrType, ducr: Option[String], mucr: Option[String]): DisassociateUcr =
    new DisassociateUcr(kind, ducr.map(_.toUpperCase), mucr.map(_.toUpperCase))

  val form: Form[DisassociateUcr] = Form(mapping)

  def apply(ucrBlock: UcrBlock): DisassociateUcr =
    ucrBlock.ucrType match {
      case Mucr.codeValue     => DisassociateUcr(Mucr, None, Some(ucrBlock.ucr))
      case Ducr.codeValue     => DisassociateUcr(Ducr, Some(ucrBlock.ucr), None)
      case DucrPart.codeValue => DisassociateUcr(DucrPart, Some(ucrBlock.ucr), None)
      case _                  => throw new IllegalArgumentException(s"Invalid ucrType: ${ucrBlock.ucrType}")
    }
}
