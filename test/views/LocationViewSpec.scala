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
import forms.Choice.Arrival
import forms.Location
import helpers.views.{CommonMessages, LocationMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.spec.ViewSpec

class LocationViewSpec extends ViewSpec with LocationMessages with CommonMessages {

  private val form: Form[Location] = Location.form()
  private val locationPage = injector.instanceOf[views.html.location]

  private val view: Html = locationPage(form, Arrival)

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
      assertMessage(locationCodeError, "Location Code and Additional Qualifier must be between 6 and 13 characters")
      assertMessage(countryEmpty, "Country cannot be empty")
      assertMessage(countryError, "Country is incorrect")
    }
  }

  "Location View on empty page" should {

    "display page title" in {

      getElementById(view, "title").text() mustBe messages(question)
    }

    "display text input for all fields" in {

      getElementById(view, "locationType-label").text() mustBe messages(locationType)
      getElementById(view, "qualifierCode-label").text() mustBe messages(qualifierCode)
      getElementById(view, "locationCode-label").text() mustBe messages(locationCode)
      getElementById(view, "country-label").text() mustBe messages(country)
    }

    "display \"Back\" button that links to Movement Details" in {

      val backButton = getElementById(view, "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") mustBe routes.MovementDetailsController.displayPage().url
    }

    "display \"Save and continue\" button on page" in {

      val saveButton = getElementById(view, "submit")

      saveButton.text() mustBe messages(saveAndContinueCaption)
    }
  }
}
