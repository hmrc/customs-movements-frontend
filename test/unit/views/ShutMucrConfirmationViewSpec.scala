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
import controllers.storage.FlashKeys
import helpers.views.CommonMessages
import play.api.mvc.Flash
import play.twirl.api.Html
import testdata.CommonTestData.correctUcr
import views.html.shut_mucr_confirmation
import views.spec.UnitViewSpec
import views.tags.ViewTest

@ViewTest
class ShutMucrConfirmationViewSpec extends UnitViewSpec with CommonMessages {

  private val shutMucrConformationPage = new shut_mucr_confirmation(mainTemplate)
  private val view: Html = shutMucrConformationPage()(request, Flash(Map(FlashKeys.MUCR -> correctUcr)), messages)

  "Shut Mucr Confirmation View" should {

    "have proper labels for messages" in {

      val messages = messagesApi.preferred(request)

      messages must haveTranslationFor("shutMucr.confirmation.tab.heading")
      messages must haveTranslationFor("shutMucr.confirmation.heading")
      messages must haveTranslationFor("shutMucr.confirmation.nextSteps")
      messages must haveTranslationFor("shutMucr.confirmation.nextSteps.shutMucr")
      messages must haveTranslationFor("shutMucr.confirmation.nextSteps.depart")
    }

    "display page reference" in {

      view.getElementById("highlight-box-heading").text() mustBe messages("shutMucr.confirmation.heading")
    }

    "have status information" in {

      view.getElementById("status-info").text() mustBe messages("movement.confirmation.statusInfo")
    }

    "have what next section" in {

      view.getElementById("what-next").text() mustBe messages("movement.confirmation.whatNext")
    }

    "have next steps section" in {

      view.getElementById("next-steps").text() mustBe messages("shutMucr.confirmation.nextSteps")
    }

    "display 'Back to start page' button on page" in {

      val backButton = view.getElementsByClass("govuk-button")

      backButton.text() mustBe messages("site.backToStart")
      backButton.first() must haveHref(routes.ChoiceController.displayChoiceForm())
    }
  }

}
