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

import base.Injector
import forms.Choice
import forms.Choice._
import helpers.views.{ChoiceMessages, CommonMessages}
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import views.html.choice_page
import views.spec.UnitViewSpec
import views.spec.UnitViewSpec.realMessagesApi
import views.tags.ViewTest

@ViewTest
class ChoiceViewSpec extends UnitViewSpec with ChoiceMessages with CommonMessages with Injector {

  private val form: Form[Choice] = Choice.form()
  private val choicePage = instanceOf[choice_page]
  private def createView(form: Form[Choice] = form, messages: Messages = stubMessages()): Document =
    choicePage(form)(request, messages)

  "Choice View" should {

    "have proper labels for messages" in {

      val messages = messagesApi.preferred(request)

      messages must haveTranslationFor(title)
      messages must haveTranslationFor(arrivalDecLabel)
      messages must haveTranslationFor(departureDecLabel)
      messages must haveTranslationFor(shutMucrLabel)
      messages must haveTranslationFor(associateDecLabel)
      messages must haveTranslationFor(disassociateDecLabel)
    }

    "have proper labels for error messages" in {

      val messages = messagesApi.preferred(request)

      messages must haveTranslationFor(choiceEmpty)
      messages must haveTranslationFor(choiceError)
    }
  }

  "Choice View on empty page" should {

    "display same page title as header" in {

      val viewWithMessage = createView(messages = realMessagesApi.preferred(request))
      viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
    }

    "display header with hint" in {

      createView().getElementById("title").text() must be(messages(title))
    }

    "display 'Back' button that links to 'Make an export declaration' page" in {

      val backButton = createView().getElementById("link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be(controllers.routes.StartController.displayStartPage().url)
    }

    "display 4 radio buttons with labels" in {

      val view = createView(Choice.form())

      view.select("div.multiple-choice:nth-child(2)").text() must be(messages(arrivalDecLabel))
      view.select("div.multiple-choice:nth-child(3)").text() must be(messages(associateDecLabel))
      view.select("div.multiple-choice:nth-child(4)").text() must be(messages(disassociateDecLabel))
      view.select("div.multiple-choice:nth-child(5)").text() must be(messages(shutMucrLabel))
      view.select("div.multiple-choice:nth-child(6)").text() must be(messages(departureDecLabel))
      view.select("div.multiple-choice:nth-child(7)").text() must be(messages(viewSubmissionsLabel))
    }

    "display 4 unchecked radio buttons" in {

      val view = createView(Choice.form())

      ensureRadioIsUnChecked(view, "arrival")
      ensureRadioIsUnChecked(view, "departure")
      ensureRadioIsUnChecked(view, "disassociate")
      ensureRadioIsUnChecked(view, "shut_mucr")
    }

    "display 'Save and continue' button on page" in {

      val view = createView()

      val saveButton = view.getElementById("submit")
      saveButton.text() must be(messages(continueCaption))
    }
  }

  "Choice View for invalid input" should {

    "display error when no choice is made" in {

      val view = createView(Choice.form().bind(Map[String, String]()))

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("choice", "#choice")

      view.getElementById("error-message-choice-input").text() must be(messages(choiceEmpty))
    }

    "display error when choice is incorrect" in {

      val view = createView(Choice.form().bind(Map("choice" -> "incorrect")))

      view must haveGlobalErrorSummary
      view must haveFieldErrorLink("choice", "#choice")

      view.getElementById("error-message-choice-input").text() must be(messages(choiceError))
    }
  }

  "Choice View when filled" should {

    "display selected 1st radio button - Arrival (EAL)" in {

      val view = createView(Choice.form().fill(Arrival))

      ensureRadioIsChecked(view, "arrival")
      ensureRadioIsUnChecked(view, "departure")
      ensureRadioIsUnChecked(view, "disassociate")
      ensureRadioIsUnChecked(view, "shut_mucr")
    }

    "display selected 2nd radio button - Departure (EDL)" in {

      val view = createView(Choice.form().fill(Departure))

      ensureRadioIsUnChecked(view, "arrival")
      ensureRadioIsChecked(view, "departure")
      ensureRadioIsUnChecked(view, "disassociate")
      ensureRadioIsUnChecked(view, "shut_mucr")
    }

    "display selected 3rd radio button - Disassociate (EAC)" in {

      val view = createView(Choice.form().fill(DisassociateDUCR))

      ensureRadioIsUnChecked(view, "arrival")
      ensureRadioIsUnChecked(view, "departure")
      ensureRadioIsChecked(view, "disassociate")
      ensureRadioIsUnChecked(view, "shut_mucr")
    }

    "display selected 4th radio button - Shut a MUCR (CST)" in {

      val view = createView(Choice.form().fill(ShutMUCR))

      ensureRadioIsUnChecked(view, "arrival")
      ensureRadioIsUnChecked(view, "departure")
      ensureRadioIsUnChecked(view, "disassociate")
      ensureRadioIsChecked(view, "shut_mucr")
    }
  }

  private def ensureRadioIsChecked(view: Document, elementId: String): Unit = {
    val option = view.getElementById(elementId)
    option.attr("checked") mustBe "checked"
  }

  private def ensureRadioIsUnChecked(view: Document, elementId: String): Unit = {
    val option = view.getElementById(elementId)
    option.attr("checked") mustBe empty
  }
}
