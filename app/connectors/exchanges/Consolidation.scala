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

import connectors.exchanges.ActionType.ConsolidationType
import play.api.libs.json._
import uk.gov.hmrc.play.json.Union

trait Consolidation {
  val consolidationType: ConsolidationType
  val eori: String
}

object Consolidation {
  implicit val associateDucrFormat = Json.format[AssociateDUCRRequest]
  implicit val associateMucrFormat = Json.format[AssociateMUCRRequest]
  implicit val associateDucrPartFormat = Json.format[AssociateDUCRPartRequest]
  implicit val disassociateDucrFormat = Json.format[DisassociateDUCRRequest]
  implicit val disassociateMucrFormat = Json.format[DisassociateMUCRRequest]
  implicit val disassociateDucrPartFormat = Json.format[DisassociateDUCRPartRequest]
  implicit val shutMucrFormat = Json.format[ShutMUCRRequest]

  implicit val format: Format[Consolidation] = Union
    .from[Consolidation](typeField = "consolidationType")
    .and[AssociateDUCRRequest](typeTag = ConsolidationType.DucrAssociation.typeName)
    .and[AssociateMUCRRequest](typeTag = ConsolidationType.MucrAssociation.typeName)
    .and[AssociateDUCRPartRequest](typeTag = ConsolidationType.DucrPartAssociation.typeName)
    .and[DisassociateDUCRRequest](typeTag = ConsolidationType.DucrDisassociation.typeName)
    .and[DisassociateMUCRRequest](typeTag = ConsolidationType.MucrDisassociation.typeName)
    .and[DisassociateDUCRPartRequest](typeTag = ConsolidationType.DucrPartDisassociation.typeName)
    .and[ShutMUCRRequest](typeTag = ConsolidationType.ShutMucr.typeName)
    .format
}

case class AssociateDUCRRequest(override val eori: String, mucr: String, ucr: String) extends Consolidation {
  override val consolidationType: ConsolidationType = ConsolidationType.DucrAssociation
}

case class AssociateMUCRRequest(override val eori: String, mucr: String, ucr: String) extends Consolidation {
  override val consolidationType: ConsolidationType = ConsolidationType.MucrAssociation
}

case class AssociateDUCRPartRequest(override val eori: String, mucr: String, ucr: String) extends Consolidation {
  override val consolidationType: ConsolidationType = ConsolidationType.DucrPartDisassociation
}

case class DisassociateDUCRRequest(override val eori: String, ucr: String) extends Consolidation {
  override val consolidationType: ConsolidationType = ConsolidationType.DucrDisassociation
}

case class DisassociateMUCRRequest(override val eori: String, ucr: String) extends Consolidation {
  override val consolidationType: ConsolidationType = ConsolidationType.MucrDisassociation
}

case class DisassociateDUCRPartRequest(override val eori: String, ucr: String) extends Consolidation {
  override val consolidationType: ConsolidationType = ConsolidationType.DucrPartDisassociation
}

case class ShutMUCRRequest(override val eori: String, mucr: String) extends Consolidation {
  override val consolidationType: ConsolidationType = ConsolidationType.ShutMucr
}
