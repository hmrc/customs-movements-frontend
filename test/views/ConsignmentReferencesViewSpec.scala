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
      assertMessage(arrivalQuestion, "What consignment do you want to arrive?")
      assertMessage(departureQuestion, "What consignment do you want to depart?")
      assertMessage(referenceDucr, "Declaration Consignment Reference (DUCR)")
      assertMessage(referenceMucr, "Master Consignment Reference (MUCR)")
      assertMessage(referenceDucrValue, "Declaration Unique Consignment Reference")
      assertMessage(referenceMucrValue, "Master Unique Consignment Reference")
    }

    "have a proper labels for error messages" in {

      assertMessage(referenceDucrEmpty, "Enter a Declaration Unique Consignment Reference")
      assertMessage(referenceMucrEmpty, "Enter a Master Unique Consignment Reference")
      assertMessage(referenceDucrError, "Declaration Unique Consignment Reference is incorrect")
      assertMessage(referenceMucrError, "Master Unique Consignment Reference is incorrect")
      assertMessage(referenceEmpty, "Please choose reference")
      assertMessage(referenceError, "Incorrect reference")
    }
  }

  "Consignment References View on empty page" should {

    "display page question" in {

      getElementById(arrivalView, "title").text() mustBe messages(arrivalQuestion)
    }

    "display page heading" in {

      getElementById(arrivalView, "section-header").text() mustBe messages(arrivalHeading)
    }

    "display reference radio options" in {

      getElementById(arrivalView, "Ducr-label").text() mustBe messages(referenceDucr)
      getElementById(arrivalView, "Mucr-label").text() mustBe messages(referenceMucr)
    }

    "display reference value text input" in {

      getElementById(arrivalView, "mucrValue-label").text() mustBe messages(referenceMucrValue)
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
