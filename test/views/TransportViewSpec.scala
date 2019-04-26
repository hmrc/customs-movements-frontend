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

import forms.{Choice, Transport}
import helpers.views.{CommonMessages, TransportMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec

class TransportViewSpec extends ViewSpec with TransportMessages with CommonMessages {

  private val form: Form[Transport] = Transport.form()

  private def createArrivalView(form: Form[Transport] = form): Html =
    views.html.transport(form, Choice.AllowedChoiceValues.Arrival)

  private def createDepartureView(form: Form[Transport] = form): Html =
    views.html.transport(form, Choice.AllowedChoiceValues.Departure)

  "Transport View" should {

    "have a proper labels for messages" in {

      assertMessage(title, "Transport")
      assertMessage(modeOfTransportQuestion, "What is the mode of transport at the border?")
      assertMessage(modeOfTransportHint, "The transport that the goods will be loaded on when the depart at the border")
      assertMessage(modeOfTransportSea, "Sea transport")
      assertMessage(modeOfTransportRail, "Rail transport")
      assertMessage(modeOfTransportRoad, "Road transport")
      assertMessage(modeOfTransportAir, "Air transport")
      assertMessage(modeOfTransportPostalOrMail, "Postal or mail")
      assertMessage(modeOfTransportFixed, "Fixed transport installations")
      assertMessage(modeOfTransportInland, "Inland waterway transport")
      assertMessage(modeOfTransportOther, "Other, for example own propulsion")
      assertMessage(nationalityQuestion, "Nationality of transport crossing the border")
      assertMessage(nationalityHint, "A 2 digit code")
    }

    "have a proper labels for errors" in {

      assertMessage(modeOfTransportEmpty, "You need to choose mode of transport")
      assertMessage(modeOfTransportError, "Mode of transport is incorrect")
      assertMessage(nationalityEmpty, "Please provide nationality of transport")
      assertMessage(nationalityError, "Nationality of transport is incorrect")
    }
  }

  "Transport View on empty page" should {

    "display page title" in {

      getElementByCss(createArrivalView(), "head>title").text() must be(messages(title))
    }

    "display page header" in {

      getElementById(createArrivalView(), "title").text() must be(messages(title))
    }

    "display input for mode of transport with all possible answers" in {

      getElementById(createArrivalView(), "modeOfTransport-label").text() must be(messages(modeOfTransportQuestion))
      getElementById(createArrivalView(), "modeOfTransport-hint").text() must be(messages(modeOfTransportHint))
      getElementById(createArrivalView(), "1-label").text() must be(messages(modeOfTransportSea))
      getElementById(createArrivalView(), "2-label").text() must be(messages(modeOfTransportRail))
      getElementById(createArrivalView(), "3-label").text() must be(messages(modeOfTransportRoad))
      getElementById(createArrivalView(), "4-label").text() must be(messages(modeOfTransportAir))
      getElementById(createArrivalView(), "5-label").text() must be(messages(modeOfTransportPostalOrMail))
      getElementById(createArrivalView(), "6-label").text() must be(messages(modeOfTransportFixed))
      getElementById(createArrivalView(), "7-label").text() must be(messages(modeOfTransportInland))
      getElementById(createArrivalView(), "8-label").text() must be(messages(modeOfTransportOther))
    }

    "display input for nationality" in {

      getElementById(createArrivalView(), "nationality-label").text() must be(messages(nationalityQuestion))
      getElementById(createArrivalView(), "nationality-hint").text() must be(messages(nationalityHint))
    }

    "display \"Back\" button that links to Location for arrival" in {

      val backButton = getElementById(createArrivalView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-movements/location")
    }

    "display \"Back\" button that links to Date of Departure for departure" in {

      val backButton = getElementById(createDepartureView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-movements/movements-details")
    }

    "display \"Save and continue\" button on page" in {

      val saveButton = getElementById(createArrivalView(), "submit")

      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }
}
