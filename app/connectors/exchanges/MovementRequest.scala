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

package connectors.exchanges

import connectors.exchanges.ActionType.MovementType
import forms._
import play.api.libs.json.{Json, OFormat}

case class MovementRequest(
  eori: String,
  choice: MovementType,
  consignmentReference: ConsignmentReferences,
  movementDetails: MovementDetailsRequest,
  location: Option[Location] = None,
  transport: Option[Transport] = None
)

object MovementRequest {
  implicit val format: OFormat[MovementRequest] = Json.format[MovementRequest]
}
