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
import helpers.views.{AssociateDucrConfirmationMessages, CommonMessages}
import play.api.mvc.Flash
import play.twirl.api.Html
import views.spec.ViewSpec
import views.tags.ViewTest

@ViewTest
class AssociateDucrConfirmationViewSpec extends ViewSpec with AssociateDucrConfirmationMessages with CommonMessages {

  private val page = injector.instanceOf[views.html.associate_ducr_confirmation]

  private def createView(mucr: String): Html =
    page()(fakeRequest, new Flash(Map(FlashKeys.MUCR -> mucr)), messages)

  "Associate Ducr Confirmation View" should {

    "have a proper labels for messages" in {
      assertMessage(title, "Consolidation complete")
      assertMessage(heading, "Your MUCR")
      assertMessage(footNote, "You might want to take a screenshot of this for your records.")
    }

    "display page reference" in {
      val document = createView("GB123")

      document.getElementById("highlight-box-reference") must containText("GB123")
    }

    "display 'Back to start page' button on page" in {
      val view = createView("DUCR")

      view.getElementsByClass("button").text() mustBe messages(continue)
    }
  }

}
