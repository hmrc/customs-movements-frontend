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
import helpers.views.{CommonMessages, ShutMucrMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.spec.ViewSpec
import views.html.shut_mucr
import views.tags.ViewTest

@ViewTest
class ShutMucrViewSpec extends ViewSpec with ShutMucrMessages with CommonMessages {

  private val form: Form[ShutMucr] = ShutMucr.form()
  private val shutMucrPage = injector.instanceOf[shut_mucr]
  private def createView(form: Form[ShutMucr] = form): Html = shutMucrPage(form)

  "ShutMucr View" should {

    "have proper labels for messages" in {

      assertMessage(tabTitle, "Shut a MUCR")
      assertMessage(title, "Which MUCR do you want to shut?")
    }

    "have proper labels for error messages" in {

      assertMessage(mucrErrorEmpty, "MUCR number cannot be empty")
      assertMessage(mucrErrorFormat, "MUCR number is in incorrect format")
    }
  }

  "ShutMucr View on empty page" should {

    "display same page title as header" in {

      val view = createView()
      view.title() must include(view.getElementsByTag("h1").text())
    }

    "display 'Back' button that links to 'Choice' page" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must equal(messages(backCaption))
      backButton.attr("href") must be(controllers.routes.ChoiceController.displayChoiceForm().url)
    }

    "display input field label" in {

      getElementById(createView(), "mucr-label").text() must equal(messages(title))
    }

    "display empty input field" in {

      val textInput = getElementById(createView(), "mucr")

      textInput.text() must be(empty)
      textInput.attr("value") must be(empty)
    }

    "display 'Continue' button on page" in {

      getElementById(createView(), "submit").text() must equal(messages(saveAndContinueCaption))
    }
  }

  "ShutMucr view for invalid input" should {

    "display error" when {

      "provided with empty MUCR" in {

        val view = createView(ShutMucr.form().bind(Map("mucr" -> "")))

        getElementById(view, "error-message-mucr-input").text() must equal(messages(mucrErrorEmpty))
      }

      "provided with incorrect MUCR" in {

        val view = createView(ShutMucr.form().bind(Map("mucr" -> "!@#2@##@%#")))

        getElementById(view, "error-message-mucr-input").text() must equal(messages(mucrErrorFormat))
      }
    }
  }

  "ShutMucr View with entered data" should {

    "display data in mucr text input field" in {

      val correctMucr = "GB/44ZKKLA1VD-AWLUD26N35DA"
      val view = createView(ShutMucr.form().bind(Map("mucr" -> correctMucr)))

      getElementById(view, "mucr").attr("value") must equal(correctMucr)
    }
  }

}
