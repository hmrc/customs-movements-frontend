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
  val ASSOCIATE_DUCR, DISASSOCIATE_DUCR, SHUT_MUCR = Value
  implicit val format: Format[ConsolidationType] = Format(Reads.enumNameReads(ConsolidationType), Writes.enumNameWrites)
}

case class AssociateUCRRequest(override val eori: String, mucr: String, ucr: String) extends Consolidation {
  override val consolidationType: ConsolidationType.Value = ConsolidationType.ASSOCIATE_DUCR
}

object AssociateUCRRequest {
  implicit val format: OFormat[AssociateUCRRequest] = Json.format[AssociateUCRRequest]
}

case class DisassociateDUCRRequest(override val eori: String, ucr: String) extends Consolidation {
  override val consolidationType: ConsolidationType.Value = ConsolidationType.DISASSOCIATE_DUCR
}

object DisassociateDUCRRequest {
  implicit val format: OFormat[DisassociateDUCRRequest] = Json.format[DisassociateDUCRRequest]
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
    .and[AssociateUCRRequest](typeTag = ConsolidationType.ASSOCIATE_DUCR.toString)
    .and[DisassociateDUCRRequest](typeTag = ConsolidationType.DISASSOCIATE_DUCR.toString)
    .and[ShutMUCRRequest](typeTag = ConsolidationType.SHUT_MUCR.toString)
    .format
}
