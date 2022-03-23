/*
 * Copyright 2022 HM Revenue & Customs
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

package connectors.exchanges

import play.api.libs.json._

abstract class ActionType(val typeName: String)

object ActionType {

  case object IleQuery extends ActionType("IleQuery")

  abstract class ConsolidationType(override val typeName: String, val ileCode: String) extends ActionType(typeName)
  object ConsolidationType {
    case object DucrAssociation extends ConsolidationType("DucrAssociation", "EAC")
    case object MucrAssociation extends ConsolidationType("MucrAssociation", "EAC")
    case object DucrPartAssociation extends ConsolidationType("DucrPartAssociation", "EAC")
    case object DucrDisassociation extends ConsolidationType("DucrDisassociation", "EAC")
    case object MucrDisassociation extends ConsolidationType("MucrDisassociation", "EAC")
    case object DucrPartDisassociation extends ConsolidationType("DucrPartDisassociation", "EAC")
    case object ShutMucr extends ConsolidationType("ShutMucr", "CST")

    val allTypes: Set[ConsolidationType] =
      Set(DucrAssociation, MucrAssociation, DucrPartAssociation, DucrDisassociation, MucrDisassociation, DucrPartDisassociation, ShutMucr)

    def existsFor(typeName: String): Boolean = this.allTypes.map(_.typeName).contains(typeName)

    implicit val format: Format[ConsolidationType] = new Format[ConsolidationType] {
      override def writes(consolidationType: ConsolidationType): JsValue = JsString(consolidationType.typeName)

      override def reads(json: JsValue): JsResult[ConsolidationType] = json match {
        case JsString("DucrAssociation")        => JsSuccess(DucrAssociation)
        case JsString("MucrAssociation")        => JsSuccess(MucrAssociation)
        case JsString("DucrPartAssociation")    => JsSuccess(DucrPartAssociation)
        case JsString("DucrDisassociation")     => JsSuccess(DucrDisassociation)
        case JsString("MucrDisassociation")     => JsSuccess(MucrDisassociation)
        case JsString("DucrPartDisassociation") => JsSuccess(DucrPartDisassociation)
        case JsString("ShutMucr")               => JsSuccess(ShutMucr)
        case unknownType                        => JsError(s"Unknown ConsolidationType: [$unknownType]")
      }
    }
  }

  abstract class MovementType(override val typeName: String, val ileCode: String) extends ActionType(typeName)
  object MovementType {
    case object Arrival extends MovementType("Arrival", "EAL")
    case object Departure extends MovementType("Departure", "EDL")

    val allTypes: Set[MovementType] = Set(Arrival, Departure)

    def existsFor(typeName: String): Boolean = this.allTypes.map(_.typeName).contains(typeName)

    implicit val format: Format[MovementType] = new Format[MovementType] {
      override def writes(movementType: MovementType): JsValue = JsString(movementType.typeName)

      override def reads(json: JsValue): JsResult[MovementType] = json match {
        case JsString("Arrival")   => JsSuccess(Arrival)
        case JsString("Departure") => JsSuccess(Departure)
        case unknownType           => JsError(s"Unknown MovementType: [$unknownType]")
      }
    }
  }

  implicit val format: Format[ActionType] = new Format[ActionType] {
    override def writes(actionType: ActionType): JsValue = actionType match {
      case movementType: MovementType           => MovementType.format.writes(movementType)
      case consolidationType: ConsolidationType => ConsolidationType.format.writes(consolidationType)
      case IleQuery                             => JsString(IleQuery.typeName)
      case other                                => JsString(other.typeName)
    }

    override def reads(json: JsValue): JsResult[ActionType] = json match {
      case JsString("IleQuery")                                  => JsSuccess(IleQuery)
      case JsString(value) if MovementType.existsFor(value)      => MovementType.format.reads(JsString(value))
      case JsString(value) if ConsolidationType.existsFor(value) => ConsolidationType.format.reads(JsString(value))
      case unknownType                                           => JsError(s"Unknown ActionType: [$unknownType]")
    }
  }

}
