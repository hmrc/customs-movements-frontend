/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import views.html.loading_screen
import views.tags.ViewTest

@ViewTest
class LoadingScreenViewSpec extends ViewSpec with Injector {

  private implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val page = instanceOf[loading_screen]
  private val view = page()

  "Loading screen" should {

    "render title" in {

      view.getTitle must containMessage("ileQuery.loading.title")
    }

    "render page header" in {

      view.getElementById("title") must containMessage("ileQuery.loading.title")
    }

    "render page hint" in {

      view.getElementById("main-content") must containMessage("ileQuery.loading.hint")
    }
  }
}
