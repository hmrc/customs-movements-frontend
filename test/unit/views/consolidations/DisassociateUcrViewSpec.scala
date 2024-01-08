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

package views.consolidations

import base.Injector
import controllers.routes.DucrPartChiefController
import forms.DisassociateUcr
import forms.UcrType.{Ducr, Mucr}
import models.cache.DisassociateUcrAnswers
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.Html
import views.ViewSpec
import views.html.consolidations.disassociate_ucr
import views.tags.ViewTest

@ViewTest
class DisassociateUcrViewSpec extends ViewSpec with Injector {

  private implicit val request = journeyRequest(DisassociateUcrAnswers())

  private val form = DisassociateUcr.form
  private val disassociatePage: disassociate_ucr = instanceOf[disassociate_ucr]

  private def createView(form: Form[DisassociateUcr]): Html = disassociatePage(form)(request, messages)

  "Disassociate Ucr View" when {

    "have a proper labels for messages" in {
      messages must haveTranslationFor("disassociate.ucr.title")
      messages must haveTranslationFor("disassociate.ucr.heading")
      messages must haveTranslationFor("disassociate.ucr.ducr")
      messages must haveTranslationFor("disassociate.ucr.ducr.hint")
      messages must haveTranslationFor("disassociate.ucr.mucr")
    }

    "the page has errors" should {
      "have the page's title prefixed with 'Error:'" in {
        val view = createView(form.withGlobalError("error.summary.title"))
        view.head.getElementsByTag("title").first.text must startWith("Error: ")
      }
    }

    "display 'Back' button that links to Ducr Part Chief" in {
      val backButton = createView(form).getBackButton

      backButton mustBe defined
      backButton.foreach { button =>
        button must haveHref(DucrPartChiefController.displayPage)
        button must containMessage("site.back.previousQuestion")
      }
    }

    "form is empty" should {
      val emptyView = createView(form)

      "have title" in {
        emptyView.getTitle must containMessage("disassociate.ucr.title")
      }

      "have section header" in {
        emptyView.getElementById("section-header") must containMessage("disassociate.ucr.heading")
      }

      "have heading" in {
        emptyView.getElementsByTag("h1").text() mustBe messages("disassociate.ucr.title")
      }

      "have 'DUCR' section" which {
        "have radio button" in {
          emptyView.getElementById("kind") mustBe unchecked
        }

        "display label" in {
          emptyView.getElementsByAttributeValue("for", "kind").first() must containMessage("disassociate.ucr.ducr")
        }
        "have input for value" in {
          emptyView.getElementById("ducr").`val`() mustBe empty
        }

        "have hint text" in {
          emptyView.getElementById("ducr-hint") must containMessage("disassociate.ucr.ducr.hint")
        }
      }

      "have 'MUCR' section" which {
        "have radio button" in {
          emptyView.getElementById("kind-2") mustBe unchecked
        }
        "display label" in {
          emptyView.getElementsByAttributeValue("for", "kind-2").first() must containMessage("disassociate.ucr.mucr")
        }
        "have input" in {
          emptyView.getElementById("mucr").`val`() mustBe empty
        }
      }

      "display 'Continue' button on page" in {
        emptyView.getSubmitButton mustBe defined
        emptyView.getSubmitButton.get must containMessage("site.continue")
      }
    }

    "form contains 'MUCR' with value" should {
      val mucrView = createView(form.fill(DisassociateUcr(Mucr, ducr = None, mucr = Some("1234"))))
      "display value" in {
        mucrView.getElementById("mucr").`val`() mustBe "1234"
      }
    }

    "form contains input text labels" in {
      val mucrView = createView(form.fill(DisassociateUcr(Mucr, ducr = None, mucr = Some("1234"))))
      mucrView.getElementsByAttributeValue("for", "mucr").first() must containMessage("site.inputText.mucr.label")
      mucrView.getElementsByAttributeValue("for", "ducr").first() must containMessage("site.inputText.ducr.label")
    }

    "form contains 'DUCR' with value" should {
      val ducrView = createView(form.fill(DisassociateUcr(Ducr, ducr = Some("1234"), mucr = None)))
      "display value" in {
        ducrView.getElementById("ducr").`val`() mustBe "1234"
      }
    }

    "display DUCR empty" in {
      val view: Document = createView(form.fillAndValidate(DisassociateUcr(Ducr, ducr = Some(""), mucr = None)))

      view must haveGovUkGlobalErrorSummary
      view must haveGovUkFieldError("ducr", messages("disassociate.ucr.ducr.empty"))
    }

    "display DUCR invalid" in {
      val view: Document = createView(form.fillAndValidate(DisassociateUcr(Ducr, ducr = Some("DUCR"), mucr = None)))

      view must haveGovUkGlobalErrorSummary
      view must haveGovUkFieldError("ducr", messages("disassociate.ucr.ducr.error"))
    }

    "display MUCR empty" in {
      val view: Document = createView(form.fillAndValidate(DisassociateUcr(Mucr, ducr = None, mucr = Some(""))))

      view must haveGovUkGlobalErrorSummary
      view must haveGovUkFieldError("mucr", messages("disassociate.ucr.mucr.empty"))
    }

    "display MUCR invalid" in {
      val view: Document = createView(form.fillAndValidate(DisassociateUcr(Mucr, ducr = None, mucr = Some("MUCR"))))

      view must haveGovUkGlobalErrorSummary
      view must haveGovUkFieldError("mucr", messages("disassociate.ucr.mucr.error"))
    }
  }
}
