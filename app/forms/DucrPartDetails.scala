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

import forms.DucrPartDetails._
import models.UcrBlock
import play.api.data.Forms._
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator.{isValidDucrPartIdIgnoreCase, validDucrIgnoreCase}

case class DucrPartDetails(ducr: String, ducrPartId: String) {

  def toUcrBlock: UcrBlock = {
    val ucr = s"$ducr$separator$ducrPartId"
    UcrBlock(ucr = ucr, ucrType = UcrType.DucrParts)
  }
}

object DucrPartDetails {
  implicit val format: OFormat[DucrPartDetails] = Json.format[DucrPartDetails]

  private val separator = "-"

  def apply(ucrBlock: UcrBlock): DucrPartDetails =
    if (ucrBlock.ucrType != UcrType.DucrParts.codeValue)
      throw new IllegalArgumentException(s"Cannot create DucrPartDetails instance from UcrBlock of type: [${ucrBlock.ucrType}]")
    else {
      val separatorIndex = ucrBlock.ucr.lastIndexOf(separator)
      val (ducr, ducrPartId) = ucrBlock.ucr.splitAt(separatorIndex)
      val ducrPartIdWithoutSeparator = ducrPartId.tail

      DucrPartDetails(ducr = ducr, ducrPartId = ducrPartIdWithoutSeparator)
    }

  val mapping: Mapping[DucrPartDetails] = {
    def bind(ducr: String, ducrPartId: String): DucrPartDetails = DucrPartDetails(ducr.toUpperCase, ducrPartId.toUpperCase)

    Forms.mapping(
      "ducr" -> text().verifying("ducrParts.ducr.error", validDucrIgnoreCase),
      "ducrPartId" -> text().verifying("ducrParts.ducrPartId.error", isValidDucrPartIdIgnoreCase)
    )(bind)(unapply)
  }

  def form(): Form[DucrPartDetails] = Form(mapping)

}
