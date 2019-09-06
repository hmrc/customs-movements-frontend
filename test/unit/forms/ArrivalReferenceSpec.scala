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

package unit.forms

import base.TestDataHelper
import forms.ArrivalReference
import forms.ArrivalReference.form
import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.FormError

class ArrivalReferenceSpec extends WordSpec with MustMatchers {

  "Arrival Reference form" should {

    "return no errors" when {

      "reference is empty" in {

        val emptyReference = ArrivalReference(None)

        form.fillAndValidate(emptyReference).errors mustBe empty
      }

      "reference is alphanumeric and no longer than 25 characters" in {

        val correctReference = ArrivalReference(Some("123456"))

        form.fillAndValidate(correctReference).errors mustBe empty
      }
    }

    "return error" when {

      "reference contains special characters" in {

        val incorrectReference = ArrivalReference(Some("!@#$%"))

        val errors = form.fillAndValidate(incorrectReference).errors
        val expectedErrors = Seq(FormError("reference", "arrivalReference.error"))

        errors mustBe expectedErrors
      }

      "reference is longer than 25 characters" in {

        val incorrectReference = ArrivalReference(Some(TestDataHelper.createRandomAlphanumericString(26)))

        val errors = form.fillAndValidate(incorrectReference).errors
        val expectedErrors = Seq(FormError("reference", "arrivalReference.error"))

        errors mustBe expectedErrors
      }
    }
  }
}
