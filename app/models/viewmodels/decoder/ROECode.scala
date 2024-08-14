/*
 * Copyright 2024 HM Revenue & Customs
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

import models.viewmodels.decoder.ROECode.prelodgedPrefix
import play.api.Logger
import play.api.libs.json._

/**
 * ROE codes mapping based on Inventory Linking Exports codes.
 * Details can be found in Exports Notifications Behaviour sheet.
 *
 * @param code the code value
 * @param messageKey messages key with related description
 */
sealed class ROECode(override val code: String, override val messageKey: String, val priority: Int, val prefixed: Boolean = false)
    extends Ordered[ROECode] with CodeWithMessageKey {

  def displayCode: String = if (prefixed) prelodgedPrefix + code else code
  def withPrefix = new ROECode(this.code, this.messageKey, this.priority, true)

  override def compare(that: ROECode): Int = this.priority - that.priority
}

object ROECode {

  private val logger = Logger(this.getClass)
  private[decoder] val prelodgedPrefix = "H"

  lazy val codes: Set[ROECode] =
    Set(
      DocumentaryControl,
      PhysicalExternalPartyControl,
      NonBlockingDocumentaryControl,
      NoControlRequired,
      RiskingNotPerformed,
      PrelodgePrefix,
      UnknownRoe()
    )

  case object DocumentaryControl extends ROECode(code = "1", messageKey = "decoder.roe.DocumentaryControl", priority = 2)
  case object PhysicalExternalPartyControl extends ROECode(code = "2", messageKey = "decoder.roe.PhysicalExternalPartyControl", priority = 1)
  case object NonBlockingDocumentaryControl extends ROECode(code = "3", messageKey = "decoder.roe.NonBlockingDocumentaryControl", priority = 3)
  case object NoControlRequired extends ROECode(code = "6", messageKey = "decoder.roe.NoControlRequired", priority = 6)
  case object RiskingNotPerformed extends ROECode(code = "0", messageKey = "decoder.roe.RiskingNotPerformed", priority = 4)
  case object PrelodgePrefix extends ROECode(code = "H", messageKey = "decoder.roe.PrelodgePrefix", priority = 5)
  case class UnknownRoe(override val code: String = "") extends ROECode(code = code, messageKey = "ileCode.unknown", priority = 100)
  case object NoneRoe extends ROECode(code = "", messageKey = "", priority = 101)

  private def parseCode(code: String): ROECode = {
    val prefixed = code.length == 2
    val baseCode = code.takeRight(1)

    codes.find(_.code == baseCode) match {
      case Some(roeCode) if prefixed => roeCode.withPrefix
      case Some(roeCode)             => roeCode
      case None =>
        logger.warn(s"Unknown ROE code: $code")
        UnknownRoe(code)
    }
  }

  implicit object ROECodeFormat extends Format[ROECode] {
    def reads(value: JsValue): JsResult[ROECode] = value match {
      case JsString(code) => JsSuccess(parseCode(code))
      case _              => JsSuccess(NoneRoe)
    }

    def writes(value: ROECode): JsString = JsString(value.code)
  }
}
