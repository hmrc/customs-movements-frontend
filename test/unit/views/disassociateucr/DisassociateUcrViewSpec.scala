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

package views.disassociateucr

import base.OverridableInjector
import config.DucrPartConfig
import forms.DisassociateUcr
import forms.UcrType.{Ducr, Mucr}
import models.cache.DisassociateUcrAnswers
import org.jsoup.nodes.Document
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.bind
import play.twirl.api.Html
import views.ViewSpec
import views.html.disassociateucr.disassociate_ucr
import views.tags.ViewTest

@ViewTest
class DisassociateUcrViewSpec extends ViewSpec with MockitoSugar {

  private val config = mock[DucrPartConfig]
  private val injector = new OverridableInjector(bind[DucrPartConfig].toInstance(config))

  private implicit val request = journeyRequest(DisassociateUcrAnswers())

  private val disassociatePage: disassociate_ucr = injector.instanceOf[disassociate_ucr]

  private def createView(form: Form[DisassociateUcr]): Html = disassociatePage(form)(request, messages)

  "Disassociate Ucr View" when {

    "have a proper labels for messages" in {
      messages must haveTranslationFor("disassociate.ucr.title")
      messages must haveTranslationFor("disassociate.ucr.heading")
      messages must haveTranslationFor("disassociate.ucr.ducr")
      messages must haveTranslationFor("disassociate.ucr.ducr.hint")
      messages must haveTranslationFor("disassociate.ucr.mucr")
    }

    "display 'Back' button that links to Choice when ducrPart disabled" in {
      when(config.isDucrPartsEnabled).thenReturn(false)
      val backButton = createView(DisassociateUcr.form).getBackButton

      backButton mustBe defined
      backButton.foreach { button =>
        button must haveHref(controllers.routes.ChoiceController.displayChoiceForm())
        button must containMessage("site.back")
      }
    }

    "display 'Back' button that links to Ducr Part Chief when ducrPart enabled" in {
      when(config.isDucrPartsEnabled).thenReturn(true)
      val backButton = createView(DisassociateUcr.form).getBackButton

      backButton mustBe defined
      backButton.foreach { button =>
        button must haveHref(controllers.routes.DucrPartChiefController.displayPage())
        button must containMessage("site.back")
      }
    }

    "form is empty" should {
      val emptyView = createView(DisassociateUcr.form)

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
      val mucrView = createView(DisassociateUcr.form.fill(DisassociateUcr(Mucr, ducr = None, mucr = Some("1234"))))
      "display value" in {
        mucrView.getElementById("mucr").`val`() mustBe "1234"
      }
    }

    "form contains input text labels" in {
      val mucrView = createView(DisassociateUcr.form.fill(DisassociateUcr(Mucr, ducr = None, mucr = Some("1234"))))
      mucrView.getElementsByAttributeValue("for", "mucr").first() must containMessage("site.inputText.mucr.label")
      mucrView.getElementsByAttributeValue("for", "ducr").first() must containMessage("site.inputText.ducr.label")
    }

    "form contains 'DUCR' with value" should {
      val ducrView = createView(DisassociateUcr.form.fill(DisassociateUcr(Ducr, ducr = Some("1234"), mucr = None)))
      "display value" in {
        ducrView.getElementById("ducr").`val`() mustBe "1234"
      }
    }

    "display DUCR empty" in {
      val view: Document = createView(DisassociateUcr.form.fillAndValidate(DisassociateUcr(Ducr, ducr = Some(""), mucr = None)))

      view must haveGovUkGlobalErrorSummary
      view must haveGovUkFieldError("ducr", messages("disassociate.ucr.ducr.empty"))
    }

    "display DUCR invalid" in {
      val view: Document = createView(DisassociateUcr.form.fillAndValidate(DisassociateUcr(Ducr, ducr = Some("DUCR"), mucr = None)))

      view must haveGovUkGlobalErrorSummary
      view must haveGovUkFieldError("ducr", messages("disassociate.ucr.ducr.error"))
    }

    "display MUCR empty" in {
      val view: Document = createView(DisassociateUcr.form.fillAndValidate(DisassociateUcr(Mucr, ducr = None, mucr = Some(""))))

      view must haveGovUkGlobalErrorSummary
      view must haveGovUkFieldError("mucr", messages("disassociate.ucr.mucr.empty"))
    }

    "display MUCR invalid" in {
      val view: Document = createView(DisassociateUcr.form.fillAndValidate(DisassociateUcr(Mucr, ducr = None, mucr = Some("MUCR"))))

      view must haveGovUkGlobalErrorSummary
      view must haveGovUkFieldError("mucr", messages("disassociate.ucr.mucr.error"))
    }
  }

}
