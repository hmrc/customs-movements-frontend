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

package views.associateucr

import base.Injector
import forms.{ManageMucrChoice, UcrType}
import models.cache.AssociateUcrAnswers
import org.jsoup.nodes.Element
import play.api.mvc.Call
import play.twirl.api.Html
import views.ViewSpec
import views.html.associateucr.associate_ucr_summary_no_change
import views.tags.ViewTest

@ViewTest
class AssociateUcrSummaryNoChangeViewSpec extends ViewSpec with Injector {

  private implicit val request = journeyRequest(AssociateUcrAnswers())

  private val page = instanceOf[associate_ucr_summary_no_change]

  private def createView(consignmentRef: String, associateWith: String, associateKind: UcrType, manageMucrChoice: Option[ManageMucrChoice]): Html =
    page(consignmentRef, associateWith, associateKind, manageMucrChoice)(request, messages)

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

      def validateBackButton(backButton: Option[Element], call: Call): Unit = {
        backButton mustBe defined
        backButton.foreach(button => {
          button must haveHref(call)
          button must containMessage("site.back")
        })
      }

      "query Ducr" in {

        val view = createView("DUCR", "MUCR", UcrType.Mucr, None)
        validateBackButton(view.getBackButton, controllers.consolidations.routes.MucrOptionsController.displayPage())
      }

      "query Mucr and Associate this consignment to another" in {

        val view = createView("MUCR", "MUCR", UcrType.Mucr, Some(ManageMucrChoice(ManageMucrChoice.AssociateThisMucr)))
        validateBackButton(view.getBackButton, controllers.consolidations.routes.MucrOptionsController.displayPage())
      }

      "query Mucr and Associate another consignment to this one" in {

        val view = createView("MUCR", "MUCR", UcrType.Mucr, Some(ManageMucrChoice(ManageMucrChoice.AssociateAnotherMucr)))
        validateBackButton(view.getBackButton, controllers.consolidations.routes.AssociateUcrController.displayPage())
      }
    }

    "render change link" when {

      def validateChangeLink(view: Html, call: Call): Unit = {
        val changeUcr = view.getElementsByClass("govuk-link").get(1)
        changeUcr must containMessage("site.change")
        changeUcr must haveHref(call)
      }

      "query Ducr" in {

        val view = createView("DUCR", "MUCR", UcrType.Mucr, None)
        validateChangeLink(view, controllers.consolidations.routes.MucrOptionsController.displayPage())
      }

      "query Mucr and Associate this consignment to another" in {

        val view = createView("MUCR", "MUCR", UcrType.Mucr, Some(ManageMucrChoice(ManageMucrChoice.AssociateThisMucr)))
        validateChangeLink(view, controllers.consolidations.routes.MucrOptionsController.displayPage())
      }

      "query Mucr and Associate another consignment to this one" in {

        val view = createView("MUCR", "MUCR", UcrType.Mucr, Some(ManageMucrChoice(ManageMucrChoice.AssociateAnotherMucr)))
        validateChangeLink(view, controllers.consolidations.routes.AssociateUcrController.displayPage())
      }
    }
  }
}
