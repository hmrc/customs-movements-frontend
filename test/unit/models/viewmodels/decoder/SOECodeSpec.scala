/*
 * Copyright 2023 HM Revenue & Customs
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
import models.viewmodels.decoder.SOECode._
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.FakeRequest
import views.MessagesStub
import views.spec.ViewMatchers

class SOECodeSpec extends UnitSpec with MessagesStub with ViewMatchers {

  "Soe Code" should {

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
        CompletedSuccessfully,
        Departed,
        Frustrated,
        DeclarationReceived,
        ProvisionalCustomsDebtCalculated,
        FinalCustomsDebtCalculated,
        GoodsExitResultsReceived,
        AmendedNoQuotaAllocation,
        ManualTaskRaised,
        NonExistentDeclaration,
        DeclarationUnderRisk
      )

      SOECode.DucrCodes mustBe expectedCodes
    }

    "have correct list of MUCR codes" in {
      val expectedCodes = Set(ConsolidationOpen, ConsolidationClosedWithoutP2P, ConsolidationHasP2P, ConsolidationWithEmptyMucr)

      SOECode.MucrCodes mustBe expectedCodes
    }

    implicit val request: Request[AnyContentAsEmpty.type] = FakeRequest()

    "have messages for all codes" in {
      SOECode.AllCodes.foreach(soeCode => messages must haveTranslationFor(soeCode.messageKey))
    }

    "have messages for all codes in welsh" in {
      SOECode.AllCodes.foreach(soeCode => messagesCy must haveTranslationFor(soeCode.messageKey))
    }
  }
}
