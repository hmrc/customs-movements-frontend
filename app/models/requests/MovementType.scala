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

package models.requests
import play.api.libs.json._

sealed abstract class MovementType(val value: String) {
  override def toString = value
}

object MovementType {
  val allValues = Seq(Arrival, Departure)

  def apply(input: String): MovementType =
    MovementType.allValues
      .find(_.value == input)
      .getOrElse(throw new IllegalArgumentException("Incorrect movement type"))

  case object Arrival extends MovementType("EAL")

  case object Departure extends MovementType("EDL")

  implicit object MovementTypeFormat extends Format[MovementType] {
    def writes(movementType: MovementType): JsValue = JsString(movementType.value)
    def reads(jsonValue: JsValue): JsResult[MovementType] = jsonValue match {
      case JsString(choice) =>
        MovementType.allValues.find(_.value == choice).map(JsSuccess(_)).getOrElse(JsError("Incorrect movement type"))
      case _ => JsError("Incorrect movement type")
    }
  }
}
