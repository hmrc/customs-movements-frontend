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

import forms.ShutMucr
import models.cache.ShutMucrAnswers
import views.html.shut_mucr

class ShutMucrViewSpec extends ViewSpec {

  private implicit val request = journeyRequest(ShutMucrAnswers())

  private val page = new shut_mucr(main_template)

  "View" should {
    "render title" in {
      page(ShutMucr.form()).getTitle must containMessage("shutMucr.title")
    }

    "render input for mucr" in {
      page(ShutMucr.form()).getElementById("mucr-label") must containMessage("shutMucr.title")
    }

    "render back button" in {
      val backButton = page(ShutMucr.form()).getBackButton

      backButton mustBe defined
      backButton.get must haveHref(controllers.routes.ChoiceController.displayChoiceForm())
    }

    "render error summary" when {
      "no errors" in {
        page(ShutMucr.form()).getErrorSummary mustBe empty
      }

      "some errors" in {
        page(ShutMucr.form().withError("error", "error.required")).getErrorSummary mustBe defined
      }
    }
  }

}
