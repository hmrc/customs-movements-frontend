/*
 * Copyright 2019 HM Revenue & Customs
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

package views

import controllers.consolidations.routes
import forms.ShutMucr
import helpers.views.CommonMessages
import play.twirl.api.Html
import testdata.ConsolidationTestData.validMucr
import views.html.shut_mucr_summary
import views.spec.UnitViewSpec

class ShutMucrSummaryViewSpec extends UnitViewSpec with CommonMessages {

  private val shutMucrSummaryPage = new shut_mucr_summary(mainTemplate)
  private val view: Html = shutMucrSummaryPage(ShutMucr(validMucr))(request, messages)

  "Shut Mucr Summary View" should {

    "have proper labels for messages" in {

      val messages = messagesApi.preferred(request)

      messages must haveTranslationFor("shutMucr.summary.title")
      messages must haveTranslationFor("shutMucr.summary.header")
      messages must haveTranslationFor("shutMucr.summary.type")
    }

    "display page title" in {

      view.getElementById("title").text() mustBe messages("shutMucr.summary.title")
    }

    "display page header" in {

      view.getElementById("shutMucr-header").text() mustBe messages("shutMucr.summary.header")
    }

    "display MUCR type in table row" in {

      view.getElementById("shutMucr-type").text() mustBe messages("shutMucr.summary.type")
    }

    "display correct mucr" in {

      view.getElementById("shutMucr-mucr").text() mustBe messages(validMucr)
    }

    "display correct change button" in {

      val changeButton = view.getElementById("shutMucr-change")

      changeButton must haveHref(routes.ShutMucrController.displayPage())
      changeButton.text() mustBe messages("site.edit")
    }

    "display correct submit button" in {

      val submitButton = view.getElementById("submit")

      submitButton.text() mustBe messages("site.confirmAndSubmit")
    }
  }
}
