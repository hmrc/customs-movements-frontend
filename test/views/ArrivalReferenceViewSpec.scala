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

import controllers.routes
import forms.ArrivalReference
import play.api.data.Form
import play.twirl.api.Html
import views.html.arrival_reference
import views.spec.UnitViewSpec

class ArrivalReferenceViewSpec extends UnitViewSpec {

  val arrivalReferencePage = new arrival_reference(mainTemplate)
  def createView(form: Form[ArrivalReference] = ArrivalReference.form): Html =
    arrivalReferencePage(form)(request, messages)

  "Arrival Reference messages" should {

    "have correct content" in {

      val messages = messagesApi.preferred(request)

      messages must haveTranslationFor("arrivalReference")
      messages must haveTranslationFor("arrivalReference.question")
      messages must haveTranslationFor("arrivalReference.hint")
      messages must haveTranslationFor("arrivalReference.error")
    }
  }

  "Arrival Reference page" should {

    "display same page title as header" in {

      val view = arrivalReferencePage(ArrivalReference.form)(request, messagesApi.preferred(request))
      view.title() must include(view.getElementsByTag("h1").text())
    }

    "have question" in {

      createView().getElementById("reference-label").text() mustBe messages("arrivalReference.question")
    }

    "have hint" in {

      createView().getElementById("reference-hint").text() mustBe messages("arrivalReference.hint")
    }

    "have 'Continue' button" in {

      createView().getElementById("submit").text() mustBe messages("site.continue")
    }

    "have back button" in {

      val backButton = createView().getElementById("back-link")

      backButton must containMessage("site.back")
      backButton must haveHref(routes.ConsignmentReferencesController.displayPage())
    }
  }
}
