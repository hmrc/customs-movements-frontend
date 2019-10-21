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

import forms.AssociateKind.{Ducr, Mucr}
import play.api.data.Forms._
import play.api.data.format.{Formats, Formatter}
import play.api.data.{Form, FormError, Forms}
import play.api.libs.json.{Format, JsString, Json, JsonValidationError, Reads, Writes}
import utils.validators.forms.FieldValidator._

sealed abstract class AssociateKind(val formValue: String)

object AssociateKind {
  case object Mucr extends AssociateKind("mucr")
  case object Ducr extends AssociateKind("ducr")

  private val lookup = PartialFunction[String, AssociateKind]{
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

  implicit val format = Format[AssociateKind](
    Reads.StringReads.collect(JsonValidationError("error.unknown"))(lookup),
    Writes(kind => JsString(kind.formValue))
  )
}

case class AssociateUcr(kind: AssociateKind, ducr: Option[String], mucr: Option[String]) {
  def ucr: String = ducr.orElse(mucr).get
}

object AssociateUcr {
  val formId: String = "AssociateDucr"

  implicit val format = Json.format[AssociateUcr]

  val mapping =
    Forms.mapping(
      "kind" -> of[AssociateKind],
      "ducr" -> optional(
        text()
          .verifying("associate.ducr.empty", nonEmpty)
          .verifying("associate.ducr.error", isEmpty or validDucr)
      ),
      "mucr" -> optional(text()
        .verifying("associate.mucr.empty", nonEmpty)
        .verifying("associate.mucr.error", isEmpty or validMucr)
      )
    )(AssociateUcr.apply)(AssociateUcr.unapply)

  val form: Form[AssociateUcr] = Form(mapping)
}
