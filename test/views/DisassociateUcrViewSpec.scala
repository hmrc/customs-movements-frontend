/*
 * Copyright 2019 HM Revenue & Customs
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

import forms.DisassociateKind.{Ducr, Mucr}
import forms.DisassociateUcr
import helpers.views.CommonMessages
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.Html
import views.spec.{UnitViewSpec, ViewMatchers}
import views.tags.ViewTest

@ViewTest
class DisassociateUcrViewSpec extends UnitViewSpec with CommonMessages with ViewMatchers {

  private val page = new views.html.disassociate_ucr(mainTemplate)

  private def createView(form: Form[DisassociateUcr]): Html =
    page(form)(request, messages)

  "Disassociate Ucr View" when {

    "have a proper labels for messages" in {
      val messages = messagesApi.preferred(request)
      messages must haveTranslationFor("disassociate.ucr.title")
      messages must haveTranslationFor("disassociate.ucr.heading")
    }

    "form is empty" should {
      val emptyView = createView(DisassociateUcr.form)
      "have 'DUCR' section" which {
        "have radio button" in {
          emptyView.getElementById("disassociate.ucr.ducr") mustBe unchecked
        }

        "display label" in {
          emptyView.getElementsByAttributeValue("for", "disassociate.ucr.ducr").text() mustBe "disassociate.ucr.ducr"
        }
        "have input for value" in {
          emptyView.getElementById("ducr").`val`() mustBe empty
        }
      }

      "have 'MUCR' section" which {
        "have radio button" in {
          emptyView.getElementById("disassociate.ucr.mucr") mustBe unchecked
        }
        "display label" in {
          emptyView.getElementsByAttributeValue("for", "disassociate.ucr.mucr").text() mustBe "disassociate.ucr.mucr"
        }
        "have input" in {
          emptyView.getElementById("mucr").`val`() mustBe empty
        }
      }

      "display 'Save and Continue' button on page" in {
        emptyView.getElementsByClass("button").text() mustBe saveAndContinueCaption
      }
    }

    "form contains 'MUCR' with value" should {
      val mucrView = createView(DisassociateUcr.form.fill(DisassociateUcr(Mucr, ducr = None, mucr = Some("1234"))))
      "display value" in {
        mucrView.getElementById("mucr").`val`() mustBe "1234"
      }
    }

    "form contains 'DUCR' with value" should {
      val ducrView = createView(DisassociateUcr.form.fill(DisassociateUcr(Ducr, ducr = Some("1234"), mucr = None)))
      "display value" in {
        ducrView.getElementById("ducr").`val`() mustBe "1234"
      }
    }

    "display DUCR Form errors" in {
      val view: Document = createView(DisassociateUcr.form.fillAndValidate(DisassociateUcr(Ducr, ducr = Some(""), mucr = None)))

      view must haveGlobalErrorSummary
      view must haveFieldError("ducr", "disassociate.ucr.ducr.error")
    }

    "display MUCR Form errors" in {
      val view: Document = createView(DisassociateUcr.form.fillAndValidate(DisassociateUcr(Mucr, ducr = None, mucr = Some(""))))

      view must haveGlobalErrorSummary
      view must haveFieldError("mucr", "disassociate.ucr.mucr.error")
    }
  }

}
