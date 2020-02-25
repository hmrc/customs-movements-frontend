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

package views.links

import models.UcrBlock
import unit.base.UnitSpec
import views.components.links.ChoicePageBackLink

class ChoicePageBackLinkSpec extends UnitSpec {

  val queryUcr = UcrBlock("ucr", "D")
  val ileQueryEnabled = true
  val ileQueryDisabled = false

  "ChoicePageBackLink" should {

    "return correct url when ileQuery disabled" in {
      ChoicePageBackLink.call(ileQueryDisabled, None) must be(controllers.routes.StartController.displayStartPage())
      ChoicePageBackLink.call(ileQueryDisabled, Some(queryUcr)) must be(controllers.routes.StartController.displayStartPage())
    }

    "return correct url when ileQuery enabled and query ucr present" in {
      ChoicePageBackLink.call(ileQueryEnabled, Some(queryUcr)) must be(
        controllers.ileQuery.routes.IleQueryController.getConsignmentInformation("ucr")
      )
    }

    "return correct url when ileQuery enabled and query ucr not present" in {
      ChoicePageBackLink.call(ileQueryEnabled, None) must be(controllers.ileQuery.routes.FindConsignmentController.displayQueryForm())
    }

  }
}
