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

package views.shutmucr

import base.OverridableInjector
import config.IleQueryConfig
import controllers.consolidations.routes
import forms.ShutMucr
import models.cache.ShutMucrAnswers
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import testdata.CommonTestData.validMucr
import views.ViewSpec
import views.html.shutmucr.shut_mucr_summary

class ShutMucrSummaryViewSpec extends ViewSpec with MockitoSugar with BeforeAndAfterEach {

  private implicit val request = journeyRequest(ShutMucrAnswers())
  val shutMucr = ShutMucr(validMucr)
  private val ileQueryConfig = mock[IleQueryConfig]
  private val injector = new OverridableInjector(bind[IleQueryConfig].toInstance(ileQueryConfig))
  private val shutMucrSummaryPage = injector.instanceOf[shut_mucr_summary]

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(ileQueryConfig.isIleQueryEnabled).thenReturn(true)
  }

  override def afterEach(): Unit = {
    reset(ileQueryConfig)

    super.afterEach()
  }

  "Shut Mucr Summary View" should {

    "display page heading" in {
      shutMucrSummaryPage(shutMucr).getElementById("title") must containMessage("shutMucr.summary.title")
    }

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

    "not display change button when ileQuery enabled (Sign out link only)" in {

      when(ileQueryConfig.isIleQueryEnabled).thenReturn(true)

      shutMucrSummaryPage(shutMucr).getElementsByClass("govuk-link").size() mustBe 2
    }

    "display correct change button when ileQuery disabled" in {

      when(ileQueryConfig.isIleQueryEnabled).thenReturn(false)

      val changeButton = shutMucrSummaryPage(shutMucr).getElementsByClass("govuk-link").get(1)

      changeButton must haveHref(routes.ShutMucrController.displayPage())
      changeButton.text() must include(messages("site.edit"))
    }

    "have 'Back' button when ileQuery enabled" in {

      when(ileQueryConfig.isIleQueryEnabled).thenReturn(true)

      shutMucrSummaryPage(shutMucr).getBackButton must not be defined

    }

    "have 'Back' button when ileQuery disabled" in {

      when(ileQueryConfig.isIleQueryEnabled).thenReturn(false)

      shutMucrSummaryPage(shutMucr).getBackButton must not be defined
    }

  }
}
