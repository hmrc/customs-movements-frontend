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

package views.shutmucr

import base.OverridableInjector
import config.AppConfig
import controllers.consolidations.routes
import forms.ShutMucr
import helpers.views.CommonMessages
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import testdata.ConsolidationTestData.validMucr
import views.ViewSpec
import views.html.shutmucr.shut_mucr_summary

class ShutMucrSummaryViewSpec extends ViewSpec with CommonMessages with MockitoSugar with BeforeAndAfterEach {

  private implicit val request = FakeRequest().withCSRFToken

  private val appConfig = mock[AppConfig]
  private val injector = new OverridableInjector(bind[AppConfig].toInstance(appConfig))

  private val shutMucrSummaryPage = injector.instanceOf[shut_mucr_summary]

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(appConfig.ileQueryEnabled).thenReturn(true)
  }

  override def afterEach(): Unit = {
    reset(appConfig)

    super.afterEach()
  }

  val shutMucr = ShutMucr(validMucr)

  "Shut Mucr Summary View" should {

    "display page header" in {

      shutMucrSummaryPage(shutMucr).getElementsByClass("govuk-heading-m").text() mustBe messages("shutMucr.summary.header")
    }

    "display MUCR type in summary list" in {

      shutMucrSummaryPage(shutMucr).getElementsByClass("govuk-summary-list__key").text() mustBe messages("shutMucr.summary.type")
    }

    "display correct mucr" in {

      shutMucrSummaryPage(shutMucr).getElementsByClass("govuk-summary-list__value").text() mustBe validMucr
    }

    "display correct submit button" in {

      val submitButton = shutMucrSummaryPage(shutMucr).getElementsByClass("govuk-button").first()

      submitButton.text() mustBe messages("site.confirmAndSubmit")
    }

    "not display change button when ileQuery enabled" in {
      when(appConfig.ileQueryEnabled).thenReturn(true)

      shutMucrSummaryPage(shutMucr).getElementsByClass("govuk-link") mustBe empty
    }

    "display correct change button when ileQuery disabled" in {
      when(appConfig.ileQueryEnabled).thenReturn(false)

      val changeButton = shutMucrSummaryPage(shutMucr).getElementsByClass("govuk-link").first()

      changeButton must haveHref(routes.ShutMucrController.displayPage())
      changeButton.text() must include(messages("site.edit"))
    }

  }
}
