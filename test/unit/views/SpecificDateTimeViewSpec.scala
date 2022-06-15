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

import base.OverridableInjector
import config.IleQueryConfig
import forms.SpecificDateTimeChoice
import models.cache.{ArrivalAnswers, DepartureAnswers}
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.bind
import play.twirl.api.Html
import views.html.specific_date_and_time

class SpecificDateTimeViewSpec extends ViewSpec with MockitoSugar with BeforeAndAfterEach {

  private val appConfig = mock[IleQueryConfig]
  private val injector = new OverridableInjector(bind[IleQueryConfig].toInstance(appConfig))

  private val page = injector.instanceOf[specific_date_and_time]

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(appConfig.isIleQueryEnabled).thenReturn(true)
  }

  override def afterEach(): Unit = {
    reset(appConfig)

    super.afterEach()
  }

  private val form: Form[SpecificDateTimeChoice] = SpecificDateTimeChoice.form()
  private implicit val request = journeyRequest(ArrivalAnswers())

  private def createView: Html = page(form, "some-reference")

  "SpecificDateTime View on empty page" should {

    "display page title" in {

      createView.getElementsByTag("h1").first() must containMessage("specific.datetime.heading")
    }

    "have the correct section header for the Arrival journey" in {

      createView.getElementById("section-header") must containMessage("specific.datetime.arrive.heading", "some-reference")
    }

    "have the correct section header for the Departure journey" in {

      val departureView = page(form, "some-reference")(journeyRequest(DepartureAnswers()), messages)
      departureView.getElementById("section-header") must containMessage("specific.datetime.depart.heading", "some-reference")
    }

    "display 'Back' button that links to Consignment References when ileQuery disabled" in {
      when(appConfig.isIleQueryEnabled).thenReturn(false)
      val backButton = createView.getBackButton

      backButton mustBe defined
      backButton.foreach { button =>
        button must haveHref(controllers.routes.ConsignmentReferencesController.displayPage())
        button must containMessage("site.back")
      }
    }

    "display 'Back' button that links to Choice when ileQuery enabled" in {
      when(appConfig.isIleQueryEnabled).thenReturn(true)
      val backButton = createView.getBackButton

      backButton mustBe defined
      backButton.foreach { button =>
        button must haveHref(controllers.routes.ChoiceController.displayChoiceForm())
        button must containMessage("site.back")
      }
    }

    "display 'Continue' button on page" in {
      createView.getSubmitButton mustBe defined
      createView.getSubmitButton.get must containMessage("site.continue")
    }
  }
}
