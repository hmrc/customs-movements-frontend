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

import helpers.views.CommonMessages
import play.twirl.api.Html
import views.spec.UnitViewSpec
import views.tags.ViewTest
import forms.AssociateKind._
import forms.AssociateUcr

@ViewTest
class AssociateDucrSummaryViewSpec extends UnitViewSpec with CommonMessages {

  private val page = new views.html.associate_ducr_summary(mainTemplate)

  private def createView(mucr: String, ducr: String): Html =
    page(AssociateUcr(Ducr, ducr), mucr)(request, messages)

  "Associate Ducr Confirmation View" should {

    "have a proper labels for messages" in {
      val realMessages = messagesApi.preferred(request)

      realMessages must haveTranslationFor("associate.ucr.summary.title")
      realMessages must haveTranslationFor("associate.ucr.summary.kind.mucr")
      realMessages must haveTranslationFor("associate.ucr.summary.kind.ducr")
      realMessages must haveTranslationFor("associate.ucr.summary.addConsignment")
      realMessages must haveTranslationFor("associate.ucr.summary.masterConsignment")
    }

    val view = createView("MUCR", "DUCR")

    "display 'Confirm and submit' button on page" in {
      view.getElementsByClass("button").text() mustBe messages(confirmAndSubmitCaption)
    }

    "display 'Change' link on page for associate ucr" in {
      view.getElementById("associate_ducr-change") must containText(messages(changeCaption))
      view.getElementById("associate_ducr-change") must haveHref(controllers.consolidations.routes.AssociateDucrController.displayPage())
    }

    "display 'Change' link on the page for mucr" in {

      view.getElementById("mucr-change") must containText(messages(changeCaption))
      view.getElementById("mucr-change") must haveHref(controllers.consolidations.routes.MucrOptionsController.displayPage())
    }

    "display 'Reference' link on page" in {
      view.getElementById("associate_ucr-reference") must containText("DUCR")
    }

    "display mucr type on the page" in {

      view.getElementById("mucr-type") must containText("associate.ucr.summary.kind.mucr")
    }
  }
}
