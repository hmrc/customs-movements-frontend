/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers.routes.RootController
import models.cache.AssociateUcrAnswers
import models.requests.JourneyRequest
import play.api.mvc.AnyContentAsEmpty
import play.twirl.api.Html
import views.html.user_signed_out

class UserSignedOutViewSpec extends ViewSpec with Injector {

  private implicit val request: JourneyRequest[AnyContentAsEmpty.type] = journeyRequest(AssociateUcrAnswers())
  private val page = instanceOf[user_signed_out]
  private def createView(): Html = page()

  "UserSignedOut View" should {

    val view = createView()

    "display page header" in {
      view.getElementsByTag("h1").first() must containMessage("userSignedOut.title")
    }

    "display information paragraph" in {
      view.getElementsByClass("govuk-body").first() must containMessage("userSignedOut.information")
    }

    "display link to Choice page" in {
      val link = view.getElementsByClass("govuk-link govuk-link--no-visited-state").first()

      link must containMessage("userSignedOut.startPageLink", request.host)
      link must haveHref(RootController.displayPage)
    }

    "display Exit Survey link" in {
      val exitSurvey = view.getElementById("exit-survey")

      exitSurvey must containMessage("exitSurvey.header")
    }

    "display link to gov.uk" in {
      val link = view.getElementsByClass("govuk-link govuk-link--no-visited-state").get(2)

      link must containMessage("site.link.backToGovUk")
      link must haveHref("https://www.gov.uk/")
    }
  }
}
