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

/** Inventory Linking Exports errors mapping based on Inventory Linking Exports codes.
  * Details can be found in Exports Notifications Behaviour sheet.
  *
  * @param code the code value
  * @param contentKey messages key with related description
  */
sealed abstract class ErrorCode(override val code: String, override val contentKey: String) extends CodeWithContentKey

object ErrorCode {

  val codes: Set[ErrorCode] = Set(
    InvalidUcrFormat,
    ClientIdValidationFailed,
    UcrNotAtTopOfConsolidation,
    MucrNotShutConsolidation,
    ParentMucrInSameConsolidation,
    ConsolidationNotFound,
    ConsolidationAlreadyShut,
    UcrTypeNotMatchingUcrFormat,
    DeclarationNotExist,
    UcrAlreadyAssociated,
    PriorMovementLocationDifferentThanOnDeparture,
    NoPriorArrivalFoundAtDepartureLocation,
    DeclarationsMissingP2P,
    DeclarationCancelledOrTerminated,
    UnknownDeclarationIdentifier,
    ConsolidationLevelLimitReached,
    InvalidGoodsDateTime,
    MucrNotShutDeparture,
    FutureDateTimeOverExceeded,
    UcrIsNotMucr,
    UcrNotExist,
    UcrAlreadyDisassociated,
    UcrFieldCompletionNotMatchingEacAction,
    EmptyMucr,
    InvalidExitMessage,
    LocationBasedPermissionFailed,
    InvalidGoodsLocation,
    MucrAlreadyDeparted,
    UcrRejectedUponArrival
  )

  case object InvalidUcrFormat extends ErrorCode(code = "01", contentKey = "decoder.errorCode.InvalidUcrFormat")

  case object ClientIdValidationFailed
      extends ErrorCode(code = "02", contentKey = "decoder.errorCode.ClientIdValidationFailed")

  case object UcrNotAtTopOfConsolidation
      extends ErrorCode(code = "03", contentKey = "decoder.errorCode.UcrNotAtTopOfConsolidation")

  case object MucrNotShutConsolidation
      extends ErrorCode(code = "04", contentKey = "decoder.errorCode.MucrNotShutConsolidation")

  case object ParentMucrInSameConsolidation
      extends ErrorCode(code = "05", contentKey = "decoder.errorCode.ParentMucrInSameConsolidation")

  case object ConsolidationNotFound
      extends ErrorCode(code = "06", contentKey = "decoder.errorCode.ConsolidationNotFound")

  case object ConsolidationAlreadyShut
      extends ErrorCode(code = "07", contentKey = "decoder.errorCode.ConsolidationAlreadyShut")

  case object UcrTypeNotMatchingUcrFormat
      extends ErrorCode(code = "08", contentKey = "decoder.errorCode.UcrTypeNotMatchingUcrFormat")

  case object DeclarationNotExist extends ErrorCode(code = "10", contentKey = "decoder.errorCode.DeclarationNotExist")

  case object UcrAlreadyAssociated extends ErrorCode(code = "11", contentKey = "decoder.errorCode.UcrAlreadyAssociated")

  case object PriorMovementLocationDifferentThanOnDeparture
      extends ErrorCode(code = "12", contentKey = "decoder.errorCode.PriorMovementLocationDifferentThanOnDeparture")

  case object NoPriorArrivalFoundAtDepartureLocation
      extends ErrorCode(code = "13", contentKey = "decoder.errorCode.NoPriorArrivalFoundAtDepartureLocation")

  case object DeclarationsMissingP2P
      extends ErrorCode(code = "14", contentKey = "decoder.errorCode.DeclarationsMissingP2P")

  case object DeclarationCancelledOrTerminated
      extends ErrorCode(code = "15", contentKey = "decoder.errorCode.DeclarationCancelledOrTerminated")

  case object UnknownDeclarationIdentifier
      extends ErrorCode(code = "16", contentKey = "decoder.errorCode.UnknownDeclarationIdentifier")

  case object ConsolidationLevelLimitReached
      extends ErrorCode(code = "17", contentKey = "decoder.errorCode.ConsolidationLevelLimitReached")

  case object InvalidGoodsDateTime extends ErrorCode(code = "18", contentKey = "decoder.errorCode.InvalidGoodsDateTime")

  case object MucrNotShutDeparture extends ErrorCode(code = "19", contentKey = "decoder.errorCode.MucrNotShutDeparture")

  case object FutureDateTimeOverExceeded
      extends ErrorCode(code = "20", contentKey = "decoder.errorCode.FutureDateTimeOverExceeded")

  case object UcrIsNotMucr extends ErrorCode(code = "21", contentKey = "decoder.errorCode.UcrIsNotMucr")

  case object UcrNotExist extends ErrorCode(code = "22", contentKey = "decoder.errorCode.UcrNotExist")

  case object UcrAlreadyDisassociated
      extends ErrorCode(code = "23", contentKey = "decoder.errorCode.UcrAlreadyDisassociated")

  case object UcrFieldCompletionNotMatchingEacAction
      extends ErrorCode(code = "24", contentKey = "decoder.errorCode.UcrFieldCompletionNotMatchingEacAction")

  case object EmptyMucr extends ErrorCode(code = "25", contentKey = "decoder.errorCode.EmptyMucr")

  case object InvalidExitMessage extends ErrorCode(code = "26", contentKey = "decoder.errorCode.InvalidExitMessage")

  case object LocationBasedPermissionFailed
      extends ErrorCode(code = "27", contentKey = "decoder.errorCode.LocationBasedPermissionFailed")

  case object InvalidGoodsLocation extends ErrorCode(code = "28", contentKey = "decoder.errorCode.InvalidGoodsLocation")

  case object MucrAlreadyDeparted extends ErrorCode(code = "29", contentKey = "decoder.errorCode.MucrAlreadyDeparted")

  case object UcrRejectedUponArrival
      extends ErrorCode(code = "30", contentKey = "decoder.errorCode.UcrRejectedUponArrival")

}
