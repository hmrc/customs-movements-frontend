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
import controllers.consolidations.routes
import forms.ShutMucr
import helpers.views.CommonMessages
import models.cache.ShutMucrAnswers
import models.requests.JourneyRequest
import play.api.mvc.AnyContentAsEmpty
import testdata.ConsolidationTestData.validMucr
import views.html.shut_mucr_summary

class ShutMucrSummaryViewSpec extends ViewSpec with CommonMessages with Injector {

  private val shutMucrSummaryPage = instanceOf[shut_mucr_summary]
  private implicit val request: JourneyRequest[AnyContentAsEmpty.type] = journeyRequest(ShutMucrAnswers())

  "Shut Mucr Summary View" should {

    val view = shutMucrSummaryPage(ShutMucr(validMucr))(request, messages)

    "display page header" in {

      view.getElementsByClass("govuk-heading-m").text() mustBe messages("shutMucr.summary.header")
    }

    "display MUCR type in summary list" in {

      view.getElementsByClass("govuk-summary-list__key").text() mustBe messages("shutMucr.summary.type")
    }

    "display correct mucr" in {

      view.getElementsByClass("govuk-summary-list__value").text() mustBe validMucr
    }

    "display correct change button" in {

      val changeButton = view.getElementsByClass("govuk-link").first()

      changeButton must haveHref(routes.ShutMucrController.displayPage())
      changeButton.text() must include(messages("site.edit"))
    }

    "display correct submit button" in {

      val submitButton = view.getElementsByClass("govuk-button").first()

      submitButton.text() mustBe messages("site.confirmAndSubmit")
    }
  }
}

//@ViewTest
//class DisassociateUcrSummaryViewSpec extends ViewSpec with CommonMessages with Injector {
//
//  private val page = instanceOf[disassociate_ucr_summary]
//  private implicit val request: JourneyRequest[AnyContentAsEmpty.type] = journeyRequest(DisassociateUcrAnswers())
//
//
//  "Disassociate Ucr Summary View" should {
//
//    val view = page(DisassociateUcr(Ducr, ducr = Some("SOME-DUCR"), mucr = None))(request, messages)
//
//    "display 'Confirm and submit' button on page" in {
//      view.getElementsByClass("govuk-button").text() must be(messages(confirmAndSubmitCaption))
//    }
//
//    "display 'Change' link on page" in {
//      val changeButton = view.getElementsByClass("govuk-link").first()
//      changeButton must containMessage(changeCaption)
//      changeButton must haveAttribute(
//        "href",
//        controllers.consolidations.routes.DisassociateUcrController.displayPage().url
//      )
//    }
//
//    "display 'Reference' link on page" in {
//      view.getElementsByClass("govuk-summary-list__value").first() must containText("SOME-DUCR")
//    }
//
//  }
//
//}
