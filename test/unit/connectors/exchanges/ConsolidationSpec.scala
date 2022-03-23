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

import connectors.exchanges.ActionType.ConsolidationType._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsObject, JsString, JsSuccess, JsValue}

class ConsolidationSpec extends AnyWordSpec with Matchers {

  val validEori = "GB123456789"
  private val mucr = "GB/1234-12345678"
  private val ducr = "8GB123456789012-123456"

  "Consolidation Request reads" should {

    "correct read Associate Ducr request" in {

      val associateDucrJson: JsValue =
        JsObject(
          Map(
            "consolidationType" -> JsString(DucrAssociation.typeName),
            "eori" -> JsString(validEori),
            "mucr" -> JsString(mucr),
            "ucr" -> JsString(ducr)
          )
        )

      val expectedResult = AssociateDUCRRequest(eori = validEori, mucr = mucr, ucr = ducr)

      Consolidation.format.reads(associateDucrJson) mustBe JsSuccess(expectedResult)
    }

    "correct read Associate Mucr request" in {

      val associateMucrJson: JsValue =
        JsObject(
          Map(
            "consolidationType" -> JsString(MucrAssociation.typeName),
            "eori" -> JsString(validEori),
            "mucr" -> JsString(mucr),
            "ucr" -> JsString(mucr)
          )
        )

      val expectedResult = AssociateMUCRRequest(eori = validEori, mucr = mucr, ucr = mucr)

      Consolidation.format.reads(associateMucrJson) mustBe JsSuccess(expectedResult)
    }

    "correct read Associate Ducr Part request" in {

      val associateDucrPartJson: JsValue =
        JsObject(
          Map(
            "consolidationType" -> JsString(DucrPartAssociation.typeName),
            "eori" -> JsString(validEori),
            "mucr" -> JsString(mucr),
            "ucr" -> JsString(mucr)
          )
        )

      val expectedResult = AssociateDUCRPartRequest(eori = validEori, mucr = mucr, ucr = mucr)

      Consolidation.format.reads(associateDucrPartJson) mustBe JsSuccess(expectedResult)
    }

    "correct read Disassociate Ducr request" in {

      val disassociateDucrJson: JsValue =
        JsObject(Map("consolidationType" -> JsString(DucrDisassociation.typeName), "eori" -> JsString(validEori), "ucr" -> JsString(ducr)))

      val expectedResult = DisassociateDUCRRequest(eori = validEori, ucr = ducr)

      Consolidation.format.reads(disassociateDucrJson) mustBe JsSuccess(expectedResult)
    }

    "correct read Disassociate Mucr request" in {

      val disassociateMucrJson: JsValue =
        JsObject(Map("consolidationType" -> JsString(MucrDisassociation.typeName), "eori" -> JsString(validEori), "ucr" -> JsString(mucr)))

      val expectedResult = DisassociateMUCRRequest(eori = validEori, ucr = mucr)

      Consolidation.format.reads(disassociateMucrJson) mustBe JsSuccess(expectedResult)
    }

    "correct read Disassociate Ducr Part request" in {

      val disassociateDucrPartJson: JsValue =
        JsObject(Map("consolidationType" -> JsString(DucrPartDisassociation.typeName), "eori" -> JsString(validEori), "ucr" -> JsString(ducr)))

      val expectedResult = DisassociateDUCRPartRequest(eori = validEori, ucr = ducr)

      Consolidation.format.reads(disassociateDucrPartJson) mustBe JsSuccess(expectedResult)
    }

    "correct read Shut Mucr request" in {

      val shutMucrJson: JsValue =
        JsObject(Map("consolidationType" -> JsString(ShutMucr.typeName), "eori" -> JsString(validEori), "mucr" -> JsString(mucr)))

      val expectedResult = ShutMUCRRequest(eori = validEori, mucr = mucr)

      Consolidation.format.reads(shutMucrJson) mustBe JsSuccess(expectedResult)
    }
  }
}
