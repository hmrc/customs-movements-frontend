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
import helpers.views.CommonMessages
import testdata.CommonTestData.correctUcr
import views.html.movement_confirmation_page
import views.spec.{UnitViewSpec, ViewMatchers}

class MovementConfirmationViewSpec extends UnitViewSpec with CommonMessages {

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
      messages must haveTranslationFor("movement.arrival.confirmation.nextSteps.part1")
      messages must haveTranslationFor("movement.arrival.confirmation.nextSteps.part2")
      messages must haveTranslationFor("movement.arrival.confirmation.nextSteps.part3")
      messages must haveTranslationFor("movement.arrival.confirmation.nextSteps.part4")
      messages must haveTranslationFor("movement.departure.confirmation.tab.heading")
      messages must haveTranslationFor("movement.departure.confirmation.heading")
      messages must haveTranslationFor("movement.departure.confirmation.nextSteps.part1")
      messages must haveTranslationFor("movement.departure.confirmation.nextSteps.part2")
      messages must haveTranslationFor("movement.departure.confirmation.nextSteps.part3")
      messages must haveTranslationFor("movement.departure.confirmation.nextSteps.part4")
      messages must haveTranslationFor("movement.confirmation.statusInfo.part1")
      messages must haveTranslationFor("movement.confirmation.statusInfo.part2")
      messages must haveTranslationFor("movement.confirmation.whatNext")
    }
  }

  "Arrival Confirmation Page" should {

    "have title" in {

      arrivalConfirmationView.getElementsByTag("title").first().text() must include(messages("title.format"))
    }

    "have heading" in {

      arrivalConfirmationView.getElementById("highlight-box-heading").text() mustBe
        messages("movement.arrival.confirmation.heading")
    }

    "have status information" in {

      val expectedMessage = "movement.confirmation.statusInfo.part1 movement.confirmation.statusInfo.part2."

      arrivalConfirmationView.getElementById("status-info").text() mustBe expectedMessage
    }

    "have what next section" in {

      arrivalConfirmationView.getElementById("what-next").text() mustBe messages("movement.confirmation.whatNext")
    }

    "have next steps section" in {

      val expectedMessage = "movement.arrival.confirmation.nextSteps.part1 movement.arrival.confirmation.nextSteps.part2 " +
        "movement.arrival.confirmation.nextSteps.part3 movement.arrival.confirmation.nextSteps.part4"

      arrivalConfirmationView.getElementById("next-steps").text() mustBe expectedMessage
    }

    "have back button" in {

      val backButton = arrivalConfirmationView.getElementsByClass("button")

      backButton.text() mustBe messages("site.backToStartPage")
      backButton.first() must haveHref(routes.ChoiceController.displayChoiceForm())
    }
  }

  "Departure Confirmation Page" should {

    "have title" in {

      departureConfirmationView.getElementsByTag("title").first().text() must include(messages("title.format"))
    }

    "have heading" in {

      departureConfirmationView.getElementById("highlight-box-heading").text() mustBe
        messages("movement.departure.confirmation.heading")
    }

    "have status information" in {

      val expectedMessage = "movement.confirmation.statusInfo.part1 movement.confirmation.statusInfo.part2."

      departureConfirmationView.getElementById("status-info").text() mustBe expectedMessage
    }

    "have what next section" in {

      departureConfirmationView.getElementById("what-next").text() mustBe messages("movement.confirmation.whatNext")
    }

    "have next steps section" in {

      val expectedMessage = "movement.departure.confirmation.nextSteps.part1 " +
        "movement.departure.confirmation.nextSteps.part2 movement.departure.confirmation.nextSteps.part3 " +
        "movement.departure.confirmation.nextSteps.part4."

      departureConfirmationView.getElementById("next-steps").text() mustBe expectedMessage
    }

    "have back button" in {

      val backButton = arrivalConfirmationView.getElementsByClass("button")

      backButton.text() mustBe messages("site.backToStartPage")
      backButton.first() must haveHref(routes.ChoiceController.displayChoiceForm())
    }
  }
}
