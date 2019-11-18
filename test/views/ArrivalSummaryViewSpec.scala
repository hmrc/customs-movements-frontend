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

import models.cache.ArrivalAnswers
import models.requests.JourneyRequest
import play.api.mvc.AnyContentAsEmpty
import views.html.summary.arrival_summary_page

class ArrivalSummaryViewSpec extends ViewSpec {

  private implicit val request: JourneyRequest[AnyContentAsEmpty.type] = journeyRequest(ArrivalAnswers())

  private val page = new arrival_summary_page(main_template)

  private val answers = ArrivalAnswers()

  "View" should {
    "render title" in {
      page(answers).getTitle must containMessage("summary.arrival.title")
    }

    "render heading" in {
      page(answers).getElementById("title") must containMessage("summary.arrival.title")
    }

    "render back button" in {
      val backButton = page(answers).getBackButton

      backButton mustBe defined
      backButton.get must haveHref(controllers.routes.LocationController.displayPage())
    }
  }

}