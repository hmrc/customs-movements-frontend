/*
 * Copyright 2023 HM Revenue & Customs
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

package views.consolidations

import base.Injector
import controllers.consolidations.routes.ManageMucrController
import controllers.routes.{ChoiceOnConsignmentController, DucrPartChiefController, DucrPartDetailsController}
import forms.DucrPartChiefChoice.IsDucrPart
import forms.UcrType.Mucr
import forms.{DucrPartChiefChoice, ManageMucrChoice, MucrOptions}
import models.UcrBlock
import models.cache.{AssociateUcrAnswers, Cache}
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.{Form, FormError}
import play.twirl.api.HtmlFormat.Appendable
import testdata.CommonTestData.validEori
import views.ViewSpec
import views.html.consolidations.mucr_options

class MucrOptionsViewSpec extends ViewSpec with Injector {

  private val page = instanceOf[mucr_options]

  private val form: Form[MucrOptions] = MucrOptions.form

  private val ucrBlock = Some(UcrBlock(ucr = "mucr", ucrType = Mucr))
  private val manageMucr = Some(ManageMucrChoice(ManageMucrChoice.AssociateAnotherMucr))

  private def createView(form: Form[MucrOptions] = form)(implicit request: JourneyRequest[_]): Appendable =
    page(form, ucrBlock, manageMucr)

  "MUCR options" should {

    val manageMucrChoice = Some(ManageMucrChoice(ManageMucrChoice.AssociateAnotherMucr))
    val answer = AssociateUcrAnswers(manageMucrChoice)
    implicit val request = journeyRequest(answer)

    "have the page's title prefixed with 'Error:'" when {
      "the page has errors" in {
        val view = createView(form.withGlobalError("error.summary.title"))
        view.head.getElementsByTag("title").first.text must startWith("Error: ")
      }
    }

    "have the correct title" in {
      createView().getTitle must containMessage("mucrOptions.title")
    }

    "have the correct heading" in {
      createView().getElementById("section-header") must containMessage("mucrOptions.heading", "mucr")
    }

    "have the body text" in {
      val body = createView().getElementsByClass("govuk-body").first()

      val expectedText = messages("mucrOptions.paragraph", messages("mucrOptions.paragraph.link"))
      removeBlanksIfAnyBeforeDot(body.text) mustBe expectedText
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

    "display 'Back' button that links to /ducr-part-created-chief page" when {
      "on a NON-'Find a consignment' journey and" when {
        "the Ucr is NOT of DucrPart type" in {
          val backButton = createView().getBackButton

          backButton mustBe defined
          backButton.foreach { button =>
            button must haveHref(DucrPartChiefController.displayPage)
            button must containMessage("site.back.previousQuestion")
          }
        }
      }
    }

    "display 'Back' button that links to /ducr-part-details page" when {
      "on a NON-'Find a consignment' journey and" when {
        "the Ucr is of DucrPart type" in {
          val cache = Cache(validEori, Some(answer), None, false, Some(DucrPartChiefChoice(IsDucrPart)))
          implicit val request = journeyRequest(cache)
          val backButton = createView().getBackButton

          backButton mustBe defined
          backButton.foreach { button =>
            button must haveHref(DucrPartDetailsController.displayPage)
            button must containMessage("site.back.previousQuestion")
          }
        }
      }
    }

    "display 'Back' button that links to /manage-mucr page" when {
      "on a 'Find a consignment' journey and" when {
        "AssociateUcrAnswers.manageMucrChoice is defined" in {
          implicit val request = journeyRequest(answer, None, true)
          val backButton = createView().getBackButton

          backButton mustBe defined
          backButton.foreach { button =>
            button must haveHref(ManageMucrController.displayPage)
            button must containMessage("site.back.previousQuestion")
          }
        }
      }
    }

    "display 'Back' button that links to /choice-on-consignment page" when {
      "on a 'Find a consignment' journey and" when {
        "AssociateUcrAnswers.manageMucrChoice is NOT defined" in {
          implicit val request = journeyRequest(answer, None, true)
          val backButton = page(form, ucrBlock, None).getBackButton

          backButton mustBe defined
          backButton.foreach { button =>
            button must haveHref(ChoiceOnConsignmentController.displayChoices)
            button must containMessage("site.back.previousQuestion")
          }
        }
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
