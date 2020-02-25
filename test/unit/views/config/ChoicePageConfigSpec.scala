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

import config.AppConfig
import models.UcrBlock
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import unit.base.UnitSpec
import views.components.config.ChoicePageConfig

class ChoicePageConfigSpec extends UnitSpec with BeforeAndAfterEach {

  private val queryUcr = UcrBlock("ucr", "D")
  private val ileQueryEnabled = mock[AppConfig]
  private val ileQueryDisabled = mock[AppConfig]

  override def beforeEach() {
    when(ileQueryEnabled.ileQueryEnabled).thenReturn(true)
    when(ileQueryDisabled.ileQueryEnabled).thenReturn(false)
  }

  "ChoicePageBackLink when ileQuery disabled" should {

    val config = new ChoicePageConfig(ileQueryDisabled)

    "return correct url" in {
      config.backLink(None) must be(controllers.routes.StartController.displayStartPage())
      config.backLink(Some(queryUcr)) must be(controllers.routes.StartController.displayStartPage())
    }
  }

  "ChoicePageBackLink when ileQuery disabled" should {

    val config = new ChoicePageConfig(ileQueryEnabled)

    "return correct url when query ucr present" in {
      config.backLink(Some(queryUcr)) must be(controllers.ileQuery.routes.IleQueryController.getConsignmentInformation("ucr"))
    }

    "return correct url when query ucr not present" in {
      config.backLink(None) must be(controllers.ileQuery.routes.FindConsignmentController.displayQueryForm())
    }

  }
}
