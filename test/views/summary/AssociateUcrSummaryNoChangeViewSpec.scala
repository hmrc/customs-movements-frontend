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
import controllers.consolidations.routes.{AssociateUcrController, MucrOptionsController}
import forms.{ManageMucrChoice, UcrType}
import models.cache.AssociateUcrAnswers
import models.requests.JourneyRequest
import org.jsoup.nodes.Element
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.twirl.api.Html
import views.ViewSpec
import views.html.summary.associate_ucr_summary_no_change
import views.tags.ViewTest

@ViewTest
class AssociateUcrSummaryNoChangeViewSpec extends ViewSpec with Injector {

  private implicit val request: JourneyRequest[AnyContentAsEmpty.type] = journeyRequest(AssociateUcrAnswers())

  private val page = instanceOf[associate_ucr_summary_no_change]

  private def createView(consignmentRef: String, associateWith: String, associateKind: UcrType, manageMucrChoice: Option[ManageMucrChoice]): Html =
    page(consignmentRef, associateWith, associateKind, manageMucrChoice)

  "AssociateUcrSummaryNoChange View" should {

    "render title" in {
      val view = createView("DUCR", "MUCR", UcrType.Mucr, None)
      view.getTitle must containMessage("associate.ucr.summary.title")
    }

    "display 'Confirm and submit' button on page" in {
      val view = createView("DUCR", "MUCR", UcrType.Mucr, None)
      view.getElementsByClass("govuk-button").first() must containMessage("site.confirmAndSubmit")
    }

    "render back button" when {
      def validateBackButton(backButton: Option[Element]): Unit = {
        backButton mustBe defined
        backButton.foreach { button =>
          button must haveHref(backButtonDefaultCall)
          button must containMessage("site.back")
        }
      }

      "query Ducr" in {
        val view = createView("DUCR", "MUCR", UcrType.Mucr, None)
        validateBackButton(view.getBackButton)
      }

      "query Mucr and Associate this consignment to another" in {
        val mucrChoice = Some(ManageMucrChoice(ManageMucrChoice.AssociateThisMucr))
        val view = createView("MUCR", "MUCR", UcrType.Mucr, mucrChoice)
        validateBackButton(view.getBackButton)
      }

      "query Mucr and Associate another consignment to this one" in {
        val mucrChoice = Some(ManageMucrChoice(ManageMucrChoice.AssociateAnotherMucr))
        val view = createView("MUCR", "MUCR", UcrType.Mucr, mucrChoice)
        validateBackButton(view.getBackButton)
      }
    }

    "render change link" when {
      def validateChangeLink(view: Html, call: Call): Unit = {
        val changeUcr = view.getElementsByClass("govuk-link").get(3)
        changeUcr must containMessage("site.change")
        changeUcr must haveHref(call)
      }

      "query Ducr" in {
        val view = createView("DUCR", "MUCR", UcrType.Mucr, None)
        validateChangeLink(view, MucrOptionsController.displayPage)
      }

      "query Mucr and Associate this consignment to another" in {
        val mucrChoice = Some(ManageMucrChoice(ManageMucrChoice.AssociateThisMucr))
        val view = createView("MUCR", "MUCR", UcrType.Mucr, mucrChoice)
        validateChangeLink(view, MucrOptionsController.displayPage)
      }

      "query Mucr and Associate another consignment to this one" in {
        val mucrChoice = Some(ManageMucrChoice(ManageMucrChoice.AssociateAnotherMucr))
        val view = createView("MUCR", "MUCR", UcrType.Mucr, mucrChoice)
        validateChangeLink(view, AssociateUcrController.displayPage)
      }
    }
  }
}
