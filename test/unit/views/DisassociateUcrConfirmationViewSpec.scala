/*
 * Copyright 2020 HM Revenue & Customs
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

import base.Injector
import controllers.routes
import controllers.storage.FlashKeys
import models.cache.DisassociateUcrAnswers
import play.api.mvc.Flash
import play.twirl.api.Html
import testdata.CommonTestData.correctUcr
import views.html.disassociate_ucr_confirmation
import views.tags.ViewTest

@ViewTest
class DisassociateUcrConfirmationViewSpec extends ViewSpec with Injector {

  private implicit val request = journeyRequest(DisassociateUcrAnswers())
  private val page = instanceOf[disassociate_ucr_confirmation]
  private val view: Html =
    page()(request, new Flash(Map(FlashKeys.UCR -> correctUcr, FlashKeys.CONSOLIDATION_KIND -> "DUCR")), messages)

  "Disassociate Ucr Confirmation View" should {

    "have title" in {

      view.getTitle must containMessage("disassociate.ucr.confirmation.DUCR.title")
    }

    "have heading" in {

      view.getElementsByClass("govuk-panel__title").first() must containMessage("disassociate.ucr.confirmation.heading", "DUCR", correctUcr)
    }

    "have status information" in {

      view.getElementById("status-info").getElementsByClass("govuk-link").first() must haveHref(
        routes.ChoiceController.startSpecificJourney(forms.Choice.Submissions.value)
      )
    }

    "have what next section" in {

      view.getElementById("what-next").text() mustBe messages("movement.confirmation.whatNext")
    }

    "have next steps section" in {

      val nextSteps = view.getElementById("next-steps").getElementsByClass("govuk-link")
      nextSteps.first() must haveHref(routes.ChoiceController.startSpecificJourney(forms.Choice.AssociateUCR.value))
      nextSteps.last() must haveHref(routes.ChoiceController.startSpecificJourney(forms.Choice.ShutMUCR.value))
    }

    "display 'Back to start page' button on page" in {

      val backButton = view.getElementsByClass("govuk-button")

      backButton.text() mustBe messages("site.backToStart")
      backButton.first() must haveHref(routes.ChoiceController.displayChoiceForm())
    }
  }

}
