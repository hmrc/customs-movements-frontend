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

import play.twirl.api.Html
import views.html.unauthorised
import views.spec.UnitViewSpec

class UnauthorisedViewSpec extends UnitViewSpec {

  val unauthorisedPage = new unauthorised(mainTemplate)
  val unauthorisedView: Html = unauthorisedPage()(request, messages)

  "Unauthorised mnessages" should {

    "have correct content" in {

      val messages = messagesApi.preferred(request)

      messages("unauthorised.title") mustBe "You can’t access this service with this account"
    }
  }

  "Unauthorised page" should {

    "display same page title as header" in {

      val view = unauthorisedPage()(request, messagesApi.preferred(request))
      view.title() must include(view.getElementsByTag("h1").text())
    }

    "have heading" in {

      unauthorisedView.getElementById("error-header").text() mustBe messages("unauthorised.title")
    }
  }
}
