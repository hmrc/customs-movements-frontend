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

import forms.{AssociateUcr, MucrOptions}
import helpers.views.{AssociateDucrMessages, CommonMessages}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.Html
import views.spec.{UnitViewSpec, ViewMatchers}
import views.tags.ViewTest

@ViewTest
class AssociateUcrViewSpec extends UnitViewSpec with AssociateDucrMessages with CommonMessages with ViewMatchers {

  private val page = new views.html.associate_ucr(mainTemplate)

  val mucrOptions = MucrOptions("MUCR")

  private def createView(mucr: MucrOptions, form: Form[AssociateUcr]): Html =
    page(form, mucr)(request, messages)

  "Associate Ucr View" when {

    "have a proper labels for messages" in {
      val messages = messagesApi.preferred(request)
      messages must haveTranslationFor(title)
      messages must haveTranslationFor(hint)
    }

    "form is empty" should {
      val emptyView = createView(mucrOptions, AssociateUcr.form)
      "have 'DUCR' section" which {
        "have radio button" in {
          emptyView.getElementById("associate.ucr.ducr") mustBe unchecked
        }

        "display label" in {
          emptyView.getElementsByAttributeValue("for", "associate.ucr.ducr").text() mustBe "associate.ucr.ducr"
        }
        "have input for value" in {
          emptyView.getElementById("ducr").`val`() mustBe empty
        }
      }

      "have 'MUCR' section" which {
        "have radio button" in {
          emptyView.getElementById("associate.ucr.mucr") mustBe unchecked
        }
        "display label" in {
          emptyView.getElementsByAttributeValue("for", "associate.ucr.mucr").text() mustBe "associate.ucr.mucr"
        }
        "have input" in {
          emptyView.getElementById("mucr").`val`() mustBe empty
        }
      }

      "display 'Add' button on page" in {
        emptyView.getElementsByClass("button").text() mustBe add
      }
    }

    "form contains 'MUCR' with value" should {

    }

    "form contains 'DUCR' with value" should {

    }

    "display DUCR Form errors" in {
      val view: Document = createView(mucrOptions, AssociateUcr.form.fillAndValidate(AssociateUcr("")))

      view must haveGlobalErrorSummary
      view must haveFieldError("ducr", "mucrOptions.reference.value.empty")
    }
  }

}
