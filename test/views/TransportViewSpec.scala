/*
 * Copyright 2024 HM Revenue & Customs
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
import models.cache.ArrivalAnswers
import models.requests.JourneyRequest
import play.api.mvc.AnyContentAsEmpty
import play.twirl.api.Html
import views.html.transport

class TransportViewSpec extends ViewSpec with Injector {

  private val page = instanceOf[transport]

  private implicit val request: JourneyRequest[AnyContentAsEmpty.type] = journeyRequest(ArrivalAnswers())

  private def createPage(implicit request: JourneyRequest[_] = request): Html = page(Transport.form, "some-reference")

  "Transport View" should {

    "have the page's title prefixed with 'Error:'" when {
      "the page has errors" in {
        val view = page(Transport.form.withGlobalError("error.summary.title"), "some-reference")
        view.head.getElementsByTag("title").first.text must startWith("Error: ")
      }
    }

    "have the correct title" in {
      createPage().getTitle must containMessage("transport.title")
    }

    "have the correct section header" in {
      createPage().getElementById("section-header") must containMessage("transport.heading", "some-reference")
    }

    "display header with hint" in {
      createPage().getElementsByClass("govuk-fieldset__legend").get(0).text() must be(messages("transport.modeOfTransport.question"))
    }

    "display 8 radio buttons with labels" in {
      createPage().getElementsByAttributeValue("for", "modeOfTransport").text() must be(messages("transport.modeOfTransport.1"))
      createPage().getElementsByAttributeValue("for", "modeOfTransport-2").text() must be(messages("transport.modeOfTransport.2"))
      createPage().getElementsByAttributeValue("for", "modeOfTransport-3").text() must be(messages("transport.modeOfTransport.3"))
      createPage().getElementsByAttributeValue("for", "modeOfTransport-4").text() must be(messages("transport.modeOfTransport.4"))
      createPage().getElementsByAttributeValue("for", "modeOfTransport-5").text() must be(messages("transport.modeOfTransport.5"))
      createPage().getElementsByAttributeValue("for", "modeOfTransport-6").text() must be(messages("transport.modeOfTransport.6"))
      createPage().getElementsByAttributeValue("for", "modeOfTransport-7").text() must be(messages("transport.modeOfTransport.7"))
      createPage().getElementsByAttributeValue("for", "modeOfTransport-8").text() must be(messages("transport.modeOfTransport.8"))
    }

    "Transport View on empty page" should {

      "display the correct labels and hints" in {
        createPage().getElementsByAttributeValue("for", "transportId").text() must be(messages("transport.transportId.question"))
        createPage().getElementById("transportId-hint").text() mustBe messages("transport.transportId.hint")
        createPage().getElementsByAttributeValue("for", "nationality").text() must be(messages("transport.nationality.question"))
      }

    }

    "display \"Back\" button that links to Location page" in {

      val backButton = createPage().getElementById("back-link")

      backButton.text() must be(messages("site.back.previousQuestion"))
      backButton must haveHref(routes.LocationController.displayPage)
    }

    checkSaveAndReturnToSummaryButtonIsHidden(createPage())

    checkAllSaveButtonsAreDisplayed(createPage(journeyRequest(ArrivalAnswers(readyToSubmit = Some(true)))))
  }

}
