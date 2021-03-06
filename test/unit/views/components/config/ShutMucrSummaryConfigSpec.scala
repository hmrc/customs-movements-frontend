/*
 * Copyright 2021 HM Revenue & Customs
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

class ShutMucrSummaryConfigSpec extends UnitSpec with ViewConfigFeaturesSpec {

  "ShutMucrSummaryConfig when ileQuery disabled" should {

    val config = new ShutMucrSummaryConfig(ileQueryDisabled)

    "return correct url" in {
      config.backUrl must be(controllers.consolidations.routes.ShutMucrController.displayPage())
    }
  }

  "ShutMucrSummaryConfig when ileQuery enabled" should {

    val config = new ShutMucrSummaryConfig(ileQueryEnabled)

    "return correct url when query ucr present" in {
      config.backUrl must be(controllers.routes.ChoiceController.displayChoiceForm())
    }

  }
}
