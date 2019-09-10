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

import controllers.routes
import play.api.i18n.MessagesApi
import utils.{Injector, Stubs}
import views.base.UnitViewSpec
import views.html.movement_confirmation_page

class MovementConfirmationViewSpec extends UnitViewSpec with Stubs with Injector {

  val movementConfirmationPage = new movement_confirmation_page(mainTemplate)
  val arrivalConfirmationView = movementConfirmationPage("EAL")(request, messages)
  val departureConfirmationView = movementConfirmationPage("EDL")(request, messages)

  "Movement Confirmation Page" should {

    "have correct messages" in {

      val messages = instanceOf[MessagesApi].preferred(request)

      messages("movement.EAL.confirmation") mustBe "Arrival confirmation"
      messages("movement.EDL.confirmation") mustBe "Departure confirmation"
      messages("movement.choice.EAL.label") mustBe "Arrival"
      messages("movement.choice.EDL.label") mustBe "Departure"
      messages("site.backToStartPage") mustBe "Back to start page"
    }
  }

  "Arrival Confirmation Page" should {

    "have title" in {

      arrivalConfirmationView.getElementsByTag("title").text() mustBe messages("movement.EAL.confirmation")
    }

    "have heading" in {

      arrivalConfirmationView.getElementById("highlight-box-heading").text() mustBe messages("movement.choice.EAL.label") + " has been submitted"
    }

    "have back button" in {

      val backButton = arrivalConfirmationView.getElementsByClass("button")

      backButton.text() mustBe messages("site.backToStartPage")
      backButton.attr("href") mustBe routes.StartController.displayStartPage().url
    }
  }

  "Departure Confirmation Page" should {

    "have title" in {

      departureConfirmationView.getElementsByTag("title").text() mustBe messages("movement.EDL.confirmation")
    }

    "have heading" in {

      departureConfirmationView.getElementById("highlight-box-heading").text() mustBe messages("movement.choice.EDL.label") + " has been submitted"
    }

    "have back button" in {

      val backButton = departureConfirmationView.getElementsByClass("button")

      backButton.text() mustBe messages("site.backToStartPage")
      backButton.attr("href") mustBe routes.StartController.displayStartPage().url
    }
  }
}
