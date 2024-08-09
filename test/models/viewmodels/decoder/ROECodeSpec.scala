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

package models.viewmodels.decoder

import base.UnitSpec
import models.viewmodels.decoder.ROECode._
import play.api.libs.json.{JsNumber, JsString, JsSuccess}

class ROECodeSpec extends UnitSpec {

  "Roe Code" should {

    "have correct amount of codes" in {

      val expectedCodesAmount = 7
      ROECode.codes.size mustBe expectedCodesAmount
    }

    "have correct list of codes" in {

      val expectedCodes =
        Set(
          DocumentaryControl,
          PhysicalExternalPartyControl,
          NonBlockingDocumentaryControl,
          NoControlRequired,
          RiskingNotPerformed,
          PrelodgePrefix,
          UnknownRoe()
        )

      ROECode.codes mustBe expectedCodes
    }

    "have correct priority" in {

      DocumentaryControl.priority mustBe 2
      PhysicalExternalPartyControl.priority mustBe 1
      NonBlockingDocumentaryControl.priority mustBe 3
      NoControlRequired.priority mustBe 6
      RiskingNotPerformed.priority mustBe 4
      PrelodgePrefix.priority mustBe 5
      UnknownRoe().priority mustBe 100
      NoneRoe.priority mustBe 101
    }

    "parse ROE Code" when {

      "code is correct" in {
        ROECode.codes.map(roe => (roe, roe.code)).map { case (roe, code) =>
          ROECode.ROECodeFormat.reads(JsString(code)) mustBe JsSuccess(roe)
        }
      }

      "code is correct with prelodged prefix" in {
        ROECode.codes.map(roe => (roe.withPrefix, prelodgedPrefix + roe.code)).map { case (roe, code) =>
          ROECode.ROECodeFormat.reads(JsString(code)).get.displayCode mustBe JsSuccess(roe).get.displayCode
        }
      }

      "code is incorrect" in {
        ROECode.ROECodeFormat.reads(JsString("incorrect")) mustBe JsSuccess(UnknownRoe("incorrect"))
      }

      "type of the code is incorrect" in {
        ROECode.ROECodeFormat.reads(JsNumber(1)) mustBe JsSuccess(NoneRoe)
      }
    }

    "write ROE to correct JsValue" in {
      ROECode.codes.map(roe => (roe, roe.code)).map { case (roe, code) =>
        ROECode.ROECodeFormat.writes(roe) mustBe JsString(code)
      }
    }
  }
}
