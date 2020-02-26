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

import models.UcrBlock
import views.components.config.ChoicePageConfig

class ChoicePageConfigSpec extends IleQueryFeatureConfigSpec {

  private val queryUcr = UcrBlock("ucr", "D")

  "ChoicePageBackLink when ileQuery disabled" should {

    val config = new ChoicePageConfig(ileQueryDisabled)

    "return correct url" in {
      config.backLink(None) mustBe controllers.routes.StartController.displayStartPage()
      config.backLink(Some(queryUcr)) mustBe controllers.routes.StartController.displayStartPage()
    }

    "return information about ile query" in {

      config.isQueryEnabled mustBe false
    }
  }

  "ChoicePageBackLink when ileQuery enabled" should {

    val config = new ChoicePageConfig(ileQueryEnabled)

    "return correct url when query ucr present" in {

      config.backLink(Some(queryUcr)) mustBe controllers.ileQuery.routes.IleQueryController.getConsignmentInformation("ucr")
    }

    "return correct url when query ucr not present" in {

      config.backLink(None) mustBe controllers.ileQuery.routes.FindConsignmentController.displayQueryForm()
    }

    "return information about ile query" in {

      config.isQueryEnabled mustBe true
    }
  }
}
