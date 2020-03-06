/*
 * Copyright 2020 HM Revenue & Customs
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

package views.associateucr

import base.Injector
import forms.MucrOptions
import models.UcrBlock
import models.cache.ArrivalAnswers
import org.jsoup.nodes.Document
import play.api.data.{Form, FormError}
import views.ViewSpec
import views.html.associateucr.mucr_options

class MucrOptionsViewSpec extends ViewSpec with Injector {

  private implicit val request = journeyRequest(ArrivalAnswers())

  private val form: Form[MucrOptions] = MucrOptions.form
  private val page = instanceOf[mucr_options]

  private val queryUcr = Some(UcrBlock("mucr", "m"))

  "MUCR options" should {

    "have the correct title" in {
      page(MucrOptions.form, queryUcr).getTitle must containMessage("mucrOptions.title")
    }

    "have the correct heading" in {
      page(MucrOptions.form, queryUcr).getElementById("section-header") must containMessage("mucrOptions.heading")
    }

    "render the correct labels and hints" in {
      val view = page(MucrOptions.form, queryUcr)
      view.getElementsByAttributeValue("for", "existingMucr").first() must containMessage("site.inputText.mucr.label")
      view.getElementsByAttributeValue("for", "newMucr").first() must containMessage("site.inputText.newMucr.label")
      view.getElementById("newMucr-hint") must containMessage("site.inputText.newMucr.label.hint")
    }

    "have no options selected on initial display" in {
      val view = page(MucrOptions.form, queryUcr)
      view.getElementById("createOrAdd") mustBe unchecked
      view.getElementById("createOrAdd-2") mustBe unchecked
    }

    "display 'Back' button that links to start page" in {
      val backButton = page(MucrOptions.form, queryUcr).getBackButton

      backButton mustBe defined
      backButton.foreach(button => {
        button must haveHref(controllers.routes.ChoiceController.displayChoiceForm())
        button must containMessage("site.back.toStartPage")
      })
    }

    "display 'Continue' button on page" in {
      page(MucrOptions.form, queryUcr).getElementsByClass("govuk-button").first() must containMessage("site.continue")
    }

    "render error summary" when {
      "no errors" in {
        page(MucrOptions.form, queryUcr).getErrorSummary mustBe empty
      }

      "some errors" in {
        val view: Document = page(MucrOptions.form.withError(FormError("createOrAdd", "mucrOptions.createAdd.value.empty")), queryUcr)

        view must haveGovUkGlobalErrorSummary
        view must haveGovUkFieldError("createOrAdd", messages("mucrOptions.createAdd.value.empty"))
      }
    }
  }
}
