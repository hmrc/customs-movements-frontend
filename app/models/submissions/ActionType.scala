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

package models.submissions

import play.api.libs.json._

sealed abstract class ActionType(val value: String)

object ActionType {
  case object Arrival extends ActionType("Arrival")
  case object Departure extends ActionType("Departure")
  case object DucrAssociation extends ActionType("DucrAssociation")
  case object MucrAssociation extends ActionType("MucrAssociation")
  case object DucrDisassociation extends ActionType("DucrDisassociation")
  case object MucrDisassociation extends ActionType("MucrDisassociation")
  case object ShutMucr extends ActionType("ShutMucr")

  implicit val format = new Format[ActionType] {
    override def writes(actionType: ActionType): JsValue = JsString(actionType.value)

    override def reads(json: JsValue): JsResult[ActionType] = json match {
      case JsString("Arrival")            => JsSuccess(Arrival)
      case JsString("Departure")          => JsSuccess(Departure)
      case JsString("DucrAssociation")    => JsSuccess(DucrAssociation)
      case JsString("MucrAssociation")    => JsSuccess(MucrAssociation)
      case JsString("DucrDisassociation") => JsSuccess(DucrDisassociation)
      case JsString("MucrDisassociation") => JsSuccess(DucrDisassociation)
      case JsString("ShutMucr")           => JsSuccess(ShutMucr)
      case _                              => JsError("Unknown ActionType")
    }
  }
}
