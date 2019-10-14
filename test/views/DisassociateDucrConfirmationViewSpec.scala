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
import helpers.views.CommonMessages
import play.api.mvc.Flash
import play.twirl.api.Html
import views.spec.UnitViewSpec
import views.tags.ViewTest

@ViewTest
class DisassociateDucrConfirmationViewSpec extends UnitViewSpec with CommonMessages {

  private val page = new views.html.disassociate_ducr_confirmation(mainTemplate)
  private val exampleDucr = "5GB123456789000-123ABC456DEFIIIII"
  private val view: Html = page()(request, new Flash(Map(FlashKeys.DUCR -> exampleDucr)), messages)

  "Disassociate Ducr Confirmation View" should {

    "have a proper labels for messages" in {

      val messages = messagesApi.preferred(request)

      messages must haveTranslationFor("disassociateDucr.confirmation.tab.heading")
      messages must haveTranslationFor("disassociateDucr.confirmation.title")
      messages must haveTranslationFor("consolidation.confirmation.addOrShut")
    }

    "display page reference" in {

      view.getElementById("highlight-box-heading").text() mustBe messages("disassociateDucr.confirmation.title")
    }

    "have status information" in {

      view.getElementById("status-info").text() mustBe messages("movement.confirmation.statusInfo")
    }

    "have what next section" in {

      view.getElementById("what-next").text() mustBe messages("movement.confirmation.whatNext")
    }

    "have next steps section" in {

      view.getElementById("next-steps").text() mustBe messages("consolidation.confirmation.addOrShut")
    }

    "display 'Back to start page' button on page" in {

      view.getElementsByClass("button").text() mustBe messages(backToStartPageCaption)
    }
  }

}
