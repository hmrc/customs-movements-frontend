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

class AssociateUcrPageConfigSpec extends UnitSpec with ViewConfigFeaturesSpec {

  "AssociateUcrPageConfig when ileQuery disabled" should {

    val config = new AssociateUcrPageConfig(ileQueryDisabled)

    "return correct back url" in {
      config.backUrl mustBe controllers.consolidations.routes.MucrOptionsController.displayPage()
    }
  }

  "AssociateUcrPageConfig when ileQuery enabled" should {

    val config = new AssociateUcrPageConfig(ileQueryEnabled)

    "return correct back url" in {

      config.backUrl mustBe controllers.consolidations.routes.ManageMucrController.displayPage()
    }
  }
}
