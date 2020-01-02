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

package models.viewmodels.decoder

/** CRC codes mapping based on Inventory Linking Exports codes.
  * Details can be found in Exports Notifications Behaviour sheet.
  *
  * @param code the code value
  * @param messageKey messages key with related description
  */
sealed abstract class CRCCode(override val code: String, override val messageKey: String) extends CodeWithMessageKey

object CRCCode {

  val codes: Set[CRCCode] = Set(Success, PrelodgedDeclarationNotArrived, DeclarationNotArrived)

  case object Success extends CRCCode(code = "000", messageKey = "decoder.crc.Success")
  case object PrelodgedDeclarationNotArrived extends CRCCode(code = "101", messageKey = "decoder.crc.PrelodgedDeclarationNotArrived")
  case object DeclarationNotArrived extends CRCCode(code = "102", messageKey = "decoder.crc.DeclarationNotArrived")

}
