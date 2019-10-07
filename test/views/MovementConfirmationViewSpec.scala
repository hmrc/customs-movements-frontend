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
import forms.Choice.{Arrival, Departure}
import views.html.movement_confirmation_page
import views.spec.UnitViewSpec

class MovementConfirmationViewSpec extends UnitViewSpec {

  val movementConfirmationPage = new movement_confirmation_page(mainTemplate)
  val arrivalConfirmationView = movementConfirmationPage(Arrival)(request, messages)
  val departureConfirmationView = movementConfirmationPage(Departure)(request, messages)

  "Movement Confirmation Page" should {

    "have correct messages" in {

      val messages = messagesApi.preferred(request)

      messages("movement.arrival.confirmation") mustBe "Arrival has been submitted"
      messages("movement.departure.confirmation") mustBe "Departure has been submitted"
      messages("movement.choice.arrival.label") mustBe "Arrival"
      messages("movement.choice.departure.label") mustBe "Departure"
      messages("site.backToStartPage") mustBe "Back to start page"
    }
  }

  "Arrival Confirmation Page" should {

    "display same page title as header" in {

      val view = movementConfirmationPage(Arrival)(request, messagesApi.preferred(request))
      view.title() must include(view.getElementsByTag("h1").text())
    }

    "have heading" in {

      arrivalConfirmationView.getElementById("highlight-box-heading").text() mustBe messages(
        "movement.arrival.confirmation"
      )
    }

    "have back button" in {

      val backButton = arrivalConfirmationView.getElementsByClass("button")

      backButton.text() mustBe messages("site.backToStartPage")
      backButton.attr("href") mustBe routes.StartController.displayStartPage().url
    }
  }

  "Departure Confirmation Page" should {

    "display same page title as header" in {

      val view = movementConfirmationPage(Departure)(request, messagesApi.preferred(request))
      view.title() must include(view.getElementsByTag("h1").text())
    }

    "have heading" in {

      departureConfirmationView.getElementById("highlight-box-heading").text() mustBe messages(
        "movement.departure.confirmation"
      )
    }

    "have back button" in {

      val backButton = departureConfirmationView.getElementsByClass("button")

      backButton.text() mustBe messages("site.backToStartPage")
      backButton.attr("href") mustBe routes.StartController.displayStartPage().url
    }
  }
}
