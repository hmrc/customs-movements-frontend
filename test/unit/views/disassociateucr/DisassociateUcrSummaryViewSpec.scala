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

package views.disassociateucr

import base.OverridableInjector
import config.IleQueryConfig
import forms.DisassociateUcr
import forms.UcrType.Ducr
import models.cache.DisassociateUcrAnswers
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import views.ViewSpec
import views.html.disassociateucr.disassociate_ucr_summary
import views.tags.ViewTest

@ViewTest
class DisassociateUcrSummaryViewSpec extends ViewSpec with MockitoSugar with BeforeAndAfterEach {

  private implicit val request = journeyRequest(DisassociateUcrAnswers())
  val disassociateUcr = DisassociateUcr(Ducr, ducr = Some("SOME-DUCR"), mucr = None)
  private val ileQueryConfig = mock[IleQueryConfig]
  private val injector = new OverridableInjector(bind[IleQueryConfig].toInstance(ileQueryConfig))
  private val page = injector.instanceOf[disassociate_ucr_summary]

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(ileQueryConfig.isIleQueryEnabled).thenReturn(true)
  }

  override def afterEach(): Unit = {
    reset(ileQueryConfig)

    super.afterEach()
  }

  "Disassociate Ucr Summary View" should {

    "display page heading" in {
      page(disassociateUcr).getElementById("title") must containMessage("disassociate.ucr.summary.title")
    }

    "display 'Confirm and submit' button on page" in {

      val view = page(disassociateUcr)
      view.getElementsByClass("govuk-button").text() mustBe messages("site.confirmAndSubmit")
    }

    "display 'Reference' link on page" in {

      val view = page(disassociateUcr)
      view.getElementsByClass("govuk-summary-list__value").first() must containText("SOME-DUCR")
    }

    "display 'Change' link on page when ileQuery disabled" in {

      when(ileQueryConfig.isIleQueryEnabled).thenReturn(false)

      val view = page(disassociateUcr)
      val changeButton = view.getElementsByClass("govuk-link").get(1)
      changeButton must containMessage("site.change")
      changeButton must haveAttribute("href", controllers.consolidations.routes.DisassociateUcrController.displayPage().url)
    }

    "not display 'Change' link when ileQuery enabled (Sign out link only)" in {

      when(ileQueryConfig.isIleQueryEnabled).thenReturn(true)

      val links = page(disassociateUcr).getElementsByClass("govuk-link")

      links.size() mustBe 2
    }

    "not have 'Back' button when ileQuery enabled" in {

      when(ileQueryConfig.isIleQueryEnabled).thenReturn(true)

      page(disassociateUcr).getBackButton must not be defined
    }

    "have 'Back' button when ileQuery disabled" in {

      when(ileQueryConfig.isIleQueryEnabled).thenReturn(false)

      page(disassociateUcr).getBackButton must not be defined

    }

  }

}
