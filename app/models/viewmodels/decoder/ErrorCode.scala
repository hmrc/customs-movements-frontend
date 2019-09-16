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

sealed abstract class ErrorCode(override val code: String, override val status: String, override val contentKey: String)
    extends CodeWithContentKey

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

  case object InvalidUcrFormat
      extends ErrorCode(code = "01", status = "InvalidUcrFormat", contentKey = "decoder.errorCode.InvalidUcrFormat")
  case object ClientIdValidationFailed
      extends ErrorCode(
        code = "02",
        status = "ClientIdValidationFailed",
        contentKey = "decoder.errorCode.ClientIdValidationFailed"
      )
  case object UcrNotAtTopOfConsolidation
      extends ErrorCode(
        code = "03",
        status = "UcrNotAtTopOfConsolidation",
        contentKey = "decoder.errorCode.UcrNotAtTopOfConsolidation"
      )
  case object MucrNotShutConsolidation
      extends ErrorCode(
        code = "04",
        status = "MucrNotShutConsolidation",
        contentKey = "decoder.errorCode.MucrNotShutConsolidation"
      )
  case object ParentMucrInSameConsolidation
      extends ErrorCode(
        code = "05",
        status = "ParentMucrInSameConsolidation",
        contentKey = "decoder.errorCode.ParentMucrInSameConsolidation"
      )
  case object ConsolidationNotFound
      extends ErrorCode(
        code = "06",
        status = "ConsolidationNotFound",
        contentKey = "decoder.errorCode.ConsolidationNotFound"
      )
  case object ConsolidationAlreadyShut
      extends ErrorCode(
        code = "07",
        status = "ConsolidationAlreadyShut",
        contentKey = "decoder.errorCode.ConsolidationAlreadyShut"
      )
  case object UcrTypeNotMatchingUcrFormat
      extends ErrorCode(
        code = "08",
        status = "UcrTypeNotMatchingUcrFormat",
        contentKey = "decoder.errorCode.UcrTypeNotMatchingUcrFormat"
      )
  case object DeclarationNotExist
      extends ErrorCode(
        code = "10",
        status = "DeclarationNotExist",
        contentKey = "decoder.errorCode.DeclarationNotExist"
      )
  case object UcrAlreadyAssociated
      extends ErrorCode(
        code = "11",
        status = "UcrAlreadyAssociated",
        contentKey = "decoder.errorCode.UcrAlreadyAssociated"
      )
  case object PriorMovementLocationDifferentThanOnDeparture
      extends ErrorCode(
        code = "12",
        status = "PriorMovementLocationDifferentThanOnDeparture",
        contentKey = "decoder.errorCode.PriorMovementLocationDifferentThanOnDeparture"
      )
  case object NoPriorArrivalFoundAtDepartureLocation
      extends ErrorCode(
        code = "13",
        status = "NoPriorArrivalFoundAtDepartureLocation",
        contentKey = "decoder.errorCode.NoPriorArrivalFoundAtDepartureLocation"
      )
  case object DeclarationsMissingP2P
      extends ErrorCode(
        code = "14",
        status = "DeclarationsMissingP2P",
        contentKey = "decoder.errorCode.DeclarationsMissingP2P"
      )
  case object DeclarationCancelledOrTerminated
      extends ErrorCode(
        code = "15",
        status = "DeclarationCancelledOrTerminated",
        contentKey = "decoder.errorCode.DeclarationCancelledOrTerminated"
      )
  case object UnknownDeclarationIdentifier
      extends ErrorCode(
        code = "16",
        status = "UnknownDeclarationIdentifier",
        contentKey = "decoder.errorCode.UnknownDeclarationIdentifier"
      )
  case object ConsolidationLevelLimitReached
      extends ErrorCode(
        code = "17",
        status = "ConsolidationLevelLimitReached",
        contentKey = "decoder.errorCode.ConsolidationLevelLimitReached"
      )
  case object InvalidGoodsDateTime
      extends ErrorCode(
        code = "18",
        status = "InvalidGoodsDateTime",
        contentKey = "decoder.errorCode.InvalidGoodsDateTime"
      )
  case object MucrNotShutDeparture
      extends ErrorCode(
        code = "19",
        status = "MucrNotShutDeparture",
        contentKey = "decoder.errorCode.MucrNotShutDeparture"
      )
  case object FutureDateTimeOverExceeded
      extends ErrorCode(
        code = "20",
        status = "FutureDateTimeOverExceeded",
        contentKey = "decoder.errorCode.FutureDateTimeOverExceeded"
      )
  case object UcrIsNotMucr
      extends ErrorCode(code = "21", status = "UcrIsNotMucr", contentKey = "decoder.errorCode.UcrIsNotMucr")
  case object UcrNotExist
      extends ErrorCode(code = "22", status = "UcrNotExist", contentKey = "decoder.errorCode.UcrNotExist")
  case object UcrAlreadyDisassociated
      extends ErrorCode(
        code = "23",
        status = "UcrAlreadyDisassociated",
        contentKey = "decoder.errorCode.UcrAlreadyDisassociated"
      )
  case object UcrFieldCompletionNotMatchingEacAction
      extends ErrorCode(
        code = "24",
        status = "UcrFieldCompletionNotMatchingEacAction",
        contentKey = "decoder.errorCode.UcrFieldCompletionNotMatchingEacAction"
      )
  case object EmptyMucr extends ErrorCode(code = "25", status = "EmptyMucr", contentKey = "decoder.errorCode.EmptyMucr")
  case object InvalidExitMessage
      extends ErrorCode(code = "26", status = "InvalidExitMessage", contentKey = "decoder.errorCode.InvalidExitMessage")
  case object LocationBasedPermissionFailed
      extends ErrorCode(
        code = "27",
        status = "LocationBasedPermissionFailed",
        contentKey = "decoder.errorCode.LocationBasedPermissionFailed"
      )
  case object InvalidGoodsLocation
      extends ErrorCode(
        code = "28",
        status = "InvalidGoodsLocation",
        contentKey = "decoder.errorCode.InvalidGoodsLocation"
      )
  case object MucrAlreadyDeparted
      extends ErrorCode(
        code = "29",
        status = "MucrAlreadyDeparted",
        contentKey = "decoder.errorCode.MucrAlreadyDeparted"
      )
  case object UcrRejectedUponArrival
      extends ErrorCode(
        code = "30",
        status = "UcrRejectedUponArrival",
        contentKey = "decoder.errorCode.UcrRejectedUponArrival"
      )

}
