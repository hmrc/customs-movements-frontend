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

package views.components

import base.Injector
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.ViewSpec
import views.html.components.gds.siteHeader

class SiteHeaderSpec extends ViewSpec with Injector {

  private val page = instanceOf[siteHeader]
  private implicit val request = FakeRequest()

  private def createHeader(): Html = page()(messages)

  "Site header" should {
    val siteHeader = createHeader()

    "display banner with the service name" in {
      siteHeader.getElementsByClass("govuk-header__link govuk-header__link--service-name").first() must containMessage("service.name")
    }

  }
}
