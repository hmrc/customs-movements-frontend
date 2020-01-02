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

import models.viewmodels.decoder.SOECode._
import unit.base.UnitSpec

class SOECodeSpec extends UnitSpec {

  "Soe Code" should {

    "have correct amount of DUCR codes" in {

      val expectedCodesAmount = 19
      SOECode.DucrCodes.size mustBe expectedCodesAmount
    }

    "have correct amount of MUCR codes" in {

      val expectedCodesAmount = 3
      SOECode.MucrCodes.size mustBe expectedCodesAmount
    }

    "have correct list of DUCR codes" in {

      val expectedCodes = Set(
        DeclarationValidation,
        DeclarationGoodsRelease,
        DeclarationClearance,
        DeclarationInvalidated,
        DeclarationRejected,
        DeclarationHandledExternally,
        DeclarationCorrectionValidation,
        AdvanceDeclarationRegistration,
        DeclarationAcceptance,
        DeclarationAcceptanceAtGoodsArrival,
        DeclarationRejectionAtGoodsArrival,
        DeclarationCorrected,
        DeclarationSupplemented,
        DeclarationRisked,
        CustomsPositionDetermined,
        DeclarationClearanceAfterGoodsRelease,
        InsufficientGuarantees,
        Departed,
        Frustrated
      )

      SOECode.DucrCodes mustBe expectedCodes
    }

    "have correct list of MUCR codes" in {

      val expectedCodes = Set(ConsolidationOpen, ConsolidationClosedWithoutP2P, ConsolidationHasP2P)

      SOECode.MucrCodes mustBe expectedCodes
    }
  }
}
