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

/**
 * SOE codes mapping based on Inventory Linking Exports codes.
 * Details can be found in Exports Notifications Behaviour sheet.
 *
 * @param code the code value
 * @param messageKey messages key with related description
 */
sealed abstract class SOECode(override val code: String, override val messageKey: String) extends CodeWithMessageKey

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
    CompletedSuccessfully,
    Departed,
    Frustrated,
    DeclarationReceived,
    ProvisionalCustomsDebtCalculated,
    FinalCustomsDebtCalculated,
    GoodsExitResultsReceived,
    AmendedNoQuotaAllocation,
    ManualTaskRaised,
    NonExistentDeclaration,
    DeclarationUnderRisk
  )

  val MucrCodes: Set[SOECode] = Set(ConsolidationOpen, ConsolidationClosedWithoutP2P, ConsolidationHasP2P, ConsolidationWithEmptyMucr)

  val AllCodes: Set[SOECode] = MucrCodes ++ DucrCodes

  case object NonExistentDeclaration extends SOECode(code = "0", messageKey = "decoder.soe.NonExistentDeclaration")

  case object DeclarationValidation extends SOECode(code = "1", messageKey = "decoder.soe.DeclarationValidation")

  case object DeclarationGoodsRelease extends SOECode(code = "2", messageKey = "decoder.soe.DeclarationGoodsRelease")

  case object DeclarationClearance extends SOECode(code = "3", messageKey = "decoder.soe.DeclarationClearance")

  case object DeclarationInvalidated extends SOECode(code = "4", messageKey = "decoder.soe.DeclarationInvalidated")

  case object DeclarationRejected extends SOECode(code = "5", messageKey = "decoder.soe.DeclarationRejected")

  case object DeclarationHandledExternally extends SOECode(code = "6", messageKey = "decoder.soe.DeclarationHandledExternally")

  case object DeclarationCorrectionValidation extends SOECode(code = "7", messageKey = "decoder.soe.DeclarationCorrectionValidation")

  case object AdvanceDeclarationRegistration extends SOECode(code = "8", messageKey = "decoder.soe.AdvanceDeclarationRegistration")

  case object DeclarationAcceptance extends SOECode(code = "9", messageKey = "decoder.soe.DeclarationAcceptance")

  case object DeclarationAcceptanceAtGoodsArrival extends SOECode(code = "10", messageKey = "decoder.soe.DeclarationAcceptanceAtGoodsArrival")

  case object DeclarationRejectionAtGoodsArrival extends SOECode(code = "11", messageKey = "decoder.soe.DeclarationRejectionAtGoodsArrival")

  case object DeclarationCorrected extends SOECode(code = "12", messageKey = "decoder.soe.DeclarationCorrected")

  case object DeclarationSupplemented extends SOECode(code = "13", messageKey = "decoder.soe.DeclarationSupplemented")

  case object DeclarationRisked extends SOECode(code = "14", messageKey = "decoder.soe.DeclarationRisked")

  case object CustomsPositionDetermined extends SOECode(code = "15", messageKey = "decoder.soe.CustomsPositionDetermined")

  case object DeclarationClearanceAfterGoodsRelease extends SOECode(code = "16", messageKey = "decoder.soe.DeclarationClearanceAfterGoodsRelease")

  case object InsufficientGuarantees extends SOECode(code = "17", messageKey = "decoder.soe.InsufficientGuarantees")

  case object DeclarationReceived extends SOECode(code = "18", messageKey = "decoder.soe.DeclarationReceived")

  case object ProvisionalCustomsDebtCalculated extends SOECode(code = "19", messageKey = "decoder.soe.ProvisionalCustomsDebtCalculated")

  case object FinalCustomsDebtCalculated extends SOECode(code = "20", messageKey = "decoder.soe.FinalCustomsDebtCalculated")

  case object GoodsExitResultsReceived extends SOECode(code = "21", messageKey = "decoder.soe.GoodsExitResultsReceived")

  case object CompletedSuccessfully extends SOECode(code = "22", messageKey = "decoder.soe.CompletedSuccessfully")

  case object AmendedNoQuotaAllocation extends SOECode(code = "23", messageKey = "decoder.soe.AmendedNoQuotaAllocation")

  case object ManualTaskRaised extends SOECode(code = "24", messageKey = "decoder.soe.ManualTaskRaised")

  case object DeclarationUnderRisk extends SOECode(code = "C", messageKey = "decoder.soe.DeclarationUnderRisk")

  case object Departed extends SOECode(code = "D", messageKey = "decoder.soe.Departed")

  case object Frustrated extends SOECode(code = "F", messageKey = "decoder.soe.Frustrated")

  case object ConsolidationOpen extends SOECode(code = "0", messageKey = "decoder.soe.ConsolidationOpen")

  case object ConsolidationClosedWithoutP2P extends SOECode(code = "C", messageKey = "decoder.soe.ConsolidationClosedWithoutP2P")

  case object ConsolidationHasP2P extends SOECode(code = "3", messageKey = "decoder.soe.ConsolidationHasP2P")

  case object ConsolidationWithEmptyMucr extends SOECode(code = "E", messageKey = "decoder.soe.ConsolidationWithEmptyMucr")

}
