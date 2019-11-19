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

import forms.DisassociateKind._
import forms.DisassociateUcr
import helpers.views.CommonMessages
import play.twirl.api.Html
import views.spec.UnitViewSpec
import views.tags.ViewTest

@ViewTest
class DisassociateUcrSummaryViewSpec extends UnitViewSpec with CommonMessages {

  private val page = new views.html.disassociate_ucr_summary(mainTemplate)

  private def createView(ducr: String): Html =
    page(DisassociateUcr(Ducr, ducr = Some(ducr), mucr = None))(request, messages)

  "Disassociate Ucr Summary View" should {

    "have a proper labels for messages" in {
      val realMessages = messagesApi.preferred(request)

      realMessages must haveTranslationFor("disassociate.ucr.summary.title")
      realMessages must haveTranslationFor("disassociate.ucr.summary.table.caption")
    }

    val view = createView("SOME-DUCR")

    "display 'Confirm and submit' button on page" in {
      view.getElementsByClass("button").text() must be(messages(confirmAndSubmitCaption))
    }

    "display 'Change' link on page" in {
      view.getElementById("disassociate_ucr-remove") must containMessage(changeCaption)
      view.getElementById("disassociate_ucr-remove") must haveAttribute(
        "href",
        controllers.consolidations.routes.DisassociateUcrController.displayPage().url
      )
    }

    "display 'Reference' link on page" in {
      view.getElementById("disassociate_ucr-reference") must containText("SOME-DUCR")
    }

  }

}
