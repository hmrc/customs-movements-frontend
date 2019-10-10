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

import forms.ConsignmentReferences
import helpers.views.{CommonMessages, ConsignmentReferencesMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.spec.ViewSpec

class ConsignmentReferencesViewSpec extends ViewSpec with ConsignmentReferencesMessages with CommonMessages {

  private val form: Form[ConsignmentReferences] = ConsignmentReferences.form()
  private val consignmentReferencesPage = injector.instanceOf[views.html.consignment_references]

  private def createView(form: Form[ConsignmentReferences] = form): Html = consignmentReferencesPage(form)

  "Consignment References View" should {

    "have a proper labels for messages" in {

      assertMessage(title, "Consignment references")
      assertMessage(eoriQuestion, "What is your EORI number?")
      assertMessage(eoriHint, "The number starts with a country code, for example, FR for France, and is then followed by up to 15 digits")
      assertMessage(referenceQuestion, "Which reference are you entering to arrive the goods?")
      assertMessage(referenceHint, "This can be a DUCR or MUCR")
      assertMessage(referenceDucr, "DUCR")
      assertMessage(referenceMucr, "MUCR")
      assertMessage(referenceValue, "Enter reference")
    }

    "have a proper labels for error messages" in {

      assertMessage(eoriError, "EORI number is incorrect")
      assertMessage(referenceEmpty, "Please choose reference")
      assertMessage(referenceError, "Incorrect reference")
      assertMessage(referenceValueEmpty, "Please enter reference")
      assertMessage(referenceValueError, "Please enter a valid reference")
    }
  }

  "Consignment References View on empty page" should {

    "display page title" in {

      getElementById(createView(), "title").text() must be(messages(title))
    }

    "display reference question" in {

      getElementById(createView(), "reference-label").text() must be(messages(referenceQuestion))
    }

    "display reference hint" in {

      getElementById(createView(), "reference-hint").text() must be(messages(referenceHint))
    }

    "display reference radio options" in {

      getElementById(createView(), "Ducr-label").text() must be(messages(referenceDucr))
      getElementById(createView(), "Mucr-label").text() must be(messages(referenceMucr))
    }

    "display reference value text input" in {

      getElementById(createView(), "referenceValue-label").text() must be(messages(referenceValue))
    }

    "display \"Back\" buttion that links to start page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-movements/choice")
    }

    "display \"Save and continue\" button on page" in {

      val view = createView()

      val saveButton = getElementById(view, "submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }
}
