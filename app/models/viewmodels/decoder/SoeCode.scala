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

sealed abstract class SoeCode(override val code: String, override val status: String, override val contentKey: String)
    extends CodeWithContentKey

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

  case object DeclarationValidation
      extends SoeCode(code = "1", status = "DeclarationValidation", contentKey = "decoder.soe.DeclarationValidation")
  case object DeclarationGoodsRelease
      extends SoeCode(
        code = "2",
        status = "DeclarationGoodsRelease",
        contentKey = "decoder.soe.DeclarationGoodsRelease"
      )
  case object DeclarationClearance
      extends SoeCode(code = "3", status = "DeclarationClearance", contentKey = "decoder.soe.DeclarationClearance")
  case object DeclarationInvalidated
      extends SoeCode(code = "4", status = "DeclarationInvalidated", contentKey = "decoder.soe.DeclarationInvalidated")
  case object DeclarationRejected
      extends SoeCode(code = "5", status = "DeclarationRejected", contentKey = "decoder.soe.DeclarationRejected")
  case object DeclarationHandledExternally
      extends SoeCode(
        code = "6",
        status = "DeclarationHandledExternally",
        contentKey = "decoder.soe.DeclarationHandledExternally"
      )
  case object DeclarationCorrectionValidation
      extends SoeCode(
        code = "7",
        status = "DeclarationCorrectionValidation",
        contentKey = "decoder.soe.DeclarationCorrectionValidation"
      )
  case object AdvanceDeclarationRegistration
      extends SoeCode(
        code = "8",
        status = "AdvanceDeclarationRegistration",
        contentKey = "decoder.soe.AdvanceDeclarationRegistration"
      )
  case object DeclarationAcceptance
      extends SoeCode(code = "9", status = "DeclarationAcceptance", contentKey = "decoder.soe.DeclarationAcceptance")
  case object DeclarationAcceptanceAtGoodsArrival
      extends SoeCode(
        code = "10",
        status = "DeclarationAcceptanceAtGoodsArrival",
        contentKey = "decoder.soe.DeclarationAcceptanceAtGoodsArrival"
      )
  case object DeclarationRejectionAtGoodsArrival
      extends SoeCode(
        code = "11",
        status = "DeclarationRejectionAtGoodsArrival",
        contentKey = "decoder.soe.DeclarationRejectionAtGoodsArrival"
      )
  case object DeclarationCorrected
      extends SoeCode(code = "12", status = "DeclarationCorrected", contentKey = "decoder.soe.DeclarationCorrected")
  case object DeclarationSupplemented
      extends SoeCode(
        code = "13",
        status = "DeclarationSupplemented",
        contentKey = "decoder.soe.DeclarationSupplemented"
      )
  case object DeclarationRisked
      extends SoeCode(code = "14", status = "DeclarationRisked", contentKey = "decoder.soe.DeclarationRisked")
  case object CustomsPositionDetermined
      extends SoeCode(
        code = "15",
        status = "CustomsPositionDetermined",
        contentKey = "decoder.soe.CustomsPositionDetermined"
      )
  case object DeclarationClearanceAfterGoodsRelease
      extends SoeCode(
        code = "16",
        status = "DeclarationClearanceAfterGoodsRelease",
        contentKey = "decoder.soe.DeclarationClearanceAfterGoodsRelease"
      )
  case object InsufficientGuarantees
      extends SoeCode(code = "17", status = "InsufficientGuarantees", contentKey = "decoder.soe.InsufficientGuarantees")
  case object Departed extends SoeCode(code = "D", status = "Departed", contentKey = "decoder.soe.Departed")
  case object Frustrated extends SoeCode(code = "F", status = "Frustrated", contentKey = "decoder.soe.Frustrated")

}
