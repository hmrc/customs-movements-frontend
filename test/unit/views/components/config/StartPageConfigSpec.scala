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

package views.components.config

import base.UnitSpec
import config.ExternalServicesConfig

class StartPageConfigSpec extends UnitSpec with ViewConfigFeaturesSpec {

  private val externalServicesConfig = mock[ExternalServicesConfig]

  "StartPageConfig" should {

    "return correct url when ileQuery enabled" in {
      val config = new StartPageConfig(externalServicesConfig, ileQueryEnabled)
      config.startUrl must be(controllers.ileQuery.routes.FindConsignmentController.displayQueryForm().url)
    }

    "return correct url when ileQuery disabled" in {
      val config = new StartPageConfig(externalServicesConfig, ileQueryDisabled)
      config.startUrl must be(controllers.routes.ChoiceController.displayChoiceForm().url)
    }

  }
}
