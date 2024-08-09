/*
 * Copyright 2024 HM Revenue & Customs
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

package models.viewmodels.decoder

import org.scalatest.matchers.must.Matchers
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.FakeRequest
import views.ViewSpec
import views.spec.ViewMatchers

class ILEErrorSpec extends ViewSpec with Matchers with ViewMatchers {

  private implicit val request: Request[AnyContentAsEmpty.type] = FakeRequest().withCSRFToken

  "ILE Error" should {

    "contain all expected errors" in {
      val ileErrorsNames = Set(
        "error.ile.InvalidUcrFormat",
        "error.ile.ClientIdValidationFailed",
        "error.ile.UcrNotAtTopOfConsolidation",
        "error.ile.MucrNotShutConsolidation",
        "error.ile.ParentMucrInSameConsolidation",
        "error.ile.ConsolidationNotFound",
        "error.ile.ConsolidationAlreadyShut",
        "error.ile.UcrTypeNotMatchingUcrFormat",
        "error.ile.DeclarationNotExist",
        "error.ile.UcrAlreadyAssociated",
        "error.ile.NoPriorArrivalFoundAtDepartureLocation",
        "error.ile.DeclarationsMissingP2P",
        "error.ile.DeclarationCancelledOrTerminated",
        "error.ile.UnknownDeclarationIdentifier",
        "error.ile.ConsolidationLevelLimitReached",
        "error.ile.InvalidGoodsDateTime",
        "error.ile.MucrNotShutDeparture",
        "error.ile.FutureDateTimeOverExceeded",
        "error.ile.UcrIsNotMucr",
        "error.ile.UcrNotExist",
        "error.ile.UcrAlreadyDisassociated",
        "error.ile.EmptyMucr",
        "error.ile.LocationBasedPermissionFailed",
        "error.ile.InvalidGoodsLocation",
        "error.ile.MucrAlreadyDeparted",
        "error.ile.UcrRejectedUponArrival",
        "error.ile.AlreadyRetrospectiveArrived",
        "error.ile.PreviouslyArrivedDUCROrMUCR",
        "error.ile.AlreadyArrived",
        "error.ile.UCRDoesNotExist",
        "error.ile.DifferentSubmitterId",
        "error.ile.MucrNotMatchingEac",
        "error.ile.DifferentLocation"
      )

      ILEError.allErrors.map(_.messageKey).toSet mustBe ileErrorsNames
    }

    "contain non-empty code and description for every error" in {
      ILEError.allErrors.foreach { error =>
        error.code mustNot be(empty)
        error.messageKey mustNot be(empty)
      }
    }

    "have translations for all errors" in {
      ILEError.allErrors.foreach(error => messages must haveTranslationFor(s"${error.messageKey}"))
    }

    "have translations for all errors in Welsh" in {
      ILEError.allErrors.foreach(error => messagesCy must haveTranslationFor(s"${error.messageKey}"))
    }
  }

  "ILE Error on apply" should {

    "throw IllegalArgumentException" when {

      "list is empty" in {

        intercept[IllegalArgumentException](ILEError(List.empty))
      }

      "list contains only one element" in {

        intercept[IllegalArgumentException](ILEError(List("code")))
      }

      "list contains more than two elements" in {

        intercept[IllegalArgumentException](ILEError(List("code", "description", "incorrect")))
      }
    }
  }
}
