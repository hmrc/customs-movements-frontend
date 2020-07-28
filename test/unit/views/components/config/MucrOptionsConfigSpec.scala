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
import config.AppConfig
import forms.ManageMucrChoice.AssociateAnotherMucr
import forms.{DucrPartChiefChoice, ManageMucrChoice}

class MucrOptionsConfigSpec extends UnitSpec with ViewConfigFeaturesSpec {

  private val appConfig = mock[AppConfig]

  "MucrOptionsConfig when ileQuery disabled" should {

    val config = new MucrOptionsConfig(appConfig, ileQueryDisabled)

    "return correct back url when Ducr Part not used" in {
      config.backUrl(None, None) mustBe controllers.routes.ChoiceController.displayChoiceForm()
    }

    "return correct back url when when it is a Ducr Part" in {
      config.backUrl(None, Some(DucrPartChiefChoice(DucrPartChiefChoice.IsDucrPart))) mustBe controllers.routes.DucrPartDetailsController
        .displayPage()
    }

    "return correct back url when when its not a Ducr Part" in {
      config.backUrl(None, Some(DucrPartChiefChoice(DucrPartChiefChoice.NotDucrPart))) mustBe controllers.routes.DucrPartChiefController.displayPage()
    }
  }

  "MucrOptionsConfig when ileQuery enabled" should {

    val config = new MucrOptionsConfig(appConfig, ileQueryEnabled)

    "return correct back url" when {

      "associating a queried mucr" in {
        config.backUrl(Some(ManageMucrChoice(AssociateAnotherMucr)), None) mustBe controllers.consolidations.routes.ManageMucrController.displayPage()
      }

      "associating a queried ducr" in {
        config.backUrl(None, None) mustBe controllers.routes.ChoiceController.displayChoiceForm()
      }
    }
  }
}
