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

package views.associateucr

import base.OverridableInjector
import config.AppConfig
import forms.{ManageMucrChoice, MucrOptions}
import models.UcrBlock
import models.cache.AssociateUcrAnswers
import org.jsoup.nodes.Document
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.{Form, FormError}
import play.api.inject.bind
import views.ViewSpec
import views.html.associateucr.mucr_options

class MucrOptionsViewSpec extends ViewSpec with MockitoSugar with BeforeAndAfterEach {

  private val appConfig = mock[AppConfig]
  private val injector = new OverridableInjector(bind[AppConfig].toInstance(appConfig))

  private val page = injector.instanceOf[mucr_options]

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(appConfig.ileQueryEnabled).thenReturn(true)
  }

  override def afterEach(): Unit = {
    reset(appConfig)

    super.afterEach()
  }

  private implicit val request = journeyRequest(AssociateUcrAnswers(manageMucrChoice = Some(ManageMucrChoice(ManageMucrChoice.AssociateAnotherMucr))))

  private val form: Form[MucrOptions] = MucrOptions.form

  private val queryUcr = Some(UcrBlock("mucr", "m"))
  private val manageMucr = Some(ManageMucrChoice(ManageMucrChoice.AssociateAnotherMucr))

  private def createView(form: Form[MucrOptions] = form) = page(form, queryUcr, manageMucr)

  "MUCR options" should {

    "have the correct title" in {
      createView().getTitle must containMessage("mucrOptions.title")
    }

    "have the correct heading" in {
      createView().getElementById("section-header") must containMessage("mucrOptions.heading", "mucr")
    }

    "render the correct labels and hints" in {
      val view = createView()
      view.getElementsByAttributeValue("for", "existingMucr").first() must containMessage("site.inputText.mucr.label")
      view.getElementsByAttributeValue("for", "newMucr").first() must containMessage("site.inputText.newMucr.label")
      view.getElementById("newMucr-hint") must containMessage("site.inputText.newMucr.label.hint")
    }

    "have no options selected on initial display" in {
      val view = createView()
      view.getElementById("createOrAdd") mustBe unchecked
      view.getElementById("createOrAdd-2") mustBe unchecked
    }

    "display 'Back' button that links to start page when ileQuery disabled" in {
      when(appConfig.ileQueryEnabled).thenReturn(false)
      val backButton = createView().getBackButton

      backButton mustBe defined
      backButton.foreach(button => {
        button must haveHref(controllers.routes.ChoiceController.displayChoiceForm())
        button must containMessage("site.back")
      })
    }

    "display 'Back' button that links to 'manage mucr page when ileQuery enabled" in {
      when(appConfig.ileQueryEnabled).thenReturn(true)
      val backButton = createView().getBackButton

      backButton mustBe defined
      backButton.foreach(button => {
        button must haveHref(controllers.consolidations.routes.ManageMucrController.displayPage())
        button must containMessage("site.back")
      })
    }

    "display 'Continue' button on page" in {
      createView().getElementsByClass("govuk-button").first() must containMessage("site.continue")
    }

    "render error summary" when {
      "no errors" in {
        createView().getErrorSummary mustBe empty
      }

      "some errors" in {
        val view: Document = createView(form.withError(FormError("createOrAdd", "mucrOptions.createAdd.value.empty")))

        view must haveGovUkGlobalErrorSummary
        view must haveGovUkFieldError("createOrAdd", messages("mucrOptions.createAdd.value.empty"))
      }
    }
  }
}
