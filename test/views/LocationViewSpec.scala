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

import controllers.routes
import forms.Location
import helpers.views.CommonMessages
import play.api.data.Form
import play.twirl.api.Html
import views.html.location
import views.spec.UnitViewSpec

class LocationViewSpec extends UnitViewSpec with CommonMessages {

  private val form: Form[Location] = Location.form()
  private val locationPage = new location(mainTemplate)

  private val view: Html = locationPage(form)

  "Location View" should {

    val messages = messagesApi.preferred(request)

    "have a proper labels for messages" in {

      messages must haveTranslationFor("location.title")
      messages must haveTranslationFor("location.question")
      messages must haveTranslationFor("location.hint")
    }

    "have a proper labels for errors" in {

      messages must haveTranslationFor("location.code.error")
    }
  }

  "Location View on empty page" should {

    "display page title" in {

      view.getElementById("title").text() mustBe messages("location.question")
    }

    "display input hint" in {

      view.getElementById("code-hint").text() mustBe messages("location.hint")
    }

    "display \"Back\" button that links to Movement Details" in {

      val backButton = view.getElementById("link-back")

      backButton.text() must be(messages(backCaption))
      backButton must haveHref(routes.MovementDetailsController.displayPage())
    }

    "display 'Continue' button on page" in {

      val saveButton = view.getElementById("submit")

      saveButton.text() mustBe messages(continueCaption)
    }
  }
}
