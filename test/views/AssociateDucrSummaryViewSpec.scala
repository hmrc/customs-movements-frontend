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

import forms.AssociateUcr
import helpers.views.{AssociateDucrSummaryMessages, CommonMessages}
import play.twirl.api.Html
import views.spec.UnitViewSpec
import views.tags.ViewTest
import forms.AssociateKind._
import forms.AssociateUcr

@ViewTest
class AssociateDucrSummaryViewSpec extends UnitViewSpec with AssociateDucrSummaryMessages with CommonMessages {

  private val page = new views.html.associate_ducr_summary(mainTemplate)

  private def createView(mucr: String, ducr: String): Html =
    page(AssociateUcr(Ducr, ducr = Some(ducr), mucr = None), mucr)(request, messages)

  "Associate Ducr Confirmation View" should {

    "have a proper labels for messages" in {
      val realMessages = messagesApi.preferred(request)
      realMessages(title, "{MUCR}") mustBe "The following UCR has been added to {MUCR}"
      realMessages(hint) mustBe "Make sure the details of the UCR are correct before continuing."
    }

    val view = createView("MUCR", "DUCR")

    "display 'Save and continue' button on page" in {
      view.getElementsByClass("button").text() must be(messages(continue))
    }

    "display 'Remove' link on page" in {
      view.getElementById("associate_ducr-remove") must containText(messages(remove))
      view.getElementById("associate_ducr-remove") must haveAttribute(
        "href",
        controllers.consolidations.routes.AssociateDucrController.displayPage().url
      )
    }

    "display 'Reference' link on page" in {
      view.getElementById("associate_ucr-reference") must containText("DUCR")
    }

  }

}
