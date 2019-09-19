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

sealed abstract class CRCCode(override val code: String, override val contentKey: String) extends CodeWithContentKey

object CRCCode {

  val codes: Set[CRCCode] = Set(Success, PrelodgedDeclarationNotArrived, DeclarationNotArrived)

  case object Success extends CRCCode(code = "000", contentKey = "decoder.crc.Success")
  case object PrelodgedDeclarationNotArrived
      extends CRCCode(code = "101", contentKey = "decoder.crc.PrelodgedDeclarationNotArrived")
  case object DeclarationNotArrived extends CRCCode(code = "102", contentKey = "decoder.crc.DeclarationNotArrived")

}
