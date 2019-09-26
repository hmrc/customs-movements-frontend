/*
 * Copyright 2019 HM Revenue & Customs
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

import org.scalatest.{MustMatchers, WordSpec}

class ILEErrorSpec extends WordSpec with MustMatchers {

  "ILE Error" should {

    "have correct amount of codes" in {

      val expectedCodesAmount = 29
      ILEError.allErrors.size mustBe expectedCodesAmount
    }

    "contain non-empty code and description for every error" in {

      ILEError.allErrors.foreach { error =>
        error.code mustNot be(empty)
        error.messageKey mustNot be(empty)
      }
    }

    "contain correct prefix for all message keys" in {

      val expectedPrefix = "decoder.ileError."

      ILEError.allErrors.foreach { error =>
        error.messageKey must include(expectedPrefix)
      }
    }
  }

  "ILE Error on apply" should {

    "throw IllegalArgumentException" when {

      "list is empty" in {

        intercept[IllegalArgumentException] { ILEError(List.empty) }
      }

      "list contains only one element" in {

        intercept[IllegalArgumentException] { ILEError(List("code")) }
      }

      "list contains more than two elements" in {

        intercept[IllegalArgumentException] { ILEError(List("code", "description", "incorrect")) }
      }
    }
  }
}
