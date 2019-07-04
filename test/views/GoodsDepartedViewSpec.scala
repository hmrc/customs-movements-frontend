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

import forms.GoodsDeparted
import helpers.views.{CommonMessages, GoodsDepartedMessages}
import play.api.data.Form
import play.twirl.api.Html
import views.declaration.spec.ViewSpec

class GoodsDepartedViewSpec extends ViewSpec with GoodsDepartedMessages with CommonMessages {

  val form: Form[GoodsDeparted] = GoodsDeparted.form()
  val goodsDepartedPage = injector.instanceOf[views.html.goods_departed]

  private def createView(form: Form[GoodsDeparted] = form): Html = goodsDepartedPage(form)

  "Goods Departed View" should {

    "have a proper labels for messages" in {

      assertMessage(goodsDepartedTitle, "Goods departed")
      assertMessage(goodsDepartedQuestion, "Where are your goods being departed to?")
      assertMessage(goodsDepartedHint, "Goods can be departed out of the UK or back into the UK")
      assertMessage(goodsDepartedOutOfTheUk, "Out of the UK")
      assertMessage(goodsDepartedBackIntoTheUk, "Back into the UK")
    }

    "have a proper labels for errors" in {

      assertMessage(goodsDepartedEmpty, "Please answer on the question")
      assertMessage(goodsDepartedError, "Your choice is incorrect")
    }
  }

  "Goods Departed View on empty page" should {

    "display page title" in {

      getElementByCss(createView(), "head>title").text() must be(messages(goodsDepartedTitle))
    }

    "display page header" in {

      getElementById(createView(), "title").text() must be(messages(goodsDepartedQuestion))
    }

    "display radio option hint with all options" in {

      getElementById(createView(), "departedPlace-hint").text() must be(messages(goodsDepartedHint))
      getElementById(createView(), "OutOfTheUk-label").text() must be(messages(goodsDepartedOutOfTheUk))
      getElementById(createView(), "BackIntoTheUk-label").text() must be(messages(goodsDepartedBackIntoTheUk))
    }

    "display \"Back\" button that links to Location" in {

      val backButton = getElementById(createView(), "link-back")

      backButton.text() must be(messages(backCaption))
      backButton.attr("href") must be("/customs-movements/location")
    }

    "display \"Save and continue\" button on page" in {

      val saveButton = getElementById(createView(), "submit")

      saveButton.text() must be(messages(saveAndContinueCaption))
    }
  }
}
