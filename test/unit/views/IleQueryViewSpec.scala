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
import forms.IleQueryForm
import org.jsoup.nodes.Element
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.html.ile_query
import views.tags.ViewTest

@ViewTest
class IleQueryViewSpec extends ViewSpec with Injector with MockitoSugar {

  private implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val page = instanceOf[ile_query]
  private def view: Html = page(IleQueryForm.form)

  "Ile Query page" should {

    "render title" in {
      view.getTitle must containMessage("ileQuery.title")
    }

    "render a 'Back' button" in {
      view.getElementById("back-link") must haveAttribute("href", ChoiceController.displayChoices.url)
      view.getElementById("back-link") must containMessage("site.back.toStartPage")
    }

    "render page header" in {
      view.getElementsByClass("govuk-label--xl").first.text mustBe messages("ileQuery.title")
    }

    "render error summary" when {

      "no errors" in {
        val govukErrorSummary: Element = view.getElementsByClass("govuk-error-summary__title").first
        Option(govukErrorSummary) mustBe None
      }

      "some errors" in {
        val errorView = page(IleQueryForm.form.withError("error", "error.required"))
        val govukErrorSummary = errorView.getElementsByClass("govuk-error-summary__title").first
        govukErrorSummary.text mustBe messages("error.summary.title")
      }
    }

    "contain input field" in {
      Option(view.getElementById("ucr")) mustBe defined
    }

    "contain input field hint" in {
      view.getElementById("ucr-hint").html.contains(messages("ileQuery.hint"))
    }

    "contain submit button" in {
      view.getSubmitButton mustBe defined
      view.getSubmitButton.get must containMessage("site.continue")
    }
  }
}
