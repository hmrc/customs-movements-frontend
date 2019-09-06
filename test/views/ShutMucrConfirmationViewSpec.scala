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

import controllers.storage.FlashKeys
import helpers.views.{CommonMessages, ShutMucrConfirmationMessages}
import play.api.mvc.Flash
import play.twirl.api.Html
import views.base.ViewSpec
import views.html.shut_mucr_confirmation
import views.tags.ViewTest

@ViewTest
class ShutMucrConfirmationViewSpec extends ViewSpec with ShutMucrConfirmationMessages with CommonMessages {

  private val shutMucrConformationPage = injector.instanceOf[shut_mucr_confirmation]
  private def createView(mucrOpt: Option[String] = None): Html =
    shutMucrConformationPage()(
      fakeRequest,
      Flash(mucrOpt.map(mucr => Map(FlashKeys.MUCR -> mucr)).getOrElse(Map.empty)),
      messages
    )

  "Shut Mucr Confirmation View" should {

    "have proper labels for messages" in {

      assertMessage(title, "MUCR shut")
      assertMessage(confirmationInfo, "The reference of the MUCR shut:")
      assertMessage(additionalNote, "You might want to take a screenshot of this for your records.")
    }

    "display tab title" in {

      getElementByCss(createView(), "head>title").text() must equal(messages(title))
    }

    "display page heading inside highlight box" in {

      val view = createView()

      getElementById(view, "highlight-box-heading").text() must equal(messages(title))
      getElementByCss(view, ".govuk-box-highlight").text() must include(messages(title))
    }

    "display confirmation information with MUCR inside highlight box" in {

      val view = createView()

      getElementById(view, "highlight-box-info").text() must equal(messages(confirmationInfo))
      getElementByCss(view, ".govuk-box-highlight").text() must include(messages(confirmationInfo))
    }

    "display reference MUCR inside highlight box" in {

      val mucr = "MUCR1234567890"
      val view = createView(Some(mucr))

      getElementById(view, "highlight-box-reference").text() must equal(mucr)
      getElementByCss(view, ".govuk-box-highlight").text() must include(mucr)
    }

    "display '-' as reference MUCR when no value provided in flash" in {

      getElementById(createView(), "highlight-box-reference").text() must equal("-")
    }

    "display additional note" in {

      getElementById(createView(), "additional-note").text() must equal(messages(additionalNote))
    }

    "display 'Back to start page' button that links to Start Page" in {

      val button = getElementByCss(createView(), ".button")
      button.text() must equal(messages(backToStartPageCaption))
      button.attr("href") must equal("/customs-movements/start")
    }
  }

}
