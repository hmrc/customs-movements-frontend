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
import play.api.test.FakeRequest
import views.html.unauthorised

class UnauthorisedViewSpec extends ViewSpec with Injector {

  private implicit val request = FakeRequest()
  private val unauthorisedPage = instanceOf[unauthorised]

  "Unauthorised messages" should {

    "have correct content" in {

      messages("unauthorised.title") mustBe "You can’t access this service with this account"
    }
  }

  "Unauthorised page" should {

    "display same page title as header" in {

      unauthorisedPage().getTitle must containMessage("unauthorised.title")
    }

    "have heading" in {

      unauthorisedPage().getElementById("title") must containMessage("unauthorised.title")
    }
  }
}
