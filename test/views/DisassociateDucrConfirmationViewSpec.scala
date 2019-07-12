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
import controllers.storage.FlashKeys
import helpers.views.{CommonMessages, DisassociateDucrConfirmationMessages}
import play.api.mvc.Flash
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.tags.ViewTest

@ViewTest
class DisassociateDucrConfirmationViewSpec extends ViewSpec with DisassociateDucrConfirmationMessages with CommonMessages {

  private val page = injector.instanceOf[views.html.disassociate_ducr_confirmation]

  private def createView(ducr: String): Html = page()(appConfig, fakeRequest, new Flash(Map(FlashKeys.DUCR -> ducr)), messages)

  "Disassociate Ducr Confirmation View" should {

    "have a proper labels for messages" in {
      assertMessage(title, "Disassociation complete")
      assertMessage(heading, "The reference of the DUCR disassociated:")
      assertMessage(footNote, "You might want to take a screenshot of this for your records.")
    }

    "display page reference" in {
      getElementById(createView("GB123"), "reference").text() must be("GB123")
    }

    "display 'Back to start page' button on page" in {

      val view = createView("DUCR")

      val saveButton = getElementByCss(view, ".button")
      saveButton.text() must be(messages(continue))
    }
  }

}
