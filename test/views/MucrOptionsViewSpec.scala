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

import forms.MucrOptions
import helpers.views.CommonMessages
import org.jsoup.nodes.Document
import play.api.data.Form
import views.spec.UnitViewSpec

class MucrOptionsViewSpec extends UnitViewSpec with CommonMessages {

  private val form: Form[MucrOptions] = MucrOptions.form
  private val page = new views.html.mucr_options(mainTemplate)

  private val view: Document = page(form)

  "MUCR options" should {

    "have the correct title" in {
      view.getElementById("title").text() mustBe "mucrOptions.title"
    }

    "have the correct heading" in {
      view.getElementById("section-header").text() mustBe "associate.heading"
    }

    "have the correct label for create new" in {
      view.getElementById("mucrOptions.create-label").text() mustBe "mucrOptions.create"
    }

    "have the correct label for add to existing" in {
      view.getElementById("mucrOptions.add-label").text() mustBe "mucrOptions.add"
    }

    "have no options selected on initial display" in {
      view.getElementById("mucrOptions.create") mustBe unchecked
      view.getElementById("mucrOptions.add") mustBe unchecked
    }

    "display 'Back' button that links to start page" in {
      val backButton = view.getElementById("link-back")
      backButton.text() must be(backCaption)
      backButton must haveHref(controllers.routes.ChoiceController.displayChoiceForm())
    }

    "display 'Continue' button on page" in {
      view.getElementById("submit").text() mustBe continueCaption
    }
  }
}
