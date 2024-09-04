/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers.routes.{ChoiceOnConsignmentController, ConsignmentReferencesController}
import forms.DucrPartChiefChoice.IsDucrPart
import forms.{DucrPartChiefChoice, SpecificDateTimeChoice}
import models.cache.{ArrivalAnswers, Cache, DepartureAnswers}
import models.requests.JourneyRequest
import play.api.data.Form
import play.api.mvc.AnyContentAsEmpty
import play.twirl.api.Html
import testdata.CommonTestData.validEori
import views.html.specific_date_and_time

class SpecificDateTimeViewSpec extends ViewSpec with Injector {

  private val page = instanceOf[specific_date_and_time]

  private val form: Form[SpecificDateTimeChoice] = SpecificDateTimeChoice.form()

  private implicit val request: JourneyRequest[AnyContentAsEmpty.type] = journeyRequest(ArrivalAnswers())

  private def createView(implicit request: JourneyRequest[_] = request): Html = page(form, "some-reference")

  "SpecificDateTime View on empty page" should {

    "have the page's title prefixed with 'Error:'" when {
      "the page has errors" in {
        val view = page(form.withGlobalError("error.summary.title"), "some-reference")
        view.head.getElementsByTag("title").first.text must startWith("Error: ")
      }
    }

    "display page title" in {
      createView().getElementsByTag("h1").first() must containMessage("specific.datetime.heading")
    }

    "have the correct section header for the Arrival journey" in {
      createView().getElementById("section-header") must containMessage("specific.datetime.arrive.heading", "some-reference")
    }

    "have the correct section header for the Departure journey" in {
      val departureView = page(form, "some-reference")(journeyRequest(DepartureAnswers()), messages)
      departureView.getElementById("section-header") must containMessage("specific.datetime.depart.heading", "some-reference")
    }

    "display 'Back' button that links to the /choice-on-consignment page" when {
      "on a 'Find a consignment' journey" in {
        implicit val request: JourneyRequest[AnyContentAsEmpty.type] = journeyRequest(ArrivalAnswers(), None, ucrBlockFromIleQuery = true)
        val backButton = createView.getBackButton

        backButton mustBe defined
        backButton.foreach { button =>
          button must haveHref(ChoiceOnConsignmentController.displayChoices)
          button must containMessage("site.back")
        }
      }
    }

    "display 'Back' button that links to the /ducr-part-details page" when {
      "on a NON-'Find a consignment' journey and" when {
        "the Ucr is of DucrPart type" in {
          val cache = Cache(validEori, Some(ArrivalAnswers()), None, ucrBlockFromIleQuery = false, Some(DucrPartChiefChoice(IsDucrPart)))
          implicit val request: JourneyRequest[AnyContentAsEmpty.type] = journeyRequest(cache)
          val backButton = createView.getBackButton

          backButton mustBe defined
          backButton.foreach { button =>
            button must haveHref(ConsignmentReferencesController.displayPage)
            button must containMessage("site.back.previousQuestion")
          }
        }
      }
    }

    "display 'Back' button that links to the /consignment-references page" when {
      "on a NON-'Find a consignment' journey and" when {
        "the Ucr is not of DucrPart type" in {
          val backButton = createView().getBackButton

          backButton mustBe defined
          backButton.foreach { button =>
            button must haveHref(ConsignmentReferencesController.displayPage)
            button must containMessage("site.back")
          }
        }
      }
    }

    checkAllSaveButtonsAreDisplayed(createView(journeyRequest(ArrivalAnswers(readyToSubmit = Some(true)))))

    checkSaveAndReturnToSummaryButtonIsHidden(createView())
  }
}
