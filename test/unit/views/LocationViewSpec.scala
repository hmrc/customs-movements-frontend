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

package views

import base.Injector
import controllers.routes
import forms.Location
import models.cache.{ArrivalAnswers, DepartureAnswers}
import play.api.data.Form
import play.twirl.api.Html
import views.html.location

class LocationViewSpec extends ViewSpec with Injector {

  private val form: Form[Location] = Location.form()
  private val locationPage = instanceOf[location]
  private implicit val request = journeyRequest(ArrivalAnswers())

  private val view: Html = locationPage(form, "some-reference")

  "Location View on empty page" should {

    "display page title" in {

      view.getElementsByTag("h1").first() must containMessage("location.question")
    }

    "have the correct section header for the Arrival journey" in {

      view.getElementById("section-header") must containMessage("movement.sectionHeading", "Arrive", "some-reference")
    }

    "have the correct section header for the Departure journey" in {

      val departureView = locationPage(form, "some-reference")(journeyRequest(DepartureAnswers()), messages)
      departureView.getElementById("section-header") must containMessage("movement.sectionHeading", "Depart", "some-reference")
    }

    "display input hint" in {

      view.getElementById("code-hint").text() mustBe messages("location.hint")
    }

    "display \"Back\" button that links to Movement Details" in {

      val backButton = view.getBackButton

      backButton mustBe defined
      backButton.get must containMessage("site.back")
      backButton.get must haveHref(routes.MovementDetailsController.displayPage())
    }

    "display 'Continue' button on page" in {
      view.getElementsByClass("govuk-button").first() must containMessage("site.continue")
    }
  }
}
