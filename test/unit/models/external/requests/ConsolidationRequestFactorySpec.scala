/*
 * Copyright 2020 HM Revenue & Customs
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
import testdata.CommonTestData._
import unit.base.UnitSpec

class ConsolidationRequestFactorySpec extends UnitSpec {

  val eori = validEori
  val mucr = "mucr"
  val mucrParent = "mucrParent"
  val ducr = "ducr"
  val associateDucr = AssociateUcr(AssociateKind.Ducr, ducr)
  val associateMucr = AssociateUcr(AssociateKind.Mucr, mucr)
  val disassociateDucr = DisassociateUcr(DisassociateKind.Ducr, ducr = Some(ducr), mucr = None)
  val disassociateMucr = DisassociateUcr(DisassociateKind.Mucr, ducr = None, mucr = Some(mucr))

  "Consolidation Request Factory" should {

    "build correct Association Ducr request" in {

      buildAssociationRequest(eori, mucrParent, associateDucr) mustBe ConsolidationRequest(
        consolidationType = ASSOCIATE_DUCR,
        eori = eori,
        mucr = Some(mucrParent),
        ucr = Some(ducr)
      )
    }

    "build correct Association Mucr request" in {

      buildAssociationRequest(eori, mucrParent, associateMucr) mustBe ConsolidationRequest(
        consolidationType = ASSOCIATE_MUCR,
        eori = eori,
        mucr = Some(mucrParent),
        ucr = Some(mucr)
      )
    }

    "build correct Disassociation Ducr request" in {

      buildDisassociationRequest(eori, disassociateDucr) mustBe ConsolidationRequest(
        consolidationType = DISASSOCIATE_DUCR,
        eori = eori,
        mucr = None,
        ucr = Some(ducr)
      )
    }
    "build correct Disassociation Mucr request" in {

      buildDisassociationRequest(eori, disassociateMucr) mustBe ConsolidationRequest(
        consolidationType = DISASSOCIATE_MUCR,
        eori = eori,
        mucr = None,
        ucr = Some(mucr)
      )
    }

    "build correct Shut Mucr request" in {

      val expectedShutMucrRequest = ConsolidationRequest(consolidationType = SHUT_MUCR, eori = eori, mucr = Some(mucr), ucr = None)

      buildShutMucrRequest(eori, mucr) mustBe expectedShutMucrRequest
    }
  }
}
