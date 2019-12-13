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
import forms.ArrivalReference
import models.cache.ArrivalAnswers
import play.api.data.Form
import views.html.arrival_reference

class ArrivalReferenceViewSpec extends ViewSpec with Injector {

  private implicit val request = journeyRequest(ArrivalAnswers())

  private val page = instanceOf[arrival_reference]

  private def createPage = page(ArrivalReference.form, Some("some-reference"))

  "Arrival Reference page" should {

    "have the correct title" in {
      createPage.getTitle must containMessage("arrivalReference.question")
    }

    "have the correct header" in {
      createPage.getElementsByTag("h1").first() must containMessage("arrivalReference.question")
    }

    "have the correct section header" in {
      createPage.getElementById("section-header") must containMessage("arrivalReference.sectionHeading", "some-reference")
    }

    "render the correct labels and hints" in {
      createPage.getElementsByAttributeValue("for", "reference").first() must containMessage("arrivalReference.question")
      createPage.getElementById("reference-hint") must containMessage("arrivalReference.hint")
    }

    "display 'Back' button that links to start page" in {
      val backButton = createPage.getBackButton

      backButton mustBe defined
      backButton.get must haveHref(controllers.routes.ConsignmentReferencesController.displayPage())
    }

    "display 'Continue' button on page" in {
      createPage.getElementsByClass("govuk-button").first() must containMessage("site.continue")
    }

  }
}
