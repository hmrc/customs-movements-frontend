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

package models.external.requests

import forms.DisassociateKind.{Ducr, Mucr}
import forms.DisassociateUcr
import models.external.requests.ConsolidationType.{ConsolidationType, _}
import play.api.libs.json.Json

object ConsolidationRequestFactory {

  def buildAssociationRequest(mucr: String, ducr: String): ConsolidationRequest =
    ConsolidationRequest(ASSOCIATE_DUCR, Some(mucr), Some(ducr))

  def buildDisassociationRequest(ducr: String): ConsolidationRequest =
    ConsolidationRequest(DISASSOCIATE_DUCR, None, Some(ducr))

  def buildDisassociationRequest(consolidateUcr: DisassociateUcr): ConsolidationRequest = {
    val kind = consolidateUcr.kind match {
      case Mucr => DISASSOCIATE_MUCR
      case Ducr => DISASSOCIATE_DUCR
    }
    ConsolidationRequest(kind, None, Some(consolidateUcr.ucr))
  }

  def buildShutMucrRequest(mucr: String): ConsolidationRequest =
    ConsolidationRequest(SHUT_MUCR, Some(mucr), None)
}

case class ConsolidationRequest(consolidationType: ConsolidationType, mucr: Option[String], ucr: Option[String])

object ConsolidationRequest {
  implicit val format = Json.format[ConsolidationRequest]
}
