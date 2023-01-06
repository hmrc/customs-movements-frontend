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
import play.api.data.Forms._
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual
import utils.validators.forms.FieldValidator._

case class MucrOptions(createOrAdd: String, mucr: String)

object MucrOptions {

  val formId = "MucrOptions"

  implicit val format = Json.format[MucrOptions]

  val Create = "create"
  val Add = "add"

  def apply(ucrBlock: UcrBlock): MucrOptions =
    ucrBlock.ucrType match {
      case UcrType.Mucr.codeValue => MucrOptions(Add, ucrBlock.ucr)
      case _                      => throw new IllegalArgumentException(s"Invalid ucrType: ${ucrBlock.ucrType}")
    }

  val mapping = {
    def bind(creatOrAdd: String, newMucr: Option[String], existingMucr: Option[String]): MucrOptions =
      (creatOrAdd: @unchecked) match {
        case Create => MucrOptions(Create, newMucr.getOrElse("").toUpperCase)
        case Add    => MucrOptions(Add, existingMucr.getOrElse("").toUpperCase)
      }

    def unbind(value: MucrOptions): Option[(String, Option[String], Option[String])] =
      (value.createOrAdd: @unchecked) match {
        case Create => Some((Create, Some(value.mucr), None))
        case Add    => Some((Add, None, Some(value.mucr)))
      }

    Forms
      .mapping(
        "createOrAdd" -> requiredRadio("mucrOptions.error.unselected"),
        "newMucr" -> mandatoryIfEqual(
          "createOrAdd",
          Create,
          text()
            .verifying("mucrOptions.reference.value.error.empty", nonEmpty)
            .verifying("mucrOptions.reference.value.error.invalid", isEmpty or validMucrIgnoreCase)
        ),
        "existingMucr" -> mandatoryIfEqual(
          "createOrAdd",
          Add,
          text()
            .verifying("mucrOptions.reference.value.error.empty", nonEmpty)
            .verifying("mucrOptions.reference.value.error.invalid", isEmpty or validMucrIgnoreCase)
        )
      )(bind)(unbind)
  }

  val form: Form[MucrOptions] = Form(mapping)
}
