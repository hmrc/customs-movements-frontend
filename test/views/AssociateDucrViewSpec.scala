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

import forms.AssociateDucr
import helpers.views.{AssociateDucrMessages, CommonMessages}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.tags.ViewTest

@ViewTest
class AssociateDucrViewSpec extends ViewSpec with AssociateDucrMessages with CommonMessages {

  private val page = injector.instanceOf[views.html.associate_ducr]

  private def createView(mucr: String, form: Form[AssociateDucr]): Html =
    page(form, mucr)(appConfig, fakeRequest, messages)

  "Disassociate Ducr Confirmation View" should {

    "have a proper labels for messages" in {
      messages(title, "{MUCR}") mustBe "Add a DUCR to {MUCR}"
      messages(hint) mustBe "Make sure you have entered the right MUCR details before adding a DUCR."
    }

    "display 'Add' button on page" in {
      val view: Document = createView("MUCR", AssociateDucr.form)

      view.getElementsByClass("button").text() must be(messages(add))
    }

    "display DUCR Form errors" in {
      val view: Document = createView("MUCR", AssociateDucr.form.fillAndValidate(AssociateDucr("")))

      view must haveGlobalErrorSummary
      view must haveFieldError("ducr", "Please enter a reference")
    }
  }

}
