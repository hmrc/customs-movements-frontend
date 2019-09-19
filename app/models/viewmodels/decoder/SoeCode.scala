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

sealed abstract class SoeCode(override val code: String, override val contentKey: String) extends CodeWithContentKey

object SoeCode {

  val codes: Set[SoeCode] = Set(
    DeclarationValidation,
    DeclarationGoodsRelease,
    DeclarationClearance,
    DeclarationInvalidated,
    DeclarationRejected,
    DeclarationHandledExternally,
    DeclarationCorrectionValidation,
    AdvanceDeclarationRegistration,
    DeclarationAcceptance,
    DeclarationAcceptanceAtGoodsArrival,
    DeclarationRejectionAtGoodsArrival,
    DeclarationCorrected,
    DeclarationSupplemented,
    DeclarationRisked,
    CustomsPositionDetermined,
    DeclarationClearanceAfterGoodsRelease,
    InsufficientGuarantees,
    Departed,
    Frustrated
  )

  case object DeclarationValidation extends SoeCode(code = "1", contentKey = "decoder.soe.DeclarationValidation")

  case object DeclarationGoodsRelease extends SoeCode(code = "2", contentKey = "decoder.soe.DeclarationGoodsRelease")

  case object DeclarationClearance extends SoeCode(code = "3", contentKey = "decoder.soe.DeclarationClearance")

  case object DeclarationInvalidated extends SoeCode(code = "4", contentKey = "decoder.soe.DeclarationInvalidated")

  case object DeclarationRejected extends SoeCode(code = "5", contentKey = "decoder.soe.DeclarationRejected")

  case object DeclarationHandledExternally
      extends SoeCode(code = "6", contentKey = "decoder.soe.DeclarationHandledExternally")

  case object DeclarationCorrectionValidation
      extends SoeCode(code = "7", contentKey = "decoder.soe.DeclarationCorrectionValidation")

  case object AdvanceDeclarationRegistration
      extends SoeCode(code = "8", contentKey = "decoder.soe.AdvanceDeclarationRegistration")

  case object DeclarationAcceptance extends SoeCode(code = "9", contentKey = "decoder.soe.DeclarationAcceptance")

  case object DeclarationAcceptanceAtGoodsArrival
      extends SoeCode(code = "10", contentKey = "decoder.soe.DeclarationAcceptanceAtGoodsArrival")

  case object DeclarationRejectionAtGoodsArrival
      extends SoeCode(code = "11", contentKey = "decoder.soe.DeclarationRejectionAtGoodsArrival")

  case object DeclarationCorrected extends SoeCode(code = "12", contentKey = "decoder.soe.DeclarationCorrected")

  case object DeclarationSupplemented extends SoeCode(code = "13", contentKey = "decoder.soe.DeclarationSupplemented")

  case object DeclarationRisked extends SoeCode(code = "14", contentKey = "decoder.soe.DeclarationRisked")

  case object CustomsPositionDetermined
      extends SoeCode(code = "15", contentKey = "decoder.soe.CustomsPositionDetermined")

  case object DeclarationClearanceAfterGoodsRelease
      extends SoeCode(code = "16", contentKey = "decoder.soe.DeclarationClearanceAfterGoodsRelease")

  case object InsufficientGuarantees extends SoeCode(code = "17", contentKey = "decoder.soe.InsufficientGuarantees")

  case object Departed extends SoeCode(code = "D", contentKey = "decoder.soe.Departed")

  case object Frustrated extends SoeCode(code = "F", contentKey = "decoder.soe.Frustrated")

}
