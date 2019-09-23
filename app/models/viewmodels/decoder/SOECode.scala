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

sealed abstract class SOECode(override val code: String, override val contentKey: String) extends CodeWithContentKey

object SOECode {

  val DucrCodes: Set[SOECode] = Set(
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

  val MucrCodes: Set[SOECode] = Set(ConsolidationOpen, ConsolidationClosedWithoutP2P, ConsolidationHasP2P)

  case object DeclarationValidation extends SOECode(code = "1", contentKey = "decoder.soe.DeclarationValidation")

  case object DeclarationGoodsRelease extends SOECode(code = "2", contentKey = "decoder.soe.DeclarationGoodsRelease")

  case object DeclarationClearance extends SOECode(code = "3", contentKey = "decoder.soe.DeclarationClearance")

  case object DeclarationInvalidated extends SOECode(code = "4", contentKey = "decoder.soe.DeclarationInvalidated")

  case object DeclarationRejected extends SOECode(code = "5", contentKey = "decoder.soe.DeclarationRejected")

  case object DeclarationHandledExternally
      extends SOECode(code = "6", contentKey = "decoder.soe.DeclarationHandledExternally")

  case object DeclarationCorrectionValidation
      extends SOECode(code = "7", contentKey = "decoder.soe.DeclarationCorrectionValidation")

  case object AdvanceDeclarationRegistration
      extends SOECode(code = "8", contentKey = "decoder.soe.AdvanceDeclarationRegistration")

  case object DeclarationAcceptance extends SOECode(code = "9", contentKey = "decoder.soe.DeclarationAcceptance")

  case object DeclarationAcceptanceAtGoodsArrival
      extends SOECode(code = "10", contentKey = "decoder.soe.DeclarationAcceptanceAtGoodsArrival")

  case object DeclarationRejectionAtGoodsArrival
      extends SOECode(code = "11", contentKey = "decoder.soe.DeclarationRejectionAtGoodsArrival")

  case object DeclarationCorrected extends SOECode(code = "12", contentKey = "decoder.soe.DeclarationCorrected")

  case object DeclarationSupplemented extends SOECode(code = "13", contentKey = "decoder.soe.DeclarationSupplemented")

  case object DeclarationRisked extends SOECode(code = "14", contentKey = "decoder.soe.DeclarationRisked")

  case object CustomsPositionDetermined
      extends SOECode(code = "15", contentKey = "decoder.soe.CustomsPositionDetermined")

  case object DeclarationClearanceAfterGoodsRelease
      extends SOECode(code = "16", contentKey = "decoder.soe.DeclarationClearanceAfterGoodsRelease")

  case object InsufficientGuarantees extends SOECode(code = "17", contentKey = "decoder.soe.InsufficientGuarantees")

  case object Departed extends SOECode(code = "D", contentKey = "decoder.soe.Departed")

  case object Frustrated extends SOECode(code = "F", contentKey = "decoder.soe.Frustrated")

  case object ConsolidationOpen extends SOECode(code = "0", contentKey = "decoder.soe.ConsolidationOpen")

  case object ConsolidationClosedWithoutP2P
      extends SOECode(code = "C", contentKey = "decoder.soe.ConsolidationClosedWithoutP2P")

  case object ConsolidationHasP2P extends SOECode(code = "3", contentKey = "decoder.soe.ConsolidationHasP2P")

}
