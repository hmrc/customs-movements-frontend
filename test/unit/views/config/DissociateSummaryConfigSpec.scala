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

package views.config

import views.components.config.DissociateSummaryConfig

class DissociateSummaryConfigSpec extends IleQueryFeatureConfigSpec {

  "DissociateSummaryConfig when ileQuery disabled" should {

    val config = new DissociateSummaryConfig(ileQueryDisabled)

    "return correct url" in {
      config.backUrl must be(controllers.consolidations.routes.DisassociateUcrController.displayPage())
    }
  }

  "DissociateSummaryConfig when ileQuery enabled" should {

    val config = new DissociateSummaryConfig(ileQueryEnabled)

    "return correct url when query ucr present" in {
      config.backUrl must be(controllers.routes.ChoiceController.displayChoiceForm())
    }

  }
}
