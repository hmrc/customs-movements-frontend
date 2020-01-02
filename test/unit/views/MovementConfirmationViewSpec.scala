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
import forms.{Choice, ConsignmentReferences}
import models.cache.{ArrivalAnswers, DepartureAnswers}
import testdata.CommonTestData.correctUcr
import views.html.movement_confirmation_page

class MovementConfirmationViewSpec extends ViewSpec with Injector {

  val movementConfirmationPage = instanceOf[movement_confirmation_page]

  val consignmentReferences = ConsignmentReferences(ConsignmentReferences.AllowedReferences.Ducr, correctUcr)

  "Arrival Confirmation Page" should {

    implicit val request = journeyRequest(ArrivalAnswers())
    val arrivalConfirmationView = movementConfirmationPage(Choice.Arrival, consignmentReferences)

    "have title" in {

      arrivalConfirmationView.getTitle must containMessage("movement.arrival.confirmation.tab.heading")
    }

    "have heading" in {

      arrivalConfirmationView.getElementsByClass("govuk-panel__title").first() must containMessage(
        "movement.arrival.confirmation.heading",
        "DUCR",
        correctUcr
      )
    }

    "have status information" in {

      arrivalConfirmationView.getElementById("status-info").getElementsByClass("govuk-link").first() must haveHref(
        routes.ChoiceController.startSpecificJourney(forms.Choice.Submissions.value)
      )
    }

    "have what next section" in {

      arrivalConfirmationView.getElementById("what-next") must containMessage("movement.confirmation.whatNext")
    }

    "have next steps section" in {

      val nextSteps = arrivalConfirmationView.getElementById("next-steps").getElementsByClass("govuk-link")
      nextSteps.first() must haveHref(routes.ChoiceController.startSpecificJourney(forms.Choice.AssociateUCR.value))
      nextSteps.last() must haveHref(routes.ChoiceController.startSpecificJourney(forms.Choice.Departure.value))
    }

    "have back button" in {

      val backButton = arrivalConfirmationView.getElementsByClass("govuk-button")

      backButton.first() must containMessage("site.backToStart")
      backButton.first() must haveHref(routes.ChoiceController.displayChoiceForm())
    }
  }

  "Departure Confirmation Page" should {

    implicit val request = journeyRequest(DepartureAnswers())
    val departureConfirmationView = movementConfirmationPage(Choice.Departure, consignmentReferences)

    "have title" in {

      departureConfirmationView.getTitle must containMessage("movement.departure.confirmation.tab.heading")
    }

    "have heading" in {

      departureConfirmationView.getElementsByClass("govuk-panel__title").first() must containMessage(
        "movement.departure.confirmation.heading",
        "DUCR",
        correctUcr
      )
    }

    "have status information" in {

      departureConfirmationView.getElementById("status-info").getElementsByClass("govuk-link").first() must haveHref(
        routes.ChoiceController.startSpecificJourney(forms.Choice.Submissions.value)
      )
    }

    "have what next section" in {

      departureConfirmationView.getElementById("what-next") must containMessage("movement.confirmation.whatNext")
    }

    "have next steps section" in {

      val nextSteps = departureConfirmationView.getElementById("next-steps").getElementsByClass("govuk-link")
      nextSteps.first() must haveHref(routes.ChoiceController.startSpecificJourney(forms.Choice.Departure.value))
      nextSteps.last() must haveHref(routes.ChoiceController.startSpecificJourney(forms.Choice.Arrival.value))
    }

    "have back button" in {

      val backButton = departureConfirmationView.getElementsByClass("govuk-button")

      backButton.first() must containMessage("site.backToStart")
      backButton.first() must haveHref(routes.ChoiceController.displayChoiceForm())
    }
  }
}
