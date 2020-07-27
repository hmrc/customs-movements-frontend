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

package views.components.config
import base.UnitSpec
import forms.DucrPartChiefChoice

class DissociateSummaryConfigSpec extends UnitSpec with ViewConfigFeaturesSpec {

  "DissociateSummaryConfig when ileQuery disabled" should {

    val config = new DissociateSummaryConfig(ileQueryDisabled)

    "return correct back url when Ducr Parts not used" in {
      config.backUrl(None) must be(controllers.consolidations.routes.DisassociateUcrController.displayPage())
    }

    "return correct back url when its not a Ducr Part" in {
      config.backUrl(Some(DucrPartChiefChoice(DucrPartChiefChoice.NotDucrPart))) must be(controllers.routes.DucrPartChiefController.displayPage())
    }

    "return correct back url when it is a Ducr Part" in {
      config.backUrl(Some(DucrPartChiefChoice(DucrPartChiefChoice.IsDucrPart))) must be(controllers.routes.DucrPartDetailsController.displayPage())
    }
  }

  "DissociateSummaryConfig when ileQuery enabled" should {

    val config = new DissociateSummaryConfig(ileQueryEnabled)

    "return correct url when Ducr Parts not used" in {
      config.backUrl(None) must be(controllers.routes.ChoiceController.displayChoiceForm())
    }

    "return same url when Ducr Parts used" in {
      config.backUrl(Some(DucrPartChiefChoice(DucrPartChiefChoice.IsDucrPart))) must be(controllers.routes.ChoiceController.displayChoiceForm())
    }

  }
}
