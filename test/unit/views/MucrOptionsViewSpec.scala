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

import base.Injector
import forms.MucrOptions
import helpers.views.CommonMessages
import org.jsoup.nodes.Document
import play.api.data.Form
import views.html.mucr_options
import views.spec.{UnitViewSpec, ViewMatchers}

class MucrOptionsViewSpec extends UnitViewSpec with CommonMessages with ViewMatchers with Injector {

  private val form: Form[MucrOptions] = MucrOptions.form
  private val page = instanceOf[mucr_options]

  private val view: Document = page(form)

  "MUCR options" should {

    "have the correct title" in {
      view.getElementsByClass("govuk-fieldset__heading").text() mustBe "mucrOptions.title"
    }

    "have the correct heading" in {
      view.getElementsByClass("govuk-caption-xl").text() mustBe "associate.heading"
    }

    "have the correct label for create new" in {
      view.getElementById("conditional-createOrAdd").text() mustBe "mucrOptions.create.reference"
    }

    "have no options selected on initial display" in {
      view.getElementById("mucrOptions.create") mustBe unchecked
      view.getElementById("mucrOptions.add") mustBe unchecked
    }

    "display 'Back' button that links to start page" in {
      val backButton = view.getElementById("back-link")
      backButton.text() must be(backCaption)
      backButton must haveHref(controllers.routes.ChoiceController.displayChoiceForm())
    }

    "display 'Continue' button on page" in {
      view.getElementsByClass("govuk-button").text() mustBe continueCaption
    }
  }
}
