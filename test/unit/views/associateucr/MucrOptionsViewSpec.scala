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

package views.associateucr

import base.OverridableInjector
import config.IleQueryConfig
import forms.UcrType.Mucr
import forms.{ManageMucrChoice, MucrOptions}
import models.UcrBlock
import models.cache.AssociateUcrAnswers
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.{Form, FormError}
import play.api.inject.bind
import views.ViewSpec
import views.html.associateucr.mucr_options

class MucrOptionsViewSpec extends ViewSpec with MockitoSugar with BeforeAndAfterEach {

  private val ileQueryConfig = mock[IleQueryConfig]
  private val injector = new OverridableInjector(bind[IleQueryConfig].toInstance(ileQueryConfig))

  private val page = injector.instanceOf[mucr_options]

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(ileQueryConfig.isIleQueryEnabled).thenReturn(true)
  }

  override def afterEach(): Unit = {
    reset(ileQueryConfig)

    super.afterEach()
  }

  private implicit val request = journeyRequest(AssociateUcrAnswers(manageMucrChoice = Some(ManageMucrChoice(ManageMucrChoice.AssociateAnotherMucr))))

  private val form: Form[MucrOptions] = MucrOptions.form

  private val queryUcr = Some(UcrBlock(ucr = "mucr", ucrType = Mucr))
  private val manageMucr = Some(ManageMucrChoice(ManageMucrChoice.AssociateAnotherMucr))

  private def createView(form: Form[MucrOptions] = form)(implicit request: JourneyRequest[_] = request) =
    page(form, queryUcr, manageMucr)

  "MUCR options" should {

    "have the correct title" in {
      createView().getTitle must containMessage("mucrOptions.title")
    }

    "have the correct heading" in {
      createView().getElementById("section-header") must containMessage("mucrOptions.heading", "mucr")
    }

    "have the body text" in {
      val body = createView().getElementsByClass("govuk-body").first()

      body must containMessage("mucrOptions.paragraph", messages("mucrOptions.paragraph.link"))
      body.getElementsByTag("a").first() must haveHref(
        "https://www.gov.uk/government/publications/uk-trade-tariff-cds-volume-3-export-declaration-completion-guide/group-2-references-of-messages-document-certificates-and-authorisations#de-21-simplified-declaration-previous-documents-box-40-declaration-previous-document"
      )
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
      when(ileQueryConfig.isIleQueryEnabled).thenReturn(false)
      val backButton = createView().getBackButton

      backButton mustBe defined
      backButton.foreach { button =>
        button must haveHref(controllers.routes.ChoiceController.displayChoiceForm())
        button must containMessage("site.back.previousQuestion")
      }
    }

    "display 'Back' button that links to 'manage mucr page when ileQuery enabled" in {
      when(ileQueryConfig.isIleQueryEnabled).thenReturn(true)
      val backButton = createView().getBackButton

      backButton mustBe defined
      backButton.foreach { button =>
        button must haveHref(controllers.consolidations.routes.ManageMucrController.displayPage())
        button must containMessage("site.back.previousQuestion")
      }
    }

    "render error summary" when {
      "no errors" in {
        createView().getErrorSummary mustBe empty
      }

      "some errors" in {
        val view: Document = createView(form.withError(FormError("createOrAdd", "mucrOptions.reference.value.error.empty")))

        view must haveGovUkGlobalErrorSummary
        view must haveGovUkFieldError("createOrAdd", messages("mucrOptions.reference.value.error.empty"))
      }
    }

    checkAllSaveButtonsAreDisplayed(createView(form)(journeyRequest(AssociateUcrAnswers(readyToSubmit = Some(true)))))

    checkSaveAndReturnToSummaryButtonIsHidden(createView())

  }
}
