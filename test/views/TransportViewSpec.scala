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
import forms.Transport
import helpers.views.{CommonMessages, TransportMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.html.transport
import views.spec.UnitViewSpec

class TransportViewSpec extends UnitViewSpec with TransportMessages with CommonMessages {

  private val form: Form[Transport] = Transport.form
  private val transportPage = new transport(mainTemplate)

  private val view: Html = transportPage(form)

  "Transport View" should {

    val messages = messagesApi.preferred(request)

    "have a proper labels for messages" in {

      messages must haveTranslationFor(title)
      messages must haveTranslationFor(modeOfTransportQuestion)
      messages must haveTranslationFor(modeOfTransportSea)
      messages must haveTranslationFor(modeOfTransportRail)
      messages must haveTranslationFor(modeOfTransportRoad)
      messages must haveTranslationFor(modeOfTransportAir)
      messages must haveTranslationFor(modeOfTransportPostalOrMail)
      messages must haveTranslationFor(modeOfTransportFixed)
      messages must haveTranslationFor(modeOfTransportInland)
      messages must haveTranslationFor(modeOfTransportOther)
      messages must haveTranslationFor(nationalityQuestion)
      messages must haveTranslationFor(nationalityHint)
    }

    "have a proper labels for errors" in {

      messages must haveTranslationFor(modeOfTransportEmpty)
      messages must haveTranslationFor(modeOfTransportError)
      messages must haveTranslationFor(nationalityEmpty)
      messages must haveTranslationFor(nationalityError)
    }
  }

  "Transport View on empty page" should {

    "display page header" in {

      view.getElementById("title").text() mustBe messages(title)
    }

    "display input for mode of transport with all possible answers" in {

      view.getElementById("modeOfTransport-label").text() mustBe messages(modeOfTransportQuestion)
      view.getElementById("1-label").text() mustBe messages(modeOfTransportSea)
      view.getElementById("2-label").text() mustBe messages(modeOfTransportRail)
      view.getElementById("3-label").text() mustBe messages(modeOfTransportRoad)
      view.getElementById("4-label").text() mustBe messages(modeOfTransportAir)
      view.getElementById("5-label").text() mustBe messages(modeOfTransportPostalOrMail)
      view.getElementById("6-label").text() mustBe messages(modeOfTransportFixed)
      view.getElementById("7-label").text() mustBe messages(modeOfTransportInland)
      view.getElementById("8-label").text() mustBe messages(modeOfTransportOther)
    }

    "display input for nationality" in {

      view.getElementById("nationality-label").text() mustBe messages(nationalityQuestion)
      view.getElementById("nationality-hint").text() mustBe messages(nationalityHint)
    }

    "display \"Back\" button that links to Location" in {

      val backButton = view.getElementById("link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") mustBe routes.LocationController.displayPage().url
    }

    "display \"Save and continue\" button on page" in {

      val saveButton = view.getElementById("submit")

      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }
}
