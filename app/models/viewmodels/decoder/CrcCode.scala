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

package models.viewmodels.decoder

sealed abstract class CrcCode(override val code: String, override val status: String, override val contentKey: String)
    extends CodeWithContentKey

object CrcCode {

  val codes: Set[CrcCode] = Set(Success, PrelodgedDeclarationNotArrived, DeclarationNotArrived)

  case object Success extends CrcCode(code = "000", status = "Success", contentKey = "decoder.crc.Success")
  case object PrelodgedDeclarationNotArrived
      extends CrcCode(
        code = "101",
        status = "PrelodgedDeclarationNotArrived",
        contentKey = "decoder.crc.PrelodgedDeclarationNotArrived"
      )
  case object DeclarationNotArrived
      extends CrcCode(code = "102", status = "DeclarationNotArrived", contentKey = "decoder.crc.DeclarationNotArrived")

}
