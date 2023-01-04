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
import controllers.routes.DucrPartChiefController
import forms.DucrPartDetails
import models.cache._
import models.requests.RequestWithAnswers
import org.jsoup.nodes.Document
import play.api.data.{Form, FormError}
import play.twirl.api.Html
import testdata.CommonTestData._
import views.html.ducr_part_details
import views.spec.ViewMatchers
import views.tags.ViewTest

@ViewTest
class DucrPartDetailsViewSpec extends ViewSpec with Injector with ViewMatchers {

  private val page = instanceOf[ducr_part_details]

  private def createView(form: Form[DucrPartDetails])(implicit request: RequestWithAnswers[_]): Html = page(form)

  "DucrPartDetails view" when {

    implicit val request = journeyRequest(ArrivalAnswers())

    "provided with empty form" should {
      val view = createView(DucrPartDetails.form())

      "render title" in {
        view.getTitle must containMessage("ducrPartDetails.title")
      }

      "render section header" in {
        for (answers <- Seq(ArrivalAnswers(), DepartureAnswers(), AssociateUcrAnswers(), DisassociateUcrAnswers())) {
          implicit val request = journeyRequest(answers)

          val view = createView(DucrPartDetails.form())

          val text = view.getElementById("section-header")
          text must containMessage(s"ducrPartDetails.${request.answers.`type`.toString.toLowerCase}.heading")
        }
      }

      "render heading" in {
        view.getElementById("title") must containMessage("ducrPartDetails.title")
      }

      "render page hint" in {
        view.getElementById("page-hint") must containMessage("ducrPartDetails.heading")
      }

      "render 'Back' button leading to 'Ducr Part Chief' page" in {
        val view = createView(DucrPartDetails.form())

        view.getBackButton mustBe defined
        view.getBackButton.get must haveHref(DucrPartChiefController.displayPage)
        view.getBackButton.get must containMessage("site.back.previousQuestion")
      }

      "render DUCR input field label" in {
        view.getElementsByAttributeValue("for", "ducr").first() must containMessage("ducrPartDetails.ducr")
      }

      "render DUCR input field hint" in {
        view.getElementById("ducr-hint") must containMessage("ducrPartDetails.ducr.hint")
      }

      "render empty DUCR input field" in {
        view.getElementById("ducr").`val`() mustBe empty
      }

      "render DUCR Part ID input field label" in {
        view.getElementsByAttributeValue("for", "ducrPartId").first() must containMessage("ducrPartDetails.ducrPartId")
      }

      "render DUCR Part ID input field hint" in {
        view.getElementById("ducrPartId-hint") must containMessage("ducrPartDetails.ducrPartId.hint")
      }

      "render empty DUCR Part ID input field" in {
        view.getElementById("ducrPartId").`val`() mustBe empty
      }

      "render 'Help with DUCR' details expander" in {
        val text = view.getElementsByClass("govuk-details__text").first()
        text must containMessage("ducrPartDetails.details.text", messages("ducrPartDetails.details.text.link"))
        text.child(0) must haveHref(
          "https://www.gov.uk/government/publications/uk-trade-tariff-exports/uk-trade-tariff-exports#unique-consignment-reference-ucr-numbers"
        )
        text.child(0) must haveAttribute("target", "_blank")
      }

      checkAllSaveButtonsAreDisplayed(createView(DucrPartDetails.form())(journeyRequest(ArrivalAnswers(readyToSubmit = Some(true)))))

      checkSaveAndReturnToSummaryButtonIsHidden(createView(DucrPartDetails.form()))
    }

    "provided with filled form" should {
      val form = DucrPartDetails.form().fill(DucrPartDetails(ducr = validDucr, ducrPartId = validDucrPartId))
      val view = createView(form)

      "fill DUCR input field" in {
        view.getElementById("ducr").`val`() mustBe validDucr
      }

      "fill DUCR Part ID input field" in {
        view.getElementById("ducrPartId").`val`() mustBe validDucrPartId
      }
    }

    "provided with form containing DUCR error" should {
      val form = DucrPartDetails.form().withError(FormError("ducr", "ducrPartDetails.ducr.invalid"))
      val view: Document = createView(form)

      "render error summary" in {
        view must haveGovUkGlobalErrorSummary
      }

      "render field error" in {
        view must haveGovUkFieldError("ducr", messages("ducrPartDetails.ducr.invalid"))
      }
    }

    "provided with form containing DUCR Part ID error" should {
      val form = DucrPartDetails.form().withError(FormError("ducrPartId", "ducrPartDetails.ducrPartId.invalid"))
      val view: Document = createView(form)

      "render error summary" in {
        view must haveGovUkGlobalErrorSummary
      }

      "render field error" in {
        view must haveGovUkFieldError("ducrPartId", messages("ducrPartDetails.ducrPartId.invalid"))
      }
    }
  }
}
