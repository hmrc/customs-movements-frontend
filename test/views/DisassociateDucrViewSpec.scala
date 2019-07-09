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

import forms.DisassociateDucr
import helpers.views.{CommonMessages, DisassociateDucrMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec

class DisassociateDucrViewSpec extends ViewSpec with DisassociateDucrMessages with CommonMessages {

  private val form: Form[DisassociateDucr] = DisassociateDucr.form
  private val page = injector.instanceOf[views.html.disassociate_ducr]

  private def createView(form: Form[DisassociateDucr] = form): Html = page(form)

  "Disassociate Ducr View" should {

    "have a proper labels for messages" in {

      assertMessage(title, "Which DUCR do you want to disassociate?")
    }

    "have a proper labels for error messages" in {

      assertMessage(ducrValueError, "Incorrect DUCR")
      assertMessage(ducrValueEmpty, "Please enter DUCR")
    }
  }

  "Disassociate Ducr View on empty page" should {

    "display page title" in {

      getElementById(createView(), "title").text() must be(messages(title))
    }

    "display \"Back\" buttion that links to start page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be(controllers.routes.ChoiceController.displayChoiceForm().url)
    }

    "display \"Save and continue\" button on page" in {

      val view = createView()

      val saveButton = getElementById(view, "submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }
}
