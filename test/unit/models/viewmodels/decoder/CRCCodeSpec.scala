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

package models.viewmodels.decoder

import base.UnitSpec
import models.viewmodels.decoder.CRCCode.{PrelodgedDeclarationNotArrived, Success, UCRDoesNotExist}

class CRCCodeSpec extends UnitSpec {

  "Crc Code" should {

    "have correct amount of codes" in {

      val expectedCodesAmount = 3
      CRCCode.codes.size mustBe expectedCodesAmount
    }

    "have correct list of codes" in {

      val expectedCodes = Set(Success, PrelodgedDeclarationNotArrived, UCRDoesNotExist)

      CRCCode.codes mustBe expectedCodes
    }
  }
}
