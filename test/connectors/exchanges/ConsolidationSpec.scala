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

import connectors.exchanges.ActionType.ConsolidationType._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsSuccess, Json}

class ConsolidationSpec extends AnyWordSpec with Matchers {

  val validEori = "GB123456789"
  private val mucr = "GB/1234-12345678"
  private val ducr = "8GB123456789012-123456"

  "Consolidation Request reads" should {

    "correct read Associate Ducr request" in {
      val associateDucrJson = Json.obj("consolidationType" -> DucrAssociation.typeName, "eori" -> validEori, "mucr" -> mucr, "ucr" -> ducr)
      val expectedResult = AssociateDUCRRequest(eori = validEori, mucr = mucr, ucr = ducr)
      Consolidation.format.reads(associateDucrJson) mustBe JsSuccess(expectedResult)
    }

    "correct read Associate Mucr request" in {
      val associateMucrJson = Json.obj("consolidationType" -> MucrAssociation.typeName, "eori" -> validEori, "mucr" -> mucr, "ucr" -> mucr)
      val expectedResult = AssociateMUCRRequest(eori = validEori, mucr = mucr, ucr = mucr)
      Consolidation.format.reads(associateMucrJson) mustBe JsSuccess(expectedResult)
    }

    "correct read Associate Ducr Part request" in {
      val associateDucrPartJson = Json.obj("consolidationType" -> DucrPartAssociation.typeName, "eori" -> validEori, "mucr" -> mucr, "ucr" -> mucr)
      val expectedResult = AssociateDUCRPartRequest(eori = validEori, mucr = mucr, ucr = mucr)
      Consolidation.format.reads(associateDucrPartJson) mustBe JsSuccess(expectedResult)
    }

    "correct read Disassociate Ducr request" in {
      val disassociateDucrJson = Json.obj("consolidationType" -> DucrDisassociation.typeName, "eori" -> validEori, "ucr" -> ducr)
      val expectedResult = DisassociateDUCRRequest(eori = validEori, ucr = ducr)
      Consolidation.format.reads(disassociateDucrJson) mustBe JsSuccess(expectedResult)
    }

    "correct read Disassociate Mucr request" in {
      val disassociateMucrJson = Json.obj("consolidationType" -> MucrDisassociation.typeName, "eori" -> validEori, "ucr" -> mucr)
      val expectedResult = DisassociateMUCRRequest(eori = validEori, ucr = mucr)
      Consolidation.format.reads(disassociateMucrJson) mustBe JsSuccess(expectedResult)
    }

    "correct read Disassociate Ducr Part request" in {
      val disassociateDucrPartJson = Json.obj("consolidationType" -> DucrPartDisassociation.typeName, "eori" -> validEori, "ucr" -> ducr)
      val expectedResult = DisassociateDUCRPartRequest(eori = validEori, ucr = ducr)
      Consolidation.format.reads(disassociateDucrPartJson) mustBe JsSuccess(expectedResult)
    }

    "correct read Shut Mucr request" in {
      val shutMucrJson = Json.obj("consolidationType" -> ShutMucr.typeName, "eori" -> validEori, "mucr" -> mucr)
      val expectedResult = ShutMUCRRequest(eori = validEori, mucr = mucr)
      Consolidation.format.reads(shutMucrJson) mustBe JsSuccess(expectedResult)
    }
  }
}
