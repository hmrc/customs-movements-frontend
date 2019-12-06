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
import testdata.MovementsTestData
import views.html.arrival_details

class ArrivalDetailsViewSpec extends ViewSpec {

  private implicit val request = journeyRequest(ArrivalAnswers())

  private val movementDetails = MovementsTestData.movementDetails

  private val page = new arrival_details(main_template)

  "Arrival View" should {
    "render title" in {
      page(movementDetails.arrivalForm()).getTitle must containMessage("arrivalDetails.header")
    }

    "render heading input with hint for date" in {
      page(movementDetails.arrivalForm()).getElementById("dateOfArrival-label") must containMessage("arrivalDetails.date.question")
      page(movementDetails.arrivalForm()).getElementById("dateOfArrival-hint") must containMessage("arrivalDetails.date.hint")
    }

    "render heading input with hint for time" in {
      page(movementDetails.arrivalForm()).getElementById("timeOfArrival-label") must containMessage("arrivalDetails.time.question")
      page(movementDetails.arrivalForm()).getElementById("timeOfArrival-hint") must containMessage("arrivalDetails.time.hint")
    }

    "render back button" in {
      val backButton = page(movementDetails.arrivalForm()).getBackButton

      backButton mustBe defined
      backButton.get must haveHref(controllers.routes.ArrivalReferenceController.displayPage())
    }

    "render error summary" when {
      "no errors" in {
        page(movementDetails.arrivalForm()).getErrorSummary mustBe empty
      }

      "some errors" in {
        page(movementDetails.arrivalForm().withError("error", "error.required")).getErrorSummary mustBe defined
      }
    }
  }
}
