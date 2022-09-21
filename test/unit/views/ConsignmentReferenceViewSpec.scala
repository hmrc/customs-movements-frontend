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
import org.jsoup.nodes.Document
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.FormError
import views.html.consignment_references

class ConsignmentReferenceViewSpec extends ViewSpec with Injector with MockitoSugar {

  private implicit val request = journeyRequest(ArrivalAnswers())

  private val page = instanceOf[consignment_references]

  private val goodsDirection = JourneyType.ARRIVE

  "View" should {
    "render title" in {
      page(ConsignmentReferences.form(goodsDirection)).getTitle must containMessage("consignmentReferences.ARRIVE.question")
    }

    "render heading" in {
      page(ConsignmentReferences.form(goodsDirection)).getElementById("section-header") must containMessage("consignmentReferences.ARRIVE.heading")
    }

    "render options" in {
      page(ConsignmentReferences.form(goodsDirection)).getElementsByAttributeValue("for", "reference").first() must containMessage(
        "consignmentReferences.reference.ducr"
      )
      page(ConsignmentReferences.form(goodsDirection)).getElementsByAttributeValue("for", "reference-2").first() must containMessage(
        "consignmentReferences.reference.mucr"
      )
    }

    "render labels" in {
      page(ConsignmentReferences.form(goodsDirection)).getElementsByAttributeValue("for", "mucrValue").first() must containMessage(
        "site.inputText.mucr.label"
      )
      page(ConsignmentReferences.form(goodsDirection)).getElementsByAttributeValue("for", "ducrValue").first() must containMessage(
        "site.inputText.ducr.label"
      )
    }

    "render hint above DUCR input" in {
      page(ConsignmentReferences.form(goodsDirection)).getElementsByAttributeValue("id", "ducrValue-hint").first() must containMessage(
        "consignmentReferences.reference.ducr.hint"
      )
    }

    "display DUCR invalid" in {
      val view: Document = page(ConsignmentReferences.form(goodsDirection).fillAndValidate(ConsignmentReferences(Ducr, "incorrectDucr")))

      view must haveGovUkGlobalErrorSummary
      view must haveGovUkFieldError("ducrValue", messages("consignmentReferences.reference.ducrValue.error"))
    }

    "render the back button" in {
      val backButton = page(ConsignmentReferences.form(goodsDirection)).getBackButton

      backButton mustBe defined
      backButton.get must haveHref(DucrPartChiefController.displayPage())
    }

    "render error summary" when {

      "no errors" in {
        page(ConsignmentReferences.form(goodsDirection)).getErrorSummary mustBe empty
      }

      "some errors" in {
        val view: Document =
          page(ConsignmentReferences.form(goodsDirection).withError(FormError("reference", "consignmentReferences.reference.empty.arrive")))

        view must haveGovUkGlobalErrorSummary
        view must haveGovUkFieldError("reference", messages("consignmentReferences.reference.empty.arrive"))
      }
    }

    checkAllSaveButtonsAreDisplayed(page(ConsignmentReferences.form(goodsDirection)))

  }
}
