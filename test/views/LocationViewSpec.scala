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
  private val locationPage = injector.instanceOf[views.html.location]

  private def createArrivalView(form: Form[Location] = form): Html =
    locationPage(form, Choice.AllowedChoiceValues.Arrival)

  private def createDepartureView(form: Form[Location] = form): Html =
    locationPage(form, Choice.AllowedChoiceValues.Departure)

  "Location View" should {

    "have a proper labels for messages" in {

      assertMessage(title, "Location")
      assertMessage(question, "Where are the goods located?")
      assertMessage(locationType, "Location Type")
      assertMessage(locationTypeA, "A - Designated location (denotes Frontier or Frontier linked - Airports, ITSFs etc)")
      assertMessage(locationTypeB, "B - Authorised place (identifies inland locations such as customs warehouses)")
      assertMessage(locationTypeC, "C - Approved place (only used for certificate of Agreement AirFields)")
      assertMessage(locationTypeD, "D - Other (such as pipelines, continental shelf, sind farms, etc)")
      assertMessage(qualifierCode, "Qualifier Code")
      assertMessage(qualifierCodeU, "U - UN/LOCODE")
      assertMessage(qualifierCodeY, "Y - Authorisation number")
      assertMessage(locationCode, "Location Code and Additional Qualifier")
      assertMessage(country, "Country")
    }

    "have a proper labels for errors" in {

      assertMessage(locationTypeEmpty, "Location Type cannot be empty")
      assertMessage(locationTypeError, "Location Type is incorrect")
      assertMessage(qualifierCodeEmpty, "Qualifier Code cannot be empty")
      assertMessage(qualifierCodeError, "Qualifier Code is incorrect")
      assertMessage(locationCodeEmpty, "Location Code and Additional Qualifier cannot be empty")
      assertMessage(locationCodeError, "Location Code and Additional Qualifier is incorrect")
      assertMessage(countryEmpty, "Country cannot be empty")
      assertMessage(countryError, "Country is incorrect")
    }
  }

  "Location View on empty page" should {

    "display page title" in {

      getElementById(createArrivalView(), "title").text() must be(messages(question))
    }

    "display text input for all fields" in {

      getElementById(createArrivalView(), "locationType-label").text() must be(messages(locationType))
      getElementById(createArrivalView(), "qualifierCode-label").text() must be(messages(qualifierCode))
      getElementById(createArrivalView(), "locationCode-label").text() must be(messages(locationCode))
      getElementById(createArrivalView(), "country-label").text() must be(messages(country))
    }

    "display \"Back\" button that links to Goods Date for arrival" in {

      val backButton = getElementById(createArrivalView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-movements/movement-details")
    }

    "display \"Back\" button that links to Consignment References for departure" in {

      val backButton = getElementById(createDepartureView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-movements/consignment-references")
    }

    "display \"Save and continue\" button on page" in {

      val saveButton = getElementById(createArrivalView(), "submit")

      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }
}
