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

package models.notifications

import play.api.libs.json._

sealed abstract class ResponseType(val value: String)

object ResponseType {
  case object ControlResponse extends ResponseType("inventoryLinkingControlResponse")
  case object MovementResponse extends ResponseType("inventoryLinkingMovementResponse")
  case object MovementTotalsResponse extends ResponseType("inventoryLinkingMovementTotalsResponse")

  implicit val format = new Format[ResponseType] {
    override def writes(responseType: ResponseType): JsValue = JsString(responseType.value)

    override def reads(json: JsValue): JsResult[ResponseType] = json match {
      case JsString("inventoryLinkingControlResponse")        => JsSuccess(ControlResponse)
      case JsString("inventoryLinkingMovementResponse")       => JsSuccess(MovementResponse)
      case JsString("inventoryLinkingMovementTotalsResponse") => JsSuccess(MovementTotalsResponse)
      case _                                                  => JsError("Unknown ResponseType")
    }
  }
}
