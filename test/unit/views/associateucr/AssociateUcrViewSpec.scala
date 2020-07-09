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
import forms.UcrType._
import forms.{AssociateUcr, MucrOptions}
import org.jsoup.nodes.Document
import play.api.data.{Form, FormError}
import play.twirl.api.Html
import views.html.associateucr.associate_ucr
import views.spec.{UnitViewSpec, ViewMatchers}
import views.tags.ViewTest

@ViewTest
class AssociateUcrViewSpec extends UnitViewSpec with ViewMatchers with Injector {

  private val page = instanceOf[associate_ucr]

  val mucrOptions = MucrOptions("MUCR")

  private def createView(mucr: MucrOptions, form: Form[AssociateUcr]): Html =
    page(form, mucr)(request, messages)

  "Associate Ucr View" when {

    "have a proper labels for messages" in {
      val messages = messagesApi.preferred(request)
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
          emptyView.getElementsByAttributeValue("for", "kind").text() mustBe "associate.ucr.ducr"
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
          emptyView.getElementsByAttributeValue("for", "kind-2").text() mustBe "associate.ucr.mucr"
        }
        "have input" in {
          emptyView.getElementById("mucr").`val`() mustBe empty
        }
      }

      "display 'Continue' button on page" in {
        emptyView.getElementsByClass("govuk-button").text() mustBe "site.continue"
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
      val value = AssociateUcr.form.withError(FormError("ducr", "ducr.error.empty"))

      val view: Document = createView(mucrOptions, value)

      view must haveGovUkGlobalErrorSummary
      view must haveGovUkFieldError("ducr", "ducr.error.empty")
    }
  }

}
