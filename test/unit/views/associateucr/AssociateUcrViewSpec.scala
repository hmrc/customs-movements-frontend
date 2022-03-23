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

package views.associateucr

import base.Injector
import forms.UcrType._
import forms.{AssociateUcr, MucrOptions}
import models.cache.AssociateUcrAnswers
import org.jsoup.nodes.Document
import play.api.data.{Form, FormError}
import play.twirl.api.Html
import views.ViewSpec
import views.html.associateucr.associate_ucr
import views.tags.ViewTest

@ViewTest
class AssociateUcrViewSpec extends ViewSpec with Injector {

  private implicit val request = journeyRequest(AssociateUcrAnswers())

  private val page = instanceOf[associate_ucr]

  val mucrOptions = MucrOptions(MucrOptions.Create, "MUCR")

  private def createView(mucr: MucrOptions, form: Form[AssociateUcr]): Html =
    page(form, mucr)(request, messages)

  "Associate Ucr View" when {

    "have a proper labels for messages" in {
      messages must haveTranslationFor("associate.ucr.title")
      messages must haveTranslationFor("associate.ucr.hint")
    }

    "form is empty" should {
      val emptyView = createView(mucrOptions, AssociateUcr.form)
      "have 'DUCR' section" which {
        "have radio button" in {
          emptyView.getElementById("kind") mustBe unchecked
        }

        "display label" in {
          emptyView.getElementsByAttributeValue("for", "kind").text() mustBe messages("associate.ucr.ducr")
        }
        "have input for value" in {
          emptyView.getElementById("ducr").`val`() mustBe empty
        }
      }

      "have 'MUCR' section" which {
        "have radio button" in {
          emptyView.getElementById("kind-2") mustBe unchecked
        }
        "display label" in {
          emptyView.getElementsByAttributeValue("for", "kind-2").text() mustBe messages("associate.ucr.mucr")
        }
        "have input" in {
          emptyView.getElementById("mucr").`val`() mustBe empty
        }
      }

      "display 'Continue' button on page" in {
        emptyView.getElementsByClass("govuk-button").text() mustBe messages("site.continue")
      }
    }

    "form contains 'MUCR' with value" should {
      val mucrView = createView(mucrOptions, AssociateUcr.form.fill(AssociateUcr(Mucr, "1234")))
      "display value" in {
        mucrView.getElementById("mucr").`val`() mustBe "1234"
      }
    }

    "form contains input text labels" in {
      val mucrView = createView(mucrOptions, AssociateUcr.form.fill(AssociateUcr(Mucr, "1234")))
      mucrView.getElementsByAttributeValue("for", "mucr").first() must containMessage("site.inputText.mucr.label")
      mucrView.getElementsByAttributeValue("for", "ducr").first() must containMessage("site.inputText.ducr.label")
    }

    "form contains 'DUCR' with value" should {
      val ducrView = createView(mucrOptions, AssociateUcr.form.fill(AssociateUcr(Ducr, "1234")))
      "display value" in {
        ducrView.getElementById("ducr").`val`() mustBe "1234"
      }
    }

    "display DUCR Form errors" in {
      val value = AssociateUcr.form.withError(FormError("ducr", "associate.ucr.ducr.error.invalid"))

      val view: Document = createView(mucrOptions, value)

      view must haveGovUkGlobalErrorSummary
      view must haveGovUkFieldError("ducr", messages("associate.ucr.ducr.error.invalid"))
    }
  }

}
