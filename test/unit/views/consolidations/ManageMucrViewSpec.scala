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
import controllers.routes.ChoiceOnConsignmentController
import forms.ManageMucrChoice
import forms.UcrType.Mucr
import models.UcrBlock
import models.cache.AssociateUcrAnswers
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.{Form, FormError}
import play.api.mvc.AnyContentAsEmpty
import views.ViewSpec
import views.html.consolidations.manage_mucr

class ManageMucrViewSpec extends ViewSpec with Injector {

  private implicit val request: JourneyRequest[AnyContentAsEmpty.type] = journeyRequest(AssociateUcrAnswers())
  private val requestReadyToSubmit = journeyRequest(AssociateUcrAnswers(readyToSubmit = Some(true)))

  private val page = instanceOf[manage_mucr]
  private val form: Form[ManageMucrChoice] = ManageMucrChoice.form

  private val ucrBlock = Some(UcrBlock(ucr = "mucr", ucrType = Mucr))

  "MUCR options" should {

    "have the page's title prefixed with 'Error:'" when {
      "the page has errors" in {
        val view = page(form.withGlobalError("error.summary.title"), ucrBlock)
        view.head.getElementsByTag("title").first.text must startWith("Error: ")
      }
    }

    "have the correct title" in {
      page(form, ucrBlock).getTitle must containMessage("manageMucr.title")
    }

    "have the correct heading" in {
      page(form, ucrBlock).getElementById("section-header") must containMessage("manageMucr.heading", "mucr")
    }

    "render the correct labels" in {
      val view = page(form, ucrBlock)
      view.getElementsByAttributeValue("for", "choice").first() must containMessage("manageMucr.associate.this.consignment")
      view.getElementsByAttributeValue("for", "choice-2").first() must containMessage("manageMucr.associate.other.consignment")
    }

    "display 'Back' button" in {
      val backButton = page(form, ucrBlock).getBackButton

      backButton mustBe defined
      backButton.foreach { button =>
        button must haveHref(ChoiceOnConsignmentController.displayChoices)
        button must containMessage("site.back")
      }
    }

    "render error summary" when {
      "no errors" in {
        page(form, ucrBlock).getErrorSummary mustBe empty
      }

      "some errors" in {
        val view: Document = page(form.withError(FormError("choice", "manageMucr.input.error.empty")), ucrBlock)

        view must haveGovUkGlobalErrorSummary
        view must haveGovUkFieldError("choice", messages("manageMucr.input.error.empty"))
      }
    }

    checkAllSaveButtonsAreDisplayed(page(form, ucrBlock)(requestReadyToSubmit, messages(requestReadyToSubmit)))

    checkSaveAndReturnToSummaryButtonIsHidden(page(form, ucrBlock))
  }
}
