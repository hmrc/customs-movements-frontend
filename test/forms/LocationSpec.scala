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

package forms

import base.BaseSpec

class LocationSpec extends BaseSpec {

  "Location model" should {

    "has correct formId value" in {

      Location.formId must be("Location")
    }
  }

  "Location mapping" should {

    "contains error for every field" when {

      "all fields are empty" in {

        val inputData = Location("", "", "", "")
        val errors = Location.form().fillAndValidate(inputData).errors

        errors.length must be(4)
      }

      "all fields are incorrect" in {

        val inputData = Location("incorrect", "incorrect", "!@#$incorrect", "incorrect")
        val errors = Location.form().fillAndValidate(inputData).errors

        errors.length must be(4)
      }
    }

    "not contains any errors" when {

      "data is correct" in {

        val inputData = Location("A", "U", "correct", "PL")
        val errors = Location.form().fillAndValidate(inputData).errors

        errors.length must be(0)
      }
    }
  }
}
