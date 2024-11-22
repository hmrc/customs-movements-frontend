/*
 * Copyright 2024 HM Revenue & Customs
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

package views.components.gds

import base.Injector
import forms.{Choice, UcrType}
import models.UcrBlock
import models.requests.AuthenticatedRequest
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import testdata.CommonTestData.validEori
import testdata.MovementsTestData.newUser
import views.ViewSpec
import views.html.choice_on_consignment
import views.tags.ViewTest

@ViewTest
class GdsMainTemplateSpec extends ViewSpec with Injector {

  private val choicePage = instanceOf[choice_on_consignment]

  private implicit val request: AuthenticatedRequest[AnyContentAsEmpty.type] =
    AuthenticatedRequest(FakeRequest().withCSRFToken, newUser(validEori))

  "The Main template" should {

    "have the 'Back' link placed before the main page content" in {
      val view = choicePage(Choice.form, UcrBlock("ucr", UcrType.Ducr))

      val backLink = "govuk-back-link"
      view.getElementsByClass(backLink).size mustBe 1
      view.getElementsByTag("main").first.getElementsByClass(backLink).size mustBe 0
    }
  }
}
