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

/** ROE codes mapping based on Inventory Linking Exports codes.
  * Details can be found in Exports Notifications Behaviour sheet.
  *
  * @param code the code value
  * @param messageKey messages key with related description
  */
sealed abstract class ROECode(override val code: String, override val messageKey: String) extends CodeWithMessageKey

object ROECode {

  val codes: Set[ROECode] = Set(
    DocumentaryControl,
    PhysicalExternalPartyControl,
    NonBlockingDocumentaryControl,
    NoControlRequired,
    RiskingNotPerformed,
    PrelodgePrefix
  )

  case object DocumentaryControl extends ROECode(code = "1", messageKey = "decoder.roe.DocumentaryControl")
  case object PhysicalExternalPartyControl
      extends ROECode(code = "2", messageKey = "decoder.roe.PhysicalExternalPartyControl")
  case object NonBlockingDocumentaryControl
      extends ROECode(code = "3", messageKey = "decoder.roe.NonBlockingDocumentaryControl")
  case object NoControlRequired extends ROECode(code = "6", messageKey = "decoder.roe.NoControlRequired")
  case object RiskingNotPerformed extends ROECode(code = "0", messageKey = "decoder.roe.RiskingNotPerformed")
  case object PrelodgePrefix extends ROECode(code = "H", messageKey = "decoder.roe.PrelodgePrefix")

}
