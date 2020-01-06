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
import forms.DisassociateKind._
import forms.DisassociateUcr
import models.cache.DisassociateUcrAnswers
import models.requests.JourneyRequest
import play.api.mvc.AnyContentAsEmpty
import views.html.disassociate_ucr_summary
import views.tags.ViewTest

@ViewTest
class DisassociateUcrSummaryViewSpec extends ViewSpec with Injector {

  private val page = instanceOf[disassociate_ucr_summary]
  private implicit val request: JourneyRequest[AnyContentAsEmpty.type] = journeyRequest(DisassociateUcrAnswers())

  "Disassociate Ucr Summary View" should {

    val view = page(DisassociateUcr(Ducr, ducr = Some("SOME-DUCR"), mucr = None))(request, messages)

    "display 'Confirm and submit' button on page" in {
      view.getElementsByClass("govuk-button").text() mustBe messages("site.confirmAndSubmit")
    }

    "display 'Change' link on page" in {
      val changeButton = view.getElementsByClass("govuk-link").first()
      changeButton must containMessage("site.change")
      changeButton must haveAttribute("href", controllers.consolidations.routes.DisassociateUcrController.displayPage().url)
    }

    "display 'Reference' link on page" in {
      view.getElementsByClass("govuk-summary-list__value").first() must containText("SOME-DUCR")
    }

  }

}
