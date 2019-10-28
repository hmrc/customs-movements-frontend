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
import forms.{DepartureDetails, MovementDetails}
import helpers.views.{CommonMessages, DepartureDetailsMessages}
import org.jsoup.nodes.Document
import play.api.data.Form
import testdata.MovementsTestData
import views.spec.UnitViewSpec

class DepartureDetailsViewSpec extends UnitViewSpec with DepartureDetailsMessages with CommonMessages {

  val form: Form[DepartureDetails] = MovementsTestData.movementDetails.departureForm()
  val departureDetailsPage = new views.html.departure_details(mainTemplate)

  private def createView(form: Form[DepartureDetails] = form): Document = departureDetailsPage(form)

  "Arrival Details View" should {

    "have a proper labels for messages" in {
      val messages = messagesApi.preferred(request)
      messages must haveTranslationFor(departureTitle)
      messages must haveTranslationFor(departureHeader)
      messages must haveTranslationFor(departureDateQuestion)
      messages must haveTranslationFor(departureDateHint)
      messages must haveTranslationFor(departureTimeQuestion)
      messages must haveTranslationFor(departureTimeHint)
    }
  }

  "Arrival Details View on empty page" should {

    val view = createView()

    "display same page title as header" in {
      val fullRender = departureDetailsPage(form)(request, messagesApi.preferred(request))
      fullRender.title() must include(fullRender.getElementsByTag("h1").text())
    }

    "have date section" that {
      "got label" in {
        view.getElementById("dateOfDeparture-label").text() mustBe departureDateQuestion
      }
      "got hint" in {
        view.getElementById("dateOfDeparture-hint").text() mustBe departureDateHint
      }
    }

    "have time input" that {
      "got legend" in {
        view.getElementById("timeOfDeparture-label").text() mustBe departureTimeQuestion
      }
      "got lable" in {
        view.getElementById("timeOfDeparture-hint").text() mustBe departureTimeHint
      }
    }

    "display \"Back\" button that links to Consignment References" in {

      val backButton = view.getElementById("link-back")

      backButton.text() must be(backCaption)
      backButton must haveHref(routes.ConsignmentReferencesController.displayPage())
    }

    "display \"Save and continue\" button on page" in {

      val saveButton = view.getElementById("submit")

      saveButton.text() mustBe saveAndContinueCaption
    }
  }
}
