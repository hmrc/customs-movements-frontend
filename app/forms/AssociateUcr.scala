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

import forms.AssociateKind.{Ducr, Mucr}
import models.UcrBlock
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError, Forms, Mapping}
import play.api.libs.json._

sealed abstract class AssociateKind(val formValue: String)

object AssociateKind {
  case object Mucr extends AssociateKind("mucr")
  case object Ducr extends AssociateKind("ducr")

  private val lookup = PartialFunction[String, AssociateKind] {
    case Mucr.formValue => Mucr
    case Ducr.formValue => Ducr
  }

  implicit val formatter: Formatter[AssociateKind] = new Formatter[AssociateKind] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], AssociateKind] = {
      data.get(key).map { kind =>
        lookup.andThen(Right.apply).applyOrElse(kind, (_: String) => Left(Seq(FormError(key, "error.unknown"))))
      }
    }.getOrElse(Left(Seq(FormError(key, "error.required"))))

    override def unbind(key: String, value: AssociateKind): Map[String, String] = Map(key -> value.formValue)
  }

  implicit val format =
    Format[AssociateKind](Reads.StringReads.collect(JsonValidationError("error.unknown"))(lookup), Writes(kind => JsString(kind.formValue)))
}

case class AssociateUcr(kind: AssociateKind, ucr: String)

object AssociateUcr {

  def apply(ucrBlock: UcrBlock): AssociateUcr =
    ucrBlock.ucrType match {
      case "M" => AssociateUcr(AssociateKind.Mucr, ucrBlock.ucr)
      case "D" => AssociateUcr(AssociateKind.Ducr, ucrBlock.ucr)
      case _   => throw new IllegalArgumentException(s"Invalid ucrType: ${ucrBlock.ucrType}")
    }

  val formId: String = "AssociateDucr"

  implicit val format = Json.format[AssociateUcr]

  val mapping: Mapping[AssociateUcr] = {
    import uk.gov.voa.play.form.ConditionalMappings._
    import utils.validators.forms.FieldValidator._
    def bind(associateKind: AssociateKind, ducr: Option[String], mucr: Option[String]): AssociateUcr =
      associateKind match {
        case Ducr => AssociateUcr(Ducr, ducr.get)
        case Mucr => AssociateUcr(Mucr, mucr.get)
      }

    def unbind(value: AssociateUcr): Option[(AssociateKind, Option[String], Option[String])] =
      value.kind match {
        case Ducr => Some((value.kind, Some(value.ucr), None))
        case Mucr => Some((value.kind, None, Some(value.ucr)))
        case _    => None
      }

    Forms.mapping(
      "kind" -> of[AssociateKind],
      "ducr" -> mandatoryIfEqual("kind", Ducr.formValue, text().verifying("ducr.error.format", validDucr)),
      "mucr" -> mandatoryIfEqual("kind", Mucr.formValue, text().verifying("mucr.error.format", validMucr))
    )(bind)(unbind)
  }

  val form: Form[AssociateUcr] = Form(mapping)
}
