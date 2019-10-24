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

import forms.{AssociateKind, AssociateUcr, DisassociateKind, DisassociateUcr}
import models.external.requests.ConsolidationType.{ConsolidationType, _}
import play.api.libs.json.Json

object ConsolidationRequestFactory {

  def buildAssociationRequest(eori: String, mucr: String, associateUcr: AssociateUcr): ConsolidationRequest = {
    val kind = associateUcr.kind match {
      case AssociateKind.Mucr => ASSOCIATE_MUCR
      case AssociateKind.Ducr => ASSOCIATE_DUCR
    }
    ConsolidationRequest(consolidationType = kind, eori = eori, mucr = Some(mucr), ucr = Some(associateUcr.ucr))
  }

  def buildDisassociationRequest(eori: String, consolidateUcr: DisassociateUcr): ConsolidationRequest = {
    val kind = consolidateUcr.kind match {
      case DisassociateKind.Mucr => DISASSOCIATE_MUCR
      case DisassociateKind.Ducr => DISASSOCIATE_DUCR
    }
    ConsolidationRequest(consolidationType = kind, eori = eori, mucr = None, ucr = Some(consolidateUcr.ucr))
  }

  def buildShutMucrRequest(eori: String, mucr: String): ConsolidationRequest =
    ConsolidationRequest(consolidationType = SHUT_MUCR, eori = eori, mucr = Some(mucr), ucr = None)
}

case class ConsolidationRequest(
  consolidationType: ConsolidationType,
  eori: String,
  providerId: Option[String] = None,
  mucr: Option[String],
  ucr: Option[String]
)

object ConsolidationRequest {
  implicit val format = Json.format[ConsolidationRequest]
}
