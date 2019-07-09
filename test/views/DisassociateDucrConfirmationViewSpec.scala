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

import helpers.views.{CommonMessages, DisassociateDucrConfirmationMessages}
import play.twirl.api.Html
import views.declaration.spec.ViewSpec

class DisassociateDucrConfirmationViewSpec extends ViewSpec with DisassociateDucrConfirmationMessages with CommonMessages {

  private val page = injector.instanceOf[views.html.disassociate_ducr_confirmation]

  private def createView(reference: String): Html = page(reference)

  "Disassociate Ducr Confirmation View" should {

    "have a proper labels for messages" in {
      assertMessage(title, "Disassociation complete")
      assertMessage(heading, "The reference of the DUCR disassociated:")
      assertMessage(footNote, "You might want to take a screenshot of this for your records.")
    }

    "display page reference" in {
      getElementById(createView("GB123"), "reference").text() must be("GB123")
    }

    "display \"Back\" button that links to start page" in {

      val backButton = getElementById(createView("GB123"), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be(controllers.routes.DisassociateDucrController.displayPage().url)
    }

    "display \"Back to start\" button on page" in {

      val view = createView("GB123")

      val saveButton = getElementByCss(view, ".button")
      saveButton.text() must be(messages(continue))
    }
  }

}
