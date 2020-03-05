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

package views.disassociateucr

import base.OverridableInjector
import config.AppConfig
import forms.DisassociateKind._
import forms.DisassociateUcr
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import views.ViewSpec
import views.html.disassociateucr.disassociate_ucr_summary
import views.tags.ViewTest

@ViewTest
class DisassociateUcrSummaryViewSpec extends ViewSpec with MockitoSugar with BeforeAndAfterEach {

  private implicit val request = FakeRequest().withCSRFToken

  private val appConfig = mock[AppConfig]
  private val injector = new OverridableInjector(bind[AppConfig].toInstance(appConfig))

  private val page = injector.instanceOf[disassociate_ucr_summary]

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(appConfig.ileQueryEnabled).thenReturn(true)
  }

  override def afterEach(): Unit = {
    reset(appConfig)

    super.afterEach()
  }

  val disassociateUcr = DisassociateUcr(Ducr, ducr = Some("SOME-DUCR"), mucr = None)

  "Disassociate Ucr Summary View" should {

    "display 'Confirm and submit' button on page" in {
      val view = page(disassociateUcr)
      view.getElementsByClass("govuk-button").text() mustBe messages("site.confirmAndSubmit")
    }

    "display 'Reference' link on page" in {
      val view = page(disassociateUcr)
      view.getElementsByClass("govuk-summary-list__value").first() must containText("SOME-DUCR")
    }

    "display 'Change' link on page when ileQuery disabled" in {
      when(appConfig.ileQueryEnabled).thenReturn(false)

      val view = page(disassociateUcr)
      val changeButton = view.getElementsByClass("govuk-link").first()
      changeButton must containMessage("site.change")
      changeButton must haveAttribute("href", controllers.consolidations.routes.DisassociateUcrController.displayPage().url)
    }

    "not display 'Change' link when ileQuery enabled" in {
      when(appConfig.ileQueryEnabled).thenReturn(true)

      val links = page(disassociateUcr).getElementsByClass("govuk-link")

      links mustBe empty
    }

    "have 'Back' button when ileQuery enabled" in {
      when(appConfig.ileQueryEnabled).thenReturn(true)

      val backButton = page(disassociateUcr).getBackButton

      backButton mustBe defined
      backButton.get must haveHref(controllers.routes.ChoiceController.displayChoiceForm())
    }

    "have 'Back' button when ileQuery disabled" in {
      when(appConfig.ileQueryEnabled).thenReturn(false)

      val backButton = page(disassociateUcr).getBackButton

      backButton mustBe defined
      backButton.get must haveHref(controllers.consolidations.routes.DisassociateUcrController.displayPage())
    }

  }

}
