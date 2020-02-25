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
import forms.Choice
import forms.Choice._
import helpers.views.CommonMessages
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import views.html.choice_page
import views.spec.UnitViewSpec
import views.spec.UnitViewSpec.realMessagesApi
import views.tags.ViewTest

@ViewTest
class ChoiceViewSpec extends UnitViewSpec with CommonMessages with Injector {

  private val form: Form[Choice] = Choice.form()
  private val choicePage = instanceOf[choice_page]
  private def createView(form: Form[Choice] = form, messages: Messages = stubMessages()): Document =
    choicePage(form)(request, messages)

  "Choice View" should {

    "have proper labels for messages" in {

      val messages = messagesApi.preferred(request)

      messages must haveTranslationFor("movement.choice.title")
      messages must haveTranslationFor("movement.choice.arrival.label")
      messages must haveTranslationFor("movement.choice.departure.label")
      messages must haveTranslationFor("movement.choice.associateucr.label")
      messages must haveTranslationFor("movement.choice.disassociateucr.label")
      messages must haveTranslationFor("movement.choice.shutmucr.label")
      messages must haveTranslationFor("movement.choice.submissions.label")
    }

    "have proper labels for error messages" in {

      val messages = messagesApi.preferred(request)

      messages must haveTranslationFor("choicePage.input.error.empty")
      messages must haveTranslationFor("choicePage.input.error.incorrectValue")
    }
  }

  "Choice View on empty page" should {

    "display same page title as header" in {

      val viewWithMessage = createView(messages = realMessagesApi.preferred(request))
      viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
    }

    "display header with hint" in {

      createView().getElementsByClass("govuk-fieldset__heading").get(0).text() must be(messages("movement.choice.title"))
    }

    "display 'Back' button that links to 'Find a consignment' page" in {

      val backButton = createView().getElementById("back-link")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be(controllers.ileQuery.routes.FindConsignmentController.displayQueryForm().url)
    }

    "display 6 radio buttons with labels" in {

      val view = createView(Choice.form())

      view.getElementsByAttributeValue("for", "choice").text() must be(messages("movement.choice.arrival.label"))
      view.getElementsByAttributeValue("for", "choice-2").text() must be(messages("movement.choice.associateucr.label"))
      view.getElementsByAttributeValue("for", "choice-3").text() must be(messages("movement.choice.disassociateucr.label"))
      view.getElementsByAttributeValue("for", "choice-4").text() must be(messages("movement.choice.shutmucr.label"))
      view.getElementsByAttributeValue("for", "choice-5").text() must be(messages("movement.choice.departure.label"))
      view.getElementsByAttributeValue("for", "choice-6").text() must be(messages("movement.choice.submissions.label"))
    }

    "display 4 unchecked radio buttons" in {

      val view = createView(Choice.form())

      ensureRadioIsUnChecked(view, "choice")
      ensureRadioIsUnChecked(view, "choice-2")
      ensureRadioIsUnChecked(view, "choice-3")
      ensureRadioIsUnChecked(view, "choice-4")
    }

    "display 'Save and continue' button on page" in {

      val view = createView()

      val saveButton = view.getElementsByClass("govuk-button").get(0)
      saveButton.text() must be(messages(continueCaption))
    }
  }

  "Choice View for invalid input" should {

    "display error when no choice is made" in {

      val view = createView(Choice.form().bind(Map[String, String]()))

      view must haveGovUkGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#choice")

      view.getElementsByClass("govuk-list govuk-error-summary__list").get(0).text() must be(messages("choicePage.input.error.empty"))
    }

    "display error when choice is incorrect" in {

      val view = createView(Choice.form().bind(Map("choice" -> "incorrect")))

      view must haveGovUkGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#choice")

      view.getElementsByClass("govuk-list govuk-error-summary__list").get(0).text() must be(messages("choicePage.input.error.incorrectValue"))
    }
  }

  "Choice View when filled" should {

    "display selected 1st radio button - Arrival (EAL)" in {

      val view = createView(Choice.form().fill(Arrival))

      ensureRadioIsChecked(view, "choice")
      ensureRadioIsUnChecked(view, "choice-2")
      ensureRadioIsUnChecked(view, "choice-3")
      ensureRadioIsUnChecked(view, "choice-4")
    }

    "display selected 2nd radio button - Associate (EDL)" in {

      val view = createView(Choice.form().fill(AssociateUCR))

      ensureRadioIsUnChecked(view, "choice")
      ensureRadioIsChecked(view, "choice-2")
      ensureRadioIsUnChecked(view, "choice-3")
      ensureRadioIsUnChecked(view, "choice-4")
    }

    "display selected 3rd radio button - Disassociate (EAC)" in {

      val view = createView(Choice.form().fill(DisassociateUCR))

      ensureRadioIsUnChecked(view, "choice")
      ensureRadioIsUnChecked(view, "choice-2")
      ensureRadioIsChecked(view, "choice-3")
      ensureRadioIsUnChecked(view, "choice-4")
    }

    "display selected 4th radio button - Shut a MUCR (CST)" in {

      val view = createView(Choice.form().fill(ShutMUCR))

      ensureRadioIsUnChecked(view, "choice")
      ensureRadioIsUnChecked(view, "choice-2")
      ensureRadioIsUnChecked(view, "choice-3")
      ensureRadioIsChecked(view, "choice-4")
    }
  }

  private def ensureRadioIsChecked(view: Document, elementId: String): Unit = {
    val option = view.getElementById(elementId).getElementsByAttribute("checked")
    option.size() mustBe 1
  }

  private def ensureRadioIsUnChecked(view: Document, elementId: String): Unit = {
    val option = view.getElementById(elementId)
    option.attr("checked") mustBe empty
  }
}
