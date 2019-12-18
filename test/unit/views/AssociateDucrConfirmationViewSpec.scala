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
import views.html.associate_ucr_confirmation
import views.spec.UnitViewSpec
import views.tags.ViewTest

@ViewTest
class AssociateDucrConfirmationViewSpec extends UnitViewSpec with CommonMessages {

  private val page = new associate_ucr_confirmation(mainTemplate)

  private val view: Html = page()(request, new Flash(Map(FlashKeys.CONSOLIDATION_KIND -> "ducr", FlashKeys.UCR -> correctUcr)), messages)

  "Associate Ducr Confirmation View" should {

    "have a proper labels for messages" in {

      val messages = messagesApi.preferred(request)

      messages must haveTranslationFor("associate.ducr.confirmation.tab.heading")
      messages must haveTranslationFor("associate.ducr.confirmation.heading")
      messages must haveTranslationFor("associate.mucr.confirmation.tab.heading")
      messages must haveTranslationFor("associate.mucr.confirmation.heading")
      messages must haveTranslationFor("association.confirmation.associateOrDepart")
      messages must haveTranslationFor("association.confirmation.associateOrDepart.associate")
      messages must haveTranslationFor("association.confirmation.associateOrDepart.depart")
    }

    "display page reference" in {

      view.getElementById("highlight-box-heading").text() mustBe messages("associate.ducr.confirmation.heading")
    }

    "have status information" in {

      view.getElementById("status-info").text() mustBe messages("movement.confirmation.statusInfo")
    }

    "have what next section" in {

      view.getElementById("what-next").text() mustBe messages("movement.confirmation.whatNext")
    }

    "have next steps section" in {

      view.getElementById("next-steps").text() mustBe messages("association.confirmation.associateOrDepart")
    }

    "display 'Back to start page' button on page" in {

      val backButton = view.getElementsByClass("govuk-button")

      backButton.text() mustBe messages("site.backToStart")
      backButton.first() must haveHref(routes.ChoiceController.displayChoiceForm())
    }

    "link to list of submissions" in {
      pending
      view.getElementsByAttributeValue("href", routes.ChoiceController.startSpecificJourney(forms.Choice.Submissions.value).url) mustNot be(empty)
    }

    "link to start another association" in {
      pending
      view.getElementsByAttributeValue("href", routes.ChoiceController.startSpecificJourney(forms.Choice.AssociateUCR.value).url).size() mustBe >(0)
    }

    "link to shut mucr" in {
      pending
      view.getElementsByAttributeValue("href", routes.ChoiceController.startSpecificJourney(forms.Choice.ShutMUCR.value).url).size() mustBe >(0)
    }
  }

}
