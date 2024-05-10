/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers.consolidations.routes.{AssociateUcrController, MucrOptionsController}
import forms.DucrPartChiefChoice.IsDucrPart
import forms.UcrType._
import forms.{AssociateUcr, DucrPartChiefChoice}
import models.UcrBlock
import models.cache.{AssociateUcrAnswers, Cache}
import models.requests.JourneyRequest
import org.scalatest.Assertion
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.twirl.api.Html
import testdata.CommonTestData.validEori
import views.ViewSpec
import views.html.summary.associate_ucr_summary
import views.tags.ViewTest

@ViewTest
class AssociateUcrSummaryViewSpec extends ViewSpec with Injector {

  private implicit val request: JourneyRequest[AnyContentAsEmpty.type] = journeyRequest(AssociateUcrAnswers())

  private val page = instanceOf[associate_ucr_summary]

  private def createView(mucr: String, ducr: String)(implicit request: JourneyRequest[_]): Html =
    page(AssociateUcr(Ducr, ducr), mucr)

  "Associate Ducr Confirmation View" should {
    val view = createView("MUCR", "DUCR")

    "render title" in {
      view.getTitle must containMessage("associate.ucr.summary.title")
    }

    "display page heading" in {
      view.getElementById("title") must containMessage("associate.ucr.summary.title")
    }

    "render a back button pointing to /mucr-options" when {
      "the entered ucr is of type 'DucrPart" in {
        val cache = Cache(validEori, None, None, false, Some(DucrPartChiefChoice(IsDucrPart)))
        implicit val request = journeyRequest(cache)
        testBackButton(MucrOptionsController.displayPage)
      }
    }

    "render a back button pointing to /associate-ucr" when {

      "the entered ucr is of type 'Mucr" in {
        implicit val request = journeyRequest(AssociateUcrAnswers(), Some(UcrBlock("ucr", Mucr)))
        testBackButton(AssociateUcrController.displayPage)
      }

      "the entered ucr is of type 'Ducr" in {
        implicit val request = journeyRequest(AssociateUcrAnswers(), Some(UcrBlock("ucr", Ducr)))
        testBackButton(AssociateUcrController.displayPage)
      }
    }

    "display 'Confirm and submit' button on page" in {
      view.getElementsByClass("govuk-button").first() must containMessage("site.confirmAndSubmit")
    }

    "display 'Change' link on page for associate ucr" in {
      val changeUcr = view.getElementsByClass("govuk-link").get(3)
      changeUcr must containMessage("site.change")
      changeUcr must haveHref(AssociateUcrController.displayPage)
    }

    "display 'Change' link on the page for mucr" in {
      val changeMucr = view.getElementsByClass("govuk-link").get(4)
      changeMucr must containMessage("site.change")
      changeMucr must haveHref(MucrOptionsController.displayPage)
    }

    "display 'Add consignment' type in summary list" in {
      view.getElementsByClass("govuk-summary-list__key").first() must containMessage("associate.ucr.summary.kind.ducr")
      view.getElementsByClass("govuk-summary-list__value").first().text() mustBe "DUCR"
    }

    "display 'To master consignment' type in summary list" in {
      view.getElementsByClass("govuk-summary-list__key").last() must containMessage("associate.ucr.summary.kind.mucr")
      view.getElementsByClass("govuk-summary-list__value").last().text() mustBe "MUCR"
    }
  }

  private def testBackButton(call: Call)(implicit request: JourneyRequest[_]): Assertion = {
    val view = createView("MUCR", "DUCR")
    val backButton = view.getBackButton
    backButton mustBe defined
    backButton.get must haveHref(call)
    backButton.get must containMessage("site.back")
  }
}
