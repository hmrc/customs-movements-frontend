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

import forms.{Choice, Location}
import helpers.views.{CommonMessages, LocationMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec

class LocationViewSpec extends ViewSpec with LocationMessages with CommonMessages {

  private val form: Form[Location] = Location.form()

  private def createArrivalView(form: Form[Location] = form): Html =
    views.html.location(form, Choice.AllowedChoiceValues.Arrival)

  private def createDepartureView(form: Form[Location] = form): Html =
    views.html.location(form, Choice.AllowedChoiceValues.Departure)

  "Location View" should {

    "have a proper labels for messages" in {

      assertMessage(title, "Location")
      assertMessage(question, "Where are the goods located?")
      assertMessage(hint, "The 7 digit code of where the goods are located")
    }

    "have a proper labels for errors" in {

      assertMessage(error, "Code must have exactly 7 digits")
    }
  }

  "Location View on empty page" should {

    "display page title" in {

      getElementById(createArrivalView(), "title").text() must be(messages(title))
    }

    "display text input for location" in {

      getElementById(createArrivalView(), "goodsLocation-label").text() must be(messages(question))
    }

    "display \"Back\" button that links to Goods Date for arrival" in {

      val backButton = getElementById(createArrivalView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-movements/goods-date")
    }

    "display \"Back\" button that links to Consignment References for departure" in {

      val backButton = getElementById(createDepartureView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-movements/consignment-references")
    }

    "display \"Save and continue\" button on page" in {

      val view = createArrivalView()

      val saveButton = getElementById(view, "submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }
}
