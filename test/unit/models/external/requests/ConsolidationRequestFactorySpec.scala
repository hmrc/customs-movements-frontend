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

import models.external.requests.ConsolidationRequest
import models.external.requests.ConsolidationRequestFactory._
import unit.base.UnitSpec

class ConsolidationRequestFactorySpec extends UnitSpec {

  val mucr = "mucr"
  val ducr = "ducr"

  "Consolidation Request Factory" should {

    "build correct Association request" in {

      val expectedAssociationRequest = ConsolidationRequest("associateDucr", Some(mucr), Some(ducr))

      buildAssociationRequest(mucr, ducr) mustBe expectedAssociationRequest
    }

    "build correct Disassociation request" in {

      val expectedDisassociateRequest = ConsolidationRequest("disassociateDucr", None, Some(ducr))

      buildDisassociationRequest(ducr) mustBe expectedDisassociateRequest
    }

    "build correct Shut Mucr request" in {

      val expectedShutMucrRequest = ConsolidationRequest("shutMucr", Some(mucr), None)

      buildShutMucrRequest(mucr) mustBe expectedShutMucrRequest
    }
  }
}
