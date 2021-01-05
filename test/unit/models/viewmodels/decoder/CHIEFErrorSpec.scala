/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.test.FakeRequest
import views.ViewSpec
import views.spec.ViewMatchers

class CHIEFErrorSpec extends ViewSpec with ViewMatchers {

  private implicit val request = FakeRequest().withCSRFToken

  val expectedCHIEFError = CHIEFError("E408", "error.chief.UcrNotExist")

  "CHIEF Error" should {

    "have correct amount of codes" in {

      val expectedCodesAmount = 28
      CHIEFError.allErrors.size mustBe expectedCodesAmount
    }

    "contain non-empty code and description for every error" in {

      CHIEFError.allErrors.foreach { error =>
        error.code mustNot be(empty)
        error.messageKey mustNot be(empty)
      }
    }

    "have translations for all errors" in {

      val chiefErrorsNames = Seq(
        "MucrAlreadyShut",
        "UcrNotExist",
        "Refused",
        "UcrAlreadyAssociated",
        "InvalidUcrFormat",
        "InvalidCharacterInUcrOrPart",
        "EmptyConsolidation",
        "CannotDepartMovements",
        "DateTimeBeforeTimeout",
        "DataValidationPrimaryError",
        "InvalidCharacterInMessage",
        "DeclarationFinalised",
        "CancelledEntry",
        "TooManyAnticipatedArrivals",
        "AlreadyArrived",
        "MucrAlreadyInUseWithImports",
        "ArrivalAlreadyNotified",
        "InvalidMrnFormat",
        "MrnNotMatchEntry",
        "CannotArriveContainedMucr",
        "CannotDepartContainedMucr",
        "NotSuitableEntryReference",
        "MucrNotShutForAssociation",
        "CannotShutMovingConsolidation",
        "ShedOperatorNotExist",
        "ServiceSegmentInvalid",
        "ErrorsOnDocument"
      )

      chiefErrorsNames.foreach { errorName =>
        messages must haveTranslationFor(s"error.chief.$errorName")
      }
    }
  }

  "CHIEF Error on apply" should {

    "create CHIEF error based on list with 2 strings" in {

      val correctCHIEFError = List("E408", "error.chief.UcrNotExist")

      CHIEFError(correctCHIEFError) mustBe expectedCHIEFError
    }

    "throw IllegalArgumentException" when {

      "list is empty" in {

        intercept[IllegalArgumentException] { CHIEFError(List.empty) }
      }

      "list contains only one element" in {

        intercept[IllegalArgumentException] { CHIEFError(List("code")) }
      }

      "list contains more than two elements" in {

        intercept[IllegalArgumentException] { CHIEFError(List("code", "description", "incorrect")) }
      }
    }
  }
}
