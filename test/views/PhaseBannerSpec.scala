/*
 * Copyright 2024 HM Revenue & Customs
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

import base.OverridableInjector
import config.AppConfig
import org.mockito.MockitoSugar.{mock, reset, when}
import org.scalatest.BeforeAndAfterEach
import play.api.inject.bind
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.html.components.gds.phaseBanner

class PhaseBannerSpec extends ViewSpec with BeforeAndAfterEach {

  private val appConfig = mock[AppConfig]
  private val injector = new OverridableInjector(bind[AppConfig].toInstance(appConfig))
  private val bannerPartial = injector.instanceOf[phaseBanner]

  private val requestPath = "/customs-movements/any-page"
  private implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", requestPath)

  private val selfBaseUrlTest = "selfBaseUrlTest"
  private val giveFeedbackLinkTest = "giveFeedbackLinkTest?service=Exports-Movements"

  private def createBanner: Html = bannerPartial("")(fakeRequest, messages)

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(appConfig)
    when(appConfig.selfBaseUrl).thenReturn(Some(selfBaseUrlTest))
    when(appConfig.giveFeedbackLink).thenReturn(giveFeedbackLinkTest)
  }

  override def afterEach(): Unit = {
    reset(appConfig)
    super.afterEach()
  }

  "Phase banner" should {
    "display feedback link with correct href" when {

      "selfBaseUrl is defined" in {
        val href = s"$giveFeedbackLinkTest&backUrl=$selfBaseUrlTest$requestPath"
        createBanner.getElementsByClass("govuk-phase-banner__text").first.getElementsByTag("a").first must haveHref(href)
      }

      "selfBaseUrl is not defined" in {
        when(appConfig.selfBaseUrl).thenReturn(None)
        val phaseBanner = createBanner.getElementsByClass("govuk-phase-banner__text").first
        phaseBanner.getElementsByTag("a").first must haveHref(s"$giveFeedbackLinkTest")
      }
    }
  }
}
