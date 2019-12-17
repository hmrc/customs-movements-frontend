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

import base.Injector
import controllers.routes
import forms.Transport
import helpers.views.{CommonMessages, TransportMessages}
import models.cache.ArrivalAnswers
import play.api.data.Form
import play.twirl.api.Html
import views.html.transport
import views.spec.UnitViewSpec

class TransportViewSpec extends ViewSpec with CommonMessages with Injector {

  private val form: Form[Transport] = Transport.form
  private val transportPage = instanceOf[transport]

  private implicit val request = journeyRequest(ArrivalAnswers())

  private def createPage = transportPage(Transport.form, Some("some-reference"))

  "Transport View" should {

    "have the correct title" in {
      createPage.getTitle must containMessage("transport.title")
    }

    "have the correct section header" in {
      createPage.getElementById("section-header") must containMessage("transport.heading", "some-reference")
    }

    "display header with hint" in {
      createPage.getElementsByClass("govuk-fieldset__legend").get(0).text() must be(messages("transport.modeOfTransport.question"))
    }

    "display 8 radio buttons with labels" in {
      createPage.getElementsByAttributeValue("for", "modeOfTransport").text() must be(messages("transport.modeOfTransport.1"))
      createPage.getElementsByAttributeValue("for", "modeOfTransport-2").text() must be(messages("transport.modeOfTransport.2"))
      createPage.getElementsByAttributeValue("for", "modeOfTransport-3").text() must be(messages("transport.modeOfTransport.3"))
      createPage.getElementsByAttributeValue("for", "modeOfTransport-4").text() must be(messages("transport.modeOfTransport.4"))
      createPage.getElementsByAttributeValue("for", "modeOfTransport-5").text() must be(messages("transport.modeOfTransport.5"))
      createPage.getElementsByAttributeValue("for", "modeOfTransport-6").text() must be(messages("transport.modeOfTransport.6"))
      createPage.getElementsByAttributeValue("for", "modeOfTransport-7").text() must be(messages("transport.modeOfTransport.7"))
      createPage.getElementsByAttributeValue("for", "modeOfTransport-8").text() must be(messages("transport.modeOfTransport.8"))
    }

    "Transport View on empty page" should {

      "display the correct labels and hints" in {
        createPage.getElementsByAttributeValue("for", "transportId").text() must be(messages("transport.transportId.question"))
        createPage.getElementById("transportId-hint").text() mustBe messages("transport.transportId.hint")
        createPage.getElementsByAttributeValue("for", "nationality").text() must be(messages("transport.nationality.question"))
        createPage.getElementById("nationality-hint").text() mustBe messages("transport.nationality.hint")
      }

    }

    "display \"Back\" button that links to Location page" in {

      val backButton = createPage.getElementById("back-link")

      backButton.text() must be(messages(backCaption))
      backButton must haveHref(routes.LocationController.displayPage())
    }

    "display 'Continue' button on page" in {
      val saveButton = createPage.getElementsByClass("govuk-button").get(0)
      saveButton.text() must be(messages(continueCaption))
    }
  }

}
