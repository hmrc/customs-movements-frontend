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
import forms.Choice.Arrival
import forms.ConsignmentReferences
import helpers.views.{CommonMessages, ConsignmentReferencesMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.spec.ViewSpec

class ConsignmentReferencesViewSpec extends ViewSpec with ConsignmentReferencesMessages with CommonMessages {

  private val form: Form[ConsignmentReferences] = ConsignmentReferences.form()
  private val consignmentReferencesPage = injector.instanceOf[views.html.consignment_references]

  private val arrivalView: Html = consignmentReferencesPage(form)(fakeJourneyRequest(Arrival), messages)

  "Consignment References View" should {

    "have a proper labels for messages" in {

      assertMessage(arrivalHeading, "Arrive consignment")
      assertMessage(departureHeading, "Depart consignment")
      assertMessage(question, "What type of consignment reference do you want to enter?")
      assertMessage(referenceDucr, "DUCR")
      assertMessage(referenceMucr, "MUCR")
      assertMessage(referenceValue, "Enter reference")
    }

    "have a proper labels for error messages" in {

      assertMessage(referenceEmpty, "Please choose reference")
      assertMessage(referenceError, "Incorrect reference")
      assertMessage(referenceValueEmpty, "Please enter reference")
      assertMessage(referenceValueError, "Please enter a valid reference")
    }
  }

  "Consignment References View on empty page" should {

    "display page question" in {

      getElementById(arrivalView, "title").text() mustBe messages(question)
    }

    "display page heading" in {

      getElementById(arrivalView, "section-header").text() mustBe messages(arrivalHeading)
    }

    "display reference radio options" in {

      getElementById(arrivalView, "Ducr-label").text() mustBe messages(referenceDucr)
      getElementById(arrivalView, "Mucr-label").text() mustBe messages(referenceMucr)
    }

    "display reference value text input" in {

      getElementById(arrivalView, "referenceValue-label").text() mustBe messages(referenceValue)
    }

    "display \"Back\" buttion that links to start page" in {

      val backButton = getElementById(arrivalView, "link-back")

      backButton.text() mustBe messages(backCaption)
      backButton.attr("href") mustBe routes.ChoiceController.displayChoiceForm().url
    }

    "display \"Save and continue\" button on page" in {

      val saveButton = getElementById(arrivalView, "submit")
      saveButton.text() mustBe messages(saveAndContinueCaption)
    }
  }
}
