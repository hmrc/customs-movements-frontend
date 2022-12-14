/*
 * Copyright 2022 HM Revenue & Customs
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
import controllers.routes.ChoiceController
import forms.DucrPartChiefChoice
import models.cache.ArrivalAnswers
import org.jsoup.nodes.Document
import play.api.data.{Form, FormError}
import views.html.ducr_part_chief

class DucrPartChiefViewSpec extends ViewSpec with Injector {

  private implicit val request = journeyRequest(ArrivalAnswers())
  private val requestReadyToSubmit = journeyRequest(ArrivalAnswers(readyToSubmit = Some(true)))

  private val page = instanceOf[ducr_part_chief]
  private val form: Form[DucrPartChiefChoice] = DucrPartChiefChoice.form()

  "DucrPartChiefView options" should {

    "have the correct title" in {
      page(form).getTitle must containMessage("ducrPartChief.ARRIVE.question")
    }

    "have the correct heading" in {
      page(form).getElementById("section-header") must containMessage("ducrPartChief.ARRIVE.heading", "mucr")
    }

    "have the correct body text" in {
      page(form).getElementById("code-body-para").text mustBe messages("ducrPartChief.bodyParagraph")
    }

    "render the correct labels" in {
      val view = page(form)
      view.getElementsByAttributeValue("for", "choice").first() must containMessage("ducrPartChief.isDucrPart")
      view.getElementsByAttributeValue("for", "choice-2").first() must containMessage("ducrPartChief.ARRIVE.notDucrPart")
    }

    "display 'Back' button" in {
      val backButton = page(form).getBackButton

      backButton mustBe defined
      backButton.foreach { button =>
        button must haveHref(ChoiceController.displayChoices)
        button must containMessage("site.back.toStartPage")
      }
    }

    "render error summary" when {
      "no errors" in {
        page(form).getErrorSummary mustBe empty
      }

      "some errors" in {
        val view: Document = page(form.withError(FormError("choice", "ducrPartChief.input.error.empty")))

        view must haveGovUkGlobalErrorSummary
        view must haveGovUkFieldError("choice", messages("ducrPartChief.input.error.empty"))
      }
    }

    checkAllSaveButtonsAreDisplayed(page(form)(requestReadyToSubmit, messages(requestReadyToSubmit)))

    checkSaveAndReturnToSummaryButtonIsHidden(page(form))

  }
}
