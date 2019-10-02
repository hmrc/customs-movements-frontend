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

import forms.{DepartureDetails, MovementDetails}
import helpers.views.{CommonMessages, DepartureDetailsMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.spec.ViewSpec

class DepartureDetailsViewSpec extends ViewSpec with DepartureDetailsMessages with CommonMessages {

  val form: Form[DepartureDetails] = MovementDetails.departureForm()
  val departureDetailsPage = injector.instanceOf[views.html.departure_details]

  private def createView(form: Form[DepartureDetails] = form): Html = departureDetailsPage(form)

  "Arrival Details View" should {

    "have a proper labels for messages" in {

      assertMessage(departureTitle, "Departure time and date")
      assertMessage(departureHeader, "Enter departure details")
      assertMessage(departureQuestion, "Date of departure")
      assertMessage(departureHint, "For example, 01 08 2007")
    }
  }

  "Arrival Details View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "head>title").text() must be(messages(departureTitle))
    }

    "display page header" in {

      getElementById(createView(), "title").text() must be(messages(departureHeader))
    }

    "display input with hint for date" in {

      getElementById(createView(), "dateOfDeparture-label").text() must be(messages(departureQuestion))
      getElementById(createView(), "dateOfDeparture-hint").text() must be(messages(departureHint))
    }

    "display \"Back\" button that links to Consignment References" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-movements/goods-departed")
    }

    "display \"Save and continue\" button on page" in {

      val saveButton = getElementById(createView(), "submit")

      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }
}
