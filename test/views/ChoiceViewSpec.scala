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

import forms.Choice
import helpers.views.{ChoiceMessages, CommonMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec
import views.html.choice_page
import views.tags.ViewTest

@ViewTest
class ChoiceViewSpec extends ViewSpec with ChoiceMessages with CommonMessages {

  private val form: Form[Choice] = Choice.form()
  private val choicePage = injector.instanceOf[choice_page]
  private def createView(form: Form[Choice] = form): Html = choicePage(form)

  "Choice View" should {

    "have proper labels for messages" in {
      assertMessage(title, "What do you want to do?")
      assertMessage(arrival, "Arrival")
      assertMessage(departure, "Departure")
      assertMessage(associate, "Associate")
      assertMessage(disassociate, "Disassociate")
    }

    "have proper labels for error messages" in {
      assertMessage(choiceEmpty, "Please, choose what do you want to do")
      assertMessage(choiceError, "Please, choose valid option")
    }
  }

  "Choice View on empty page" should {

    "display page title" in {
      getElementByCss(createView(), "title").text() must be("What do you want to do?")
    }

    "display header with hint" in {
      getElementByCss(createView(), "legend>h1").text() must be("What do you want to do?")
    }

    "display Arrival radio button with description (not selected)" in {
      val view = createView(Choice.form())
      getElementByCss(view, "#choice>div:nth-child(2)>label").text() must be("Arrival")
      verifyUnchecked(view, "arrival")
    }

    "display Departure radio button with description (not selected)" in {
      val view = createView(Choice.form())
      
      //#arrival-label
      getElementByCss(view, "#choice>div:nth-child(3)>label").text() must be("Departure")
      verifyUnchecked(view, "departure")
    }

    "display Associate radio button with description (not selected)" in {
      val view = createView(Choice.form())
      getElementByCss(view, "#choice>div:nth-child(4)>label").text() must be("Associate")
      verifyUnchecked(view, "departure")
    }

    "display 'Back' button that links to 'Make an export declaration' page" in {
      val backButton = getElementById(createView(), "link-back")
      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be(controllers.routes.StartController.displayStartPage().url)
    }

    "display 'Save and continue' button on page" in {
      val saveButton = getElementByCss(createView(), "#submit")
      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }

  "Choice View for invalid input" should {

    "display error when no choice is made" in {
      val view = createView(Choice.form().bind(Map[String, String]()))
      getElementByCss(view, "#error-message-choice-input").text() must be(messages(choiceEmpty))
    }

    "display error when choice is incorrect" in {
      val view = createView(Choice.form().bind(Map("choice" -> "incorrect")))
      getElementByCss(view, "#error-message-choice-input").text() must be(messages(choiceError))
    }
  }

  "Choice View when filled" should {
    "display selected 1st radio button - Arrival (EAL)" in {
      val view = createView(Choice.form().fill(Choice("EAL")))
      getElementByCss(view, "#choice>div:nth-child(2)>label").text() must be("Arrival")
      verifyChecked(view, "arrival")
    }

    "display selected 2nd radio button - Departure (EDL)" in {
      val view = createView(Choice.form().fill(Choice("EDL")))
      getElementByCss(view, "#choice>div:nth-child(3)>label").text() must be("Departure")
      verifyChecked(view, "departure")
    }
    
    "display selected 3rd radio button - Associate" in {
       val view = createView(Choice.form().fill(Choice("ASS")))
       getElementByCss(view, "#choice>div:nth-child(4)>label").text() must be("Associate")
       verifyChecked(view, "associate")
     }
    
    "display selected 4th radio button - Disassociate (EAC)" in {
      val view = createView(Choice.form().fill(Choice("EAC")))
      getElementByCss(view, "#choice>div:nth-child(5)>label").text() must be("Disassociate")
      verifyChecked(view, "disassociate")
    }
  }
}
