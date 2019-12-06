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

package connectors.exchanges

import connectors.exchanges.ConsolidationType.ConsolidationType
import play.api.libs.json._
import uk.gov.hmrc.play.json.Union

object ConsolidationType extends Enumeration {
  type ConsolidationType = Value
  val ASSOCIATE_DUCR, ASSOCIATE_MUCR, DISASSOCIATE_DUCR, DISASSOCIATE_MUCR, SHUT_MUCR = Value
  implicit val format: Format[ConsolidationType] = Format(Reads.enumNameReads(ConsolidationType), Writes.enumNameWrites)
}

case class AssociateDUCRRequest(override val eori: String, mucr: String, ucr: String) extends Consolidation {
  override val consolidationType: ConsolidationType.Value = ConsolidationType.ASSOCIATE_DUCR
}
object AssociateDUCRRequest {
  implicit val format: OFormat[AssociateDUCRRequest] = Json.format[AssociateDUCRRequest]
}

case class AssociateMUCRRequest(override val eori: String, mucr: String, ucr: String) extends Consolidation {
  override val consolidationType: ConsolidationType.Value = ConsolidationType.ASSOCIATE_MUCR
}
object AssociateMUCRRequest {
  implicit val format: OFormat[AssociateMUCRRequest] = Json.format[AssociateMUCRRequest]
}

case class DisassociateDUCRRequest(override val eori: String, ucr: String) extends Consolidation {
  override val consolidationType: ConsolidationType.Value = ConsolidationType.DISASSOCIATE_DUCR
}
object DisassociateDUCRRequest {
  implicit val format: OFormat[DisassociateDUCRRequest] = Json.format[DisassociateDUCRRequest]
}

case class DisassociateMUCRRequest(override val eori: String, ucr: String) extends Consolidation {
  override val consolidationType: ConsolidationType.Value = ConsolidationType.DISASSOCIATE_MUCR
}
object DisassociateMUCRRequest {
  implicit val format: OFormat[DisassociateMUCRRequest] = Json.format[DisassociateMUCRRequest]
}

case class ShutMUCRRequest(override val eori: String, mucr: String) extends Consolidation {
  override val consolidationType: ConsolidationType.Value = ConsolidationType.SHUT_MUCR
}
object ShutMUCRRequest {
  implicit val format: OFormat[ShutMUCRRequest] = Json.format[ShutMUCRRequest]
}

trait Consolidation {
  val consolidationType: ConsolidationType
  val eori: String
}

object Consolidation {
  implicit val format: Format[Consolidation] = Union
    .from[Consolidation](typeField = "consolidationType")
    .and[AssociateDUCRRequest](typeTag = ConsolidationType.ASSOCIATE_DUCR.toString)
    .and[AssociateMUCRRequest](typeTag = ConsolidationType.ASSOCIATE_MUCR.toString)
    .and[DisassociateDUCRRequest](typeTag = ConsolidationType.DISASSOCIATE_DUCR.toString)
    .and[DisassociateMUCRRequest](typeTag = ConsolidationType.DISASSOCIATE_MUCR.toString)
    .and[ShutMUCRRequest](typeTag = ConsolidationType.SHUT_MUCR.toString)
    .format
}
