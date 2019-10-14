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
import forms.ConsignmentReferences
import testdata.CommonTestData.correctUcr
import views.html.movement_confirmation_page
import views.spec.{UnitViewSpec, ViewMatchers}

class MovementConfirmationViewSpec extends UnitViewSpec {

  val movementConfirmationPage = new movement_confirmation_page(mainTemplate)
  val consignmentReferences = ConsignmentReferences(ConsignmentReferences.AllowedReferences.Ducr, correctUcr)
  val arrivalRequest = fakeJourneyRequest(Arrival)
  val departureRequest = fakeJourneyRequest(Departure)
  val arrivalConfirmationView = movementConfirmationPage(consignmentReferences)(arrivalRequest, messages)
  val departureConfirmationView = movementConfirmationPage(consignmentReferences)(departureRequest, messages)

  "Movement Confirmation Page" should {

    "have correct messages" in {

      val messages = messagesApi.preferred(request)

      messages must haveTranslationFor("movement.arrival.confirmation.tab.heading")
      messages must haveTranslationFor("movement.arrival.confirmation.heading")
      messages must haveTranslationFor("movement.arrival.confirmation.nextSteps")
      messages must haveTranslationFor("movement.departure.confirmation.tab.heading")
      messages must haveTranslationFor("movement.departure.confirmation.heading")
      messages must haveTranslationFor("movement.departure.confirmation.nextSteps")
      messages must haveTranslationFor("movement.confirmation.statusInfo")
      messages must haveTranslationFor("movement.confirmation.whatNext")
    }
  }

  "Arrival Confirmation Page" should {

    "have title" in {

      arrivalConfirmationView.getElementsByTag("title").first().text() must include(messages("title.format"))
    }

    "have heading" in {

      arrivalConfirmationView.getElementById("highlight-box-heading").text() mustBe messages("movement.arrival.confirmation.heading")
    }

    "have status information" in {

      arrivalConfirmationView.getElementById("status-info").text() mustBe messages("movement.confirmation.statusInfo")
    }

    "have what next section" in {

      arrivalConfirmationView.getElementById("what-next").text() mustBe messages("movement.confirmation.whatNext")
    }

    "have next steps section" in {

      arrivalConfirmationView.getElementById("next-steps").text() mustBe messages("movement.arrival.confirmation.nextSteps")
    }

    "have back button" in {

      val backButton = arrivalConfirmationView.getElementsByClass("button")

      backButton.text() mustBe messages("site.backToStartPage")
      backButton.attr("href") mustBe routes.StartController.displayStartPage().url
    }
  }

  "Departure Confirmation Page" should {

    "have title" in {

      departureConfirmationView.getElementsByTag("title").first().text() must include(messages("title.format"))
    }

    "have heading" in {

      departureConfirmationView.getElementById("highlight-box-heading").text() mustBe messages("movement.departure.confirmation.heading")
    }

    "have status information" in {

      departureConfirmationView.getElementById("status-info").text() mustBe messages("movement.confirmation.statusInfo")
    }

    "have what next section" in {

      departureConfirmationView.getElementById("what-next").text() mustBe messages("movement.confirmation.whatNext")
    }

    "have next steps section" in {

      departureConfirmationView.getElementById("next-steps").text() mustBe messages("movement.departure.confirmation.nextSteps")
    }

    "have back button" in {

      val backButton = departureConfirmationView.getElementsByClass("button")

      backButton.text() mustBe messages("site.backToStartPage")
      backButton.attr("href") mustBe routes.StartController.displayStartPage().url
    }
  }
}
