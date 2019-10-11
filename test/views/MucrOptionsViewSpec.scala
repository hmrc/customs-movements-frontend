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
import play.api.data.Form
import play.twirl.api.Html
import views.spec.ViewSpec

class MucrOptionsViewSpec extends ViewSpec with CommonMessages {

  private val form: Form[MucrOptions] = MucrOptions.form
  private val page = injector.instanceOf[views.html.mucr_options]

  private val view: Html = page(form)

  "MUCR options" should {

    "have the correct title" in {
      getElementById(view, "title").text() mustBe "Create or enter a MUCR to add to"
    }

    "have the correct label for create new" in {
      getElementById(view, "mucrOptions.create-label").text() mustBe "Create a new MUCR"
    }

    "have the correct label for add to existing" in {
      getElementById(view, "mucrOptions.add-label").text() mustBe "Add to an existing MUCR"
    }

    "have no options selected on initial display" in {
      verifyUnchecked(view, "mucrOptions.create")
      verifyUnchecked(view, "mucrOptions.add")
    }

    "display 'Back' button that links to start page" in {
      val backButton = getElementById(view, "link-back")
      backButton.text() must be(messages(backCaption))
      backButton.attr("href") mustBe controllers.routes.ChoiceController.displayChoiceForm().url
    }

    "display 'Save and continue' button on page" in {
      getElementById(view, "submit").text() mustBe messages(saveAndContinueCaption)
    }
  }
}
