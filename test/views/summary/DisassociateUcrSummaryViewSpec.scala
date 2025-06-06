/*
 * Copyright 2024 HM Revenue & Customs
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

package views.summary

import base.Injector
import controllers.consolidations.routes.DisassociateUcrController
import forms.DucrPartChiefChoice.IsDucrPart
import forms.UcrType.Ducr
import forms.{DisassociateUcr, DucrPartChiefChoice}
import models.cache.{Cache, DisassociateUcrAnswers}
import models.requests.JourneyRequest
import play.api.mvc.AnyContentAsEmpty
import testdata.CommonTestData.validEori
import views.ViewSpec
import views.html.summary.disassociate_ucr_summary
import views.tags.ViewTest

@ViewTest
class DisassociateUcrSummaryViewSpec extends ViewSpec with Injector {

  private implicit val request: JourneyRequest[AnyContentAsEmpty.type] = journeyRequest(DisassociateUcrAnswers())

  private val page = instanceOf[disassociate_ucr_summary]

  val disassociateUcr: DisassociateUcr = DisassociateUcr(Ducr, ducr = Some("SOME-DUCR"), mucr = None)

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
      val changeButton = view.getElementsByClass("govuk-link").get(3)
      changeButton must containMessage("site.change")
      changeButton must haveAttribute("href", DisassociateUcrController.displayPage.url)
    }

    "not display 'Change' link when on 'Find a consignment' journey" in {
      implicit val request: JourneyRequest[AnyContentAsEmpty.type] = journeyRequest(DisassociateUcrAnswers(), None, ucrBlockFromIleQuery = true)
      val links = page(disassociateUcr).getElementsByClass("govuk-link")
      links.size() mustBe 4
    }

    "have a 'Back' button linking to the /choice-on-consignment page" when {
      "on 'Find a consignment' journey" in {
        implicit val request: JourneyRequest[AnyContentAsEmpty.type] = journeyRequest(DisassociateUcrAnswers(), None, ucrBlockFromIleQuery = true)
        val backButton = page(disassociateUcr).getBackButton

        backButton mustBe defined
        backButton.get must haveHref(backButtonDefaultCall)
      }
    }

    "have a 'Back' button linking to the /ducr-part-details page" when {
      "on NON-'Find a consignment' journey and" when {
        "Ucr is DucrPart" in {
          val cache = Cache(validEori, Some(DisassociateUcrAnswers()), None, ucrBlockFromIleQuery = false, Some(DucrPartChiefChoice(IsDucrPart)))
          implicit val request: JourneyRequest[AnyContentAsEmpty.type] = journeyRequest(cache)
          val backButton = page(disassociateUcr).getBackButton

          backButton mustBe defined
          backButton.get must haveHref(backButtonDefaultCall)
        }
      }
    }

    "have a 'Back' button linking to the /disassociate-ucr page" when {
      "on NON-'Find a consignment' journey and" when {
        "Ucr is DucrPart" in {
          val backButton = page(disassociateUcr).getBackButton

          backButton mustBe defined
          backButton.get must haveHref(backButtonDefaultCall)
        }
      }
    }
  }
}
