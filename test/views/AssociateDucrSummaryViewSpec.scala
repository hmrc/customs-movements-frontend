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
import helpers.views.{AssociateDucrSummaryMessages, CommonMessages}
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.tags.ViewTest

@ViewTest
class AssociateDucrSummaryViewSpec extends ViewSpec with AssociateDucrSummaryMessages with CommonMessages {

  private val page = injector.instanceOf[views.html.associate_ducr_summary]

  private def createView(mucr: String, ducr: String): Html =
    page(AssociateDucr(ducr), mucr)(appConfig, fakeRequest, messages)

  "Disassociate Ducr Confirmation View" should {

    "have a proper labels for messages" in {
      messages(title, "{MUCR}") mustBe "Add a DUCR to {MUCR}"
    }

    "display 'Save and continue' button on page" in {
      val view = createView("MUCR", "DUCR")

      view.getElementsByClass("button").text() must be(messages(continue))
    }

    "display 'Remove' link on page" in {
      val view = createView("MUCR", "DUCR")

      view.getElementById("associate_ducr-remove") must containText(messages(remove))
      view.getElementById("associate_ducr-remove") must haveAttribute(
        "href",
        controllers.routes.AssociateDucrController.displayPage().url
      )
    }

    "display 'Reference' link on page" in {
      val view = createView("MUCR", "DUCR")

      view.getElementById("associate_ducr-reference") must containText("DUCR")
    }
  }

}
