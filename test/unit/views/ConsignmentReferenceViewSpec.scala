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

import base.Injector
import controllers.routes.DucrPartChiefController
import forms.ConsignmentReferences
import forms.UcrType.Ducr
import models.cache.{ArrivalAnswers, JourneyType}
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.FormError
import views.html.consignment_references

class ConsignmentReferenceViewSpec extends ViewSpec with Injector with MockitoSugar {

  private implicit val request = journeyRequest(ArrivalAnswers())

  private val page = instanceOf[consignment_references]

  private val goodsDirection = JourneyType.ARRIVE

  def view(implicit request: JourneyRequest[_] = request) = page(ConsignmentReferences.form(goodsDirection))

  "View" should {
    "render title" in {
      view().getTitle must containMessage("consignmentReferences.ARRIVE.question")
    }

    "render heading" in {
      view().getElementById("section-header") must containMessage("consignmentReferences.ARRIVE.heading")
    }

    "render options" in {
      view().getElementsByAttributeValue("for", "reference").first() must containMessage("consignmentReferences.reference.ducr")
      view().getElementsByAttributeValue("for", "reference-2").first() must containMessage("consignmentReferences.reference.mucr")
    }

    "render labels" in {
      view().getElementsByAttributeValue("for", "mucrValue").first() must containMessage("site.inputText.mucr.label")
      view().getElementsByAttributeValue("for", "ducrValue").first() must containMessage("site.inputText.ducr.label")
    }

    "render hint above DUCR input" in {
      view().getElementsByAttributeValue("id", "ducrValue-hint").first() must containMessage("consignmentReferences.reference.ducr.hint")
    }

    "display DUCR invalid" in {
      val view: Document = page(ConsignmentReferences.form(goodsDirection).fillAndValidate(ConsignmentReferences(Ducr, "incorrectDucr")))

      view must haveGovUkGlobalErrorSummary
      view must haveGovUkFieldError("ducrValue", messages("consignmentReferences.reference.ducrValue.error"))
    }

    "render the back button" in {
      val backButton = view().getBackButton

      backButton mustBe defined
      backButton.get must haveHref(DucrPartChiefController.displayPage())
      backButton.get must containMessage("site.back.previousQuestion")

    }

    "render error summary" when {

      "no errors" in {
        view().getErrorSummary mustBe empty
      }

      "some errors" in {
        val view: Document =
          page(ConsignmentReferences.form(goodsDirection).withError(FormError("reference", "consignmentReferences.reference.empty.arrive")))

        view must haveGovUkGlobalErrorSummary
        view must haveGovUkFieldError("reference", messages("consignmentReferences.reference.empty.arrive"))
      }
    }

    checkAllSaveButtonsAreDisplayed(view(journeyRequest(ArrivalAnswers(readyToSubmit = Some(true)))))

    checkSaveAndReturnToSummaryButtonIsHidden(view())

  }
}
