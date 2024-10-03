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

import connectors.exchanges.ActionType.ConsolidationType
import play.api.libs.json._
import uk.gov.hmrc.play.json.Union

trait Consolidation {
  val consolidationType: ConsolidationType
  val eori: String
}

object Consolidation {
  implicit val associateDucrFormat: OFormat[AssociateDUCRRequest] = Json.format[AssociateDUCRRequest]
  implicit val associateMucrFormat: OFormat[AssociateMUCRRequest] = Json.format[AssociateMUCRRequest]
  implicit val disassociateDucrFormat: OFormat[DisassociateDUCRRequest] = Json.format[DisassociateDUCRRequest]
  implicit val disassociateMucrFormat: OFormat[DisassociateMUCRRequest] = Json.format[DisassociateMUCRRequest]
  implicit val shutMucrFormat: OFormat[ShutMUCRRequest] = Json.format[ShutMUCRRequest]

  implicit val format: Format[Consolidation] = Union
    .from[Consolidation](typeField = "consolidationType")
    .and[AssociateDUCRRequest](typeTag = ConsolidationType.DucrAssociation.typeName)
    .and[AssociateMUCRRequest](typeTag = ConsolidationType.MucrAssociation.typeName)
    .and[DisassociateDUCRRequest](typeTag = ConsolidationType.DucrDisassociation.typeName)
    .and[DisassociateMUCRRequest](typeTag = ConsolidationType.MucrDisassociation.typeName)
    .and[ShutMUCRRequest](typeTag = ConsolidationType.ShutMucr.typeName)
    .format
}

case class AssociateDUCRRequest(override val eori: String, mucr: String, ucr: String) extends Consolidation {
  override val consolidationType: ConsolidationType = ConsolidationType.DucrAssociation
}

case class AssociateMUCRRequest(override val eori: String, mucr: String, ucr: String) extends Consolidation {
  override val consolidationType: ConsolidationType = ConsolidationType.MucrAssociation
}

case class DisassociateDUCRRequest(override val eori: String, ucr: String) extends Consolidation {
  override val consolidationType: ConsolidationType = ConsolidationType.DucrDisassociation
}

case class DisassociateMUCRRequest(override val eori: String, ucr: String) extends Consolidation {
  override val consolidationType: ConsolidationType = ConsolidationType.MucrDisassociation
}

case class ShutMUCRRequest(override val eori: String, mucr: String) extends Consolidation {
  override val consolidationType: ConsolidationType = ConsolidationType.ShutMucr
}
