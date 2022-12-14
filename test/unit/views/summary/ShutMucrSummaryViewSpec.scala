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

package views.summary

import base.Injector
import controllers.consolidations.routes.ShutMucrController
import controllers.routes.ChoiceOnConsignmentController
import forms.ShutMucr
import models.cache.ShutMucrAnswers
import testdata.CommonTestData.validMucr
import views.ViewSpec
import views.html.summary.shut_mucr_summary

class ShutMucrSummaryViewSpec extends ViewSpec with Injector {

  private implicit val request = journeyRequest(ShutMucrAnswers())

  private val shutMucrSummaryPage = instanceOf[shut_mucr_summary]

  val shutMucr = ShutMucr(validMucr)

  "Shut Mucr Summary View" should {

    "display page heading" in {
      shutMucrSummaryPage(shutMucr).getElementById("title") must containMessage("shutMucr.summary.title")
    }

    "display page header" in {
      shutMucrSummaryPage(shutMucr).getElementsByClass("govuk-heading-m").text() mustBe messages("shutMucr.summary.header")
    }

    "display MUCR type in summary list" in {
      shutMucrSummaryPage(shutMucr).getElementsByClass("govuk-summary-list__key").text() mustBe messages("shutMucr.summary.type")
    }

    "display correct mucr" in {
      shutMucrSummaryPage(shutMucr).getElementsByClass("govuk-summary-list__value").text() mustBe validMucr
    }

    "display correct submit button" in {
      val submitButton = shutMucrSummaryPage(shutMucr).getElementsByClass("govuk-button").first()

      submitButton.text() mustBe messages("site.confirmAndSubmit")
    }

    "not display change button when on a 'Find a consignment' journey" in {
      implicit val request = journeyRequest(ShutMucrAnswers(), None, true)
      shutMucrSummaryPage(shutMucr).getElementsByClass("govuk-link").size() mustBe 3
    }

    "display correct change button when on a NON-'Find a consignment' journey" in {
      val changeButton = shutMucrSummaryPage(shutMucr).getElementsByClass("govuk-link").get(2)

      changeButton must haveHref(ShutMucrController.displayPage)
      changeButton.text() must include(messages("site.edit"))
    }

    "have 'Back' button when on a 'Find a consignment' journey" in {
      implicit val request = journeyRequest(ShutMucrAnswers(), None, true)
      val backButton = shutMucrSummaryPage(shutMucr).getBackButton

      backButton mustBe defined
      backButton.get must haveHref(ChoiceOnConsignmentController.displayChoices)
    }

    "have 'Back' button when on a NON-'Find a consignment' journey" in {
      val backButton = shutMucrSummaryPage(shutMucr).getBackButton

      backButton mustBe defined
      backButton.get must haveHref(ShutMucrController.displayPage)
    }
  }
}
