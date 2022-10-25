/*
 * Copyright 2022 HM Revenue & Customs
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

package views.disassociateucr

import base.Injector
import controllers.consolidations.routes.DisassociateUcrController
import controllers.routes.{ChoiceOnConsignmentController, DucrPartDetailsController}
import forms.DucrPartChiefChoice.IsDucrPart
import forms.{DisassociateUcr, DucrPartChiefChoice}
import forms.UcrType.Ducr
import models.cache.{Cache, DisassociateUcrAnswers}
import testdata.CommonTestData.validEori
import views.ViewSpec
import views.html.disassociateucr.disassociate_ucr_summary
import views.tags.ViewTest

@ViewTest
class DisassociateUcrSummaryViewSpec extends ViewSpec with Injector {

  private implicit val request = journeyRequest(DisassociateUcrAnswers())

  private val page = instanceOf[disassociate_ucr_summary]

  val disassociateUcr = DisassociateUcr(Ducr, ducr = Some("SOME-DUCR"), mucr = None)

  "Disassociate Ucr Summary View" should {

    "display page heading" in {
      page(disassociateUcr).getElementById("title") must containMessage("disassociate.ucr.summary.title")
    }

    "display 'Confirm and submit' button on page" in {
      val view = page(disassociateUcr)
      view.getElementsByClass("govuk-button").text() mustBe messages("site.confirmAndSubmit")
    }

    "display 'Reference' link on page" in {
      val view = page(disassociateUcr)
      view.getElementsByClass("govuk-summary-list__value").first() must containText("SOME-DUCR")
    }

    "display 'Change' link when on NON-'Find a consignment' journey" in {
      val view = page(disassociateUcr)
      val changeButton = view.getElementsByClass("govuk-link").get(1)
      changeButton must containMessage("site.change")
      changeButton must haveAttribute("href", controllers.consolidations.routes.DisassociateUcrController.displayPage.url)
    }

    "not display 'Change' link when on 'Find a consignment' journey" in {
      implicit val request = journeyRequest(DisassociateUcrAnswers(), None, true)
      val links = page(disassociateUcr).getElementsByClass("govuk-link")
      links.size() mustBe 2
    }

    "have a 'Back' button linking to the /choice-on-consignment page" when {
      "on 'Find a consignment' journey" in {
        implicit val request = journeyRequest(DisassociateUcrAnswers(), None, true)
        val backButton = page(disassociateUcr).getBackButton

        backButton mustBe defined
        backButton.get must haveHref(ChoiceOnConsignmentController.displayChoices)
      }
    }

    "have a 'Back' button linking to the /ducr-part-details page" when {
      "on NON-'Find a consignment' journey and" when {
        "Ucr is DucrPart" in {
          val cache = Cache(validEori, Some(DisassociateUcrAnswers()), None, false, Some(DucrPartChiefChoice(IsDucrPart)))
          implicit val request = journeyRequest(cache)
          val backButton = page(disassociateUcr).getBackButton

          backButton mustBe defined
          backButton.get must haveHref(DucrPartDetailsController.displayPage)
        }
      }
    }

    "have a 'Back' button linking to the /dissociate-ucr page" when {
      "on NON-'Find a consignment' journey and" when {
        "Ucr is DucrPart" in {
          val backButton = page(disassociateUcr).getBackButton

          backButton mustBe defined
          backButton.get must haveHref(DisassociateUcrController.displayPage)
        }
      }
    }
  }
}
