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

package views.consolidations

import base.Injector
import forms.ShutMucr
import forms.ShutMucr.form
import models.cache.ShutMucrAnswers
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.{Form, FormError}
import play.api.mvc.AnyContentAsEmpty
import play.twirl.api.HtmlFormat.Appendable
import views.ViewSpec
import views.html.consolidations.shut_mucr

class ShutMucrViewSpec extends ViewSpec with Injector {

  private implicit val request: JourneyRequest[AnyContentAsEmpty.type] = journeyRequest(ShutMucrAnswers())

  private val page = instanceOf[shut_mucr]

  def createView(frm: Form[ShutMucr] = form())(implicit request: JourneyRequest[_]): Appendable = page(frm)

  "View" should {

    "have the page's title prefixed with 'Error:'" when {
      "the page has errors" in {
        val view = createView(form().withGlobalError("error.summary.title"))
        view.head.getElementsByTag("title").first.text must startWith("Error: ")
      }
    }

    "render title" in {
      createView().getTitle must containMessage("shutMucr.title")
    }

    "render input for mucr" in {
      createView().getElementsByAttributeValue("for", "mucr").first() must containMessage("shutMucr.title")
    }

    "render back button" in {
      val backButton = createView().getBackButton

      backButton mustBe defined
      backButton.foreach { button =>
        button must haveHref(controllers.routes.ChoiceController.displayChoices)
        button must containMessage("site.back.toStartPage")
      }
    }

    "display the expander for Mucr guidance" in {
      val expander = createView().getElementsByClass("govuk-details").first()
      expander.children.size mustBe 2

      expander.child(0).text mustBe messages("shutMucr.expander.title")

      val paragraph = expander.child(1)

      val prefix = "shutMucr.expander.content"
      val expectedText = messages(s"$prefix", messages(s"$prefix.link"))
      val actualText = removeBlanksIfAnyBeforeDot(paragraph.text)
      actualText mustBe expectedText

      val link = paragraph.child(0)
      link must haveHref(
        "https://www.gov.uk/government/publications/uk-trade-tariff-cds-volume-3-export-declaration-completion-guide/group-2-references-of-messages-document-certificates-and-authorisations"
      )
      link must haveAttribute("target", "_blank")
    }

    "render error summary" when {
      "no errors" in {
        createView().getGovUkErrorSummary mustBe empty
      }

      "some errors" in {
        val view: Document = createView(form().withError(FormError("mucr", "error.mucr.empty")))

        view must haveGovUkGlobalErrorSummary
        view must haveGovUkFieldError("mucr", messages("error.mucr.empty"))
      }
    }

    "render submit button" in {
      val view = createView()

      view.getSubmitButton mustBe defined
      view.getSubmitButton.get must containMessage("site.continue")
    }
  }
}
