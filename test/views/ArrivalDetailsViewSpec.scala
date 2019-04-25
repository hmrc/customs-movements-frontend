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

import forms.{ArrivalDetails, MovementDetails}
import helpers.views.{ArrivalDetailsMessages, CommonMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec

class ArrivalDetailsViewSpec extends ViewSpec with ArrivalDetailsMessages with CommonMessages {

  val form: Form[ArrivalDetails] = MovementDetails.arrivalForm()

  private def createView(form: Form[ArrivalDetails] = form): Html =
    views.html.arrival_details(form)

  "Arrival Details View" should {

    "have a proper labels for messages" in {

      assertMessage(arrivalTitle, "Arrival time and date")
      assertMessage(arrivalHeader, "Enter date and time of arrival")
      assertMessage(arrivalDateQuestion, "Date of Arrival")
      assertMessage(arrivalDateHint, "For example, 01 08 2007")
      assertMessage(arrivalTimeQuestion, "Time of arrival")
      assertMessage(arrivalTimeHint, "Enter the time in 24 hour format. For example, 13 30")
    }
  }

  "Arrival Details View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "head>title").text() must be(messages(arrivalTitle))
    }

    "display page header" in {

      getElementById(createView(), "title").text() must be(messages(arrivalHeader))
    }

    "display input with hint for date" in {

      getElementById(createView(), "dateOfArrival-label").text() must be(messages(arrivalDateQuestion))
      getElementById(createView(), "dateOfArrival-hint").text() must be(messages(arrivalDateHint))
    }

    "display input with hint for time" in {

      getElementById(createView(), "timeOfArrival-label").text() must be(messages(arrivalTimeQuestion))
      getElementById(createView(), "timeOfArrival-hint").text() must be(messages(arrivalTimeHint))
    }

    "display \"Back\" button that links to Consignment References" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-movements/consignment-references")
    }

    "display \"Save and continue\" button on page" in {

      val saveButton = getElementById(createView(), "submit")

      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }
}
