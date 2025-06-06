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
import forms.ConsignmentReferences
import forms.ConsignmentReferences.form
import forms.UcrType.Ducr
import models.cache.{ArrivalAnswers, JourneyType}
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.{Form, FormError}
import play.api.mvc.AnyContentAsEmpty
import play.twirl.api.HtmlFormat.Appendable
import views.html.consignment_references

class ConsignmentReferenceViewSpec extends ViewSpec with Injector {

  private implicit val request: JourneyRequest[AnyContentAsEmpty.type] = journeyRequest(ArrivalAnswers())

  private val page = instanceOf[consignment_references]

  private val goodsDirection = JourneyType.ARRIVE

  def createView(frm: Form[ConsignmentReferences] = form(goodsDirection))(implicit request: JourneyRequest[_]): Appendable =
    page(frm)

  "View" should {

    "have the page's title prefixed with 'Error:'" when {
      "the page has errors" in {
        val view = createView(form(goodsDirection).withGlobalError("error.summary.title"))
        view.head.getElementsByTag("title").first.text must startWith("Error: ")
      }
    }

    "render title" in {
      createView().getTitle must containMessage("consignment.references.ARRIVE.question")
    }

    "render heading" in {
      createView().getElementById("section-header") must containMessage("consignment.references.ARRIVE.heading")
    }

    "render options" in {
      createView().getElementsByAttributeValue("for", "reference").first() must containMessage("consignment.references.ducr")
      createView().getElementsByAttributeValue("for", "reference-2").first() must containMessage("consignment.references.mucr")
    }

    "render labels" in {
      createView().getElementsByAttributeValue("for", "mucrValue").first() must containMessage("site.inputText.mucr.label")
      createView().getElementsByAttributeValue("for", "ducrValue").first() must containMessage("site.inputText.ducr.label")
    }

    "render hint above DUCR input" in {
      createView().getElementsByAttributeValue("id", "ducrValue-hint").first() must containMessage("consignment.references.ducr.hint")
    }

    "display DUCR invalid" in {
      val view: Document = createView(form(goodsDirection).fillAndValidate(ConsignmentReferences(Ducr, "incorrectDucr")))
      view must haveGovUkGlobalErrorSummary
      view must haveGovUkFieldError("ducrValue", messages("consignment.references.ducrValue.error"))
    }

    "render the back button" in {
      val backButton = createView().getBackButton

      backButton mustBe defined
      backButton.get must haveHref(backButtonDefaultCall)
      backButton.get must containMessage("site.back")
    }

    "render error summary" when {

      "no errors" in {
        createView().getErrorSummary mustBe empty
      }

      "some errors" in {
        val view: Document = createView(form(goodsDirection).withError(FormError("reference", "consignment.references.empty.arrive")))
        view must haveGovUkGlobalErrorSummary
        view must haveGovUkFieldError("reference", messages("consignment.references.empty.arrive"))
      }
    }

    "display the expander for Ducr / Mucr guidance" in {
      val expander = createView().getElementsByClass("govuk-details").first()
      expander.children.size mustBe 2

      expander.child(0).text mustBe messages("consignment.references.expander.title")

      val paragraph = expander.child(1)

      val prefix = "consignment.references.expander.content"
      val expectedText = messages(s"$prefix", messages(s"$prefix.link"))
      val actualText = removeBlanksIfAnyBeforeDot(paragraph.text)
      actualText mustBe expectedText

      val link = paragraph.child(0)
      link must haveHref(
        "https://www.gov.uk/government/publications/uk-trade-tariff-cds-volume-3-export-declaration-completion-guide/group-2-references-of-messages-document-certificates-and-authorisations"
      )
      link must haveAttribute("target", "_blank")
    }

    checkAllSaveButtonsAreDisplayed(createView()(journeyRequest(ArrivalAnswers(readyToSubmit = Some(true)))))

    checkSaveAndReturnToSummaryButtonIsHidden(createView())
  }
}
