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
import forms.Location
import models.cache.{ArrivalAnswers, DepartureAnswers}
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.mvc.AnyContentAsEmpty
import play.twirl.api.Html
import views.html.location

class LocationViewSpec extends ViewSpec with Injector {

  private val form: Form[Location] = Location.form()
  private val page = instanceOf[location]

  private implicit val request: JourneyRequest[AnyContentAsEmpty.type] = journeyRequest(ArrivalAnswers())

  private def createView(implicit request: JourneyRequest[_] = request): Html = page(form, "some-reference", None)

  "Location View on empty page" should {

    "have the page's title prefixed with 'Error:'" when {
      "the page has errors" in {
        val view = page(form.withGlobalError("error.summary.title"), "some-reference", None)
        view.head.getElementsByTag("title").first.text must startWith("Error: ")
      }
    }

    "display page title" in {
      createView().getElementsByTag("h1").first() must containMessage("location.question")
    }

    "have the correct section header for the Arrival journey" in {
      createView().getElementById("section-header") must containMessage("movement.sectionHeading.arrive", "some-reference")
    }

    "have the correct section header for the Departure journey" in {
      val departureView = page(form, "some-reference", None)(journeyRequest(DepartureAnswers()), messages)
      departureView.getElementById("section-header") must containMessage("movement.sectionHeading.depart", "some-reference")
    }

    "display body text" in {
      createView().getElementById("code-body-para").text() mustBe messages("location.body.paragraph")
    }

    "display input hint" in {
      createView().getElementById("code-hint-para").text() mustBe messages("location.hint.paragraph")
    }

    "display goods location expander" in {
      createView().getElementsByClass("govuk-details__summary-text").first() must containHtml(messages("location.expander.title"))
    }

    "display 'Back' button that links to Movement Details" in {

      val backButton = createView().getBackButton

      backButton mustBe defined
      backButton.get must containMessage("site.back.previousQuestion")
      backButton.get must haveHref(routes.MovementDetailsController.displayPage)
    }

    checkAllSaveButtonsAreDisplayed(createView(journeyRequest(ArrivalAnswers(readyToSubmit = Some(true)))))

    checkSaveAndReturnToSummaryButtonIsHidden(createView())
  }
}
