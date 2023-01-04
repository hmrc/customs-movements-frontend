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
import controllers.routes.ChoiceController
import forms.DucrPartChiefChoice
import models.cache.{ArrivalAnswers, AssociateUcrAnswers, DepartureAnswers, DisassociateUcrAnswers}
import org.jsoup.nodes.Document
import play.api.data.{Form, FormError}
import views.html.ducr_part_chief

class DucrPartChiefViewSpec extends ViewSpec with Injector {

  private implicit val request = journeyRequest(ArrivalAnswers())
  private val requestReadyToSubmit = journeyRequest(ArrivalAnswers(readyToSubmit = Some(true)))

  private val page = instanceOf[ducr_part_chief]
  private val form: Form[DucrPartChiefChoice] = DucrPartChiefChoice.form()

  "DucrPartChiefView options" should {

    Seq(ArrivalAnswers(), DepartureAnswers(), AssociateUcrAnswers(), DisassociateUcrAnswers()).foreach { answers =>
      s"have the correct title with $answers" in {
        implicit val request = journeyRequest(answers)
        page(form).getTitle must containMessage(s"ducrPartChief.${request.answers.`type`.toString}.question")
      }

      s"have the correct heading with $answers" in {
        implicit val request = journeyRequest(answers)
        page(form).getElementById("section-header") must containMessage(s"ducrPartChief.${request.answers.`type`.toString}.heading", "mucr")
      }

      s"render the correct labels with $answers" in {
        implicit val request = journeyRequest(answers)
        val view = page(form)
        view.getElementsByAttributeValue("for", "choice").first() must containMessage("ducrPartChief.isDucrPart")
        view.getElementsByAttributeValue("for", "choice-2").first() must containMessage(
          s"ducrPartChief.${request.answers.`type`.toString}.notDucrPart"
        )
      }
    }

    "have the correct body text" in {
      page(form).getElementById("code-body-para").text mustBe messages("ducrPartChief.bodyParagraph")
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
