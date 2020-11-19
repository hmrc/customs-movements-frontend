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
import play.twirl.api.Html
import views.html.components.gds.phaseBanner

class PhaseBannerSpec extends ViewSpec with Injector {

  private val banner = instanceOf[phaseBanner]

  private val fakeRequestPath = "/customs-movements/start"
  private implicit val request = FakeRequest("GET", fakeRequestPath)

  private def createBanner(): Html = banner("")(request, messages)

  "Phase banner" should {
    val banner = createBanner()

    "display banner with the correct feedback link" in {
      banner.getElementsByClass("govuk-phase-banner__text").first().getElementsByTag("a").first() must haveHref(
        s"http://localhost:9250/contact/beta-feedback-unauthenticated?service=customs-movements-frontend&backUrl=http://localhost$fakeRequestPath"
      )
    }
  }
}
