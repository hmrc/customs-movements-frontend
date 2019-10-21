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

import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError, Forms}
import play.api.libs.json._
import utils.validators.forms.FieldValidator._

sealed abstract class DisassociateKind(val formValue: String)

object DisassociateKind {
  case object Mucr extends DisassociateKind("mucr")
  case object Ducr extends DisassociateKind("ducr")

  private val lookup = PartialFunction[String, DisassociateKind] {
    case Mucr.formValue => Mucr
    case Ducr.formValue => Ducr
  }

  implicit val formatter: Formatter[DisassociateKind] = new Formatter[DisassociateKind] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], DisassociateKind] = {
      data.get(key).map { kind =>
        lookup.andThen(Right.apply).applyOrElse(kind, (_: String) => Left(Seq(FormError(key, "error.unknown"))))
      }
    }.getOrElse(Left(Seq(FormError(key, "error.required"))))

    override def unbind(key: String, value: DisassociateKind): Map[String, String] = Map(key -> value.formValue)
  }

  implicit val format =
    Format[DisassociateKind](Reads.StringReads.collect(JsonValidationError("error.unknown"))(lookup), Writes(kind => JsString(kind.formValue)))
}

case class DisassociateUcr(kind: DisassociateKind, ducr: Option[String], mucr: Option[String]) {
  def ucr: String = ducr.orElse(mucr).get
}

object DisassociateUcr {
  val formId: String = "ConsolidateUcr"

  implicit val format = Json.format[DisassociateUcr]

  val mapping =
    Forms.mapping(
      "type" -> of[DisassociateKind],
      "ducr" -> optional(
        text()
          .verifying("consolidate.ucr.empty", nonEmpty)
          .verifying("consolidate.ucr.error", isEmpty or validDucr)
      ),
      "mucr" -> optional(
        text()
          .verifying("consolidate.ucr.empty", nonEmpty)
          .verifying("consolidate.ucr.error", isEmpty or validMucr)
      )
    )(DisassociateUcr.apply)(DisassociateUcr.unapply)

  val form: Form[DisassociateUcr] = Form(mapping)
}
