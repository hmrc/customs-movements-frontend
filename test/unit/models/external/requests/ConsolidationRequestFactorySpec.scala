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

package unit.models.external.requests

import forms.{AssociateKind, AssociateUcr, DisassociateKind, DisassociateUcr}
import models.external.requests.ConsolidationRequest
import models.external.requests.ConsolidationRequestFactory._
import models.external.requests.ConsolidationType._
import unit.base.UnitSpec

class ConsolidationRequestFactorySpec extends UnitSpec {

  val mucr = "mucr"
  val mucrParent = "mucrParent"
  val ducr = "ducr"
  val associateDucr = AssociateUcr(AssociateKind.Ducr, ducr = Some(ducr), mucr = None)
  val associateMucr = AssociateUcr(AssociateKind.Mucr, ducr = None, mucr = Some(mucr))
  val disassociateDucr = DisassociateUcr(DisassociateKind.Ducr, ducr = Some(ducr), mucr = None)
  val disassociateMucr = DisassociateUcr(DisassociateKind.Mucr, ducr = None, mucr = Some(mucr))

  "Consolidation Request Factory" should {

    "build correct Association Ducr request" in {

      buildAssociationRequest(mucrParent, associateDucr) mustBe ConsolidationRequest(ASSOCIATE_DUCR, Some(mucrParent), Some(ducr))
    }

    "build correct Association Mucr request" in {

      buildAssociationRequest(mucrParent, associateMucr) mustBe ConsolidationRequest(ASSOCIATE_MUCR, Some(mucrParent), Some(mucr))
    }

    "build correct Disassociation Ducr request" in {

      buildDisassociationRequest(disassociateDucr) mustBe ConsolidationRequest(DISASSOCIATE_DUCR, None, Some(ducr))
    }
    "build correct Disassociation Mucr request" in {

      buildDisassociationRequest(disassociateMucr) mustBe ConsolidationRequest(DISASSOCIATE_MUCR, None, Some(mucr))
    }

    "build correct Shut Mucr request" in {

      val expectedShutMucrRequest = ConsolidationRequest(SHUT_MUCR, Some(mucr), None)

      buildShutMucrRequest(mucr) mustBe expectedShutMucrRequest
    }
  }
}
