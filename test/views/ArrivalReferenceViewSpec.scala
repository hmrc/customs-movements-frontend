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

import forms.ArrivalReference
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.twirl.api.Html
import utils.{Injector, Stubs}
import views.base.UnitViewSpec
import views.html.arrival_reference

class ArrivalReferenceViewSpec extends UnitViewSpec with Stubs with Injector {

  val arrivalReferencePage = new arrival_reference(mainTemplate)
  def createView(form: Form[ArrivalReference] = ArrivalReference.form): Html =
    arrivalReferencePage(form)(request, messages)

  "Arrival Reference messages" should {

    "have correct content" in {

      val messages = instanceOf[MessagesApi].preferred(request)

      messages("arrivalReference") mustBe "Arrival reference"
      messages("arrivalReference.question") mustBe "Give this arrival a unique reference"
      messages("arrivalReference.hint") mustBe "This will be help you quickly identify it in the future. It can be no more than 25 characters. Leave it blank if you don't want to add a reference."
      messages("arrivalReference.error.format") mustBe "The reference should be no more than 25 characters."
    }
  }

  "Arrival Reference page" should {

    "have title" in {

      createView().getElementsByTag("title").text() mustBe messages("arrivalReference")
    }

    "have question" in {

      createView().getElementById("reference-label").text() mustBe messages("arrivalReference.question")
    }

    "have hint" in {

      createView().getElementById("reference-hint").text() mustBe messages("arrivalReference.hint")
    }

    "have save and continue button" in {

      createView().getElementById("submit").text() mustBe messages("site.save_and_continue")
    }

    "have back button" in {

      val backButton = createView().getElementById("link-back")

      backButton.text() mustBe messages("site.back")
      backButton.attr("href") must endWith("/consignment-references")
    }
  }
}