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

import base.FormBaseSpec
import play.api.data.FormError

class LocationSpec extends FormBaseSpec {

  "Location model" should {

    "has correct formId value" in {

      Location.formId must be("Location")
    }
  }

  "Location mapping" should {

    "return error" when {

      "location is too short" in {

        val inputData = Location(Some("1234"))
        val errors = Location.form().fillAndValidate(inputData).errors

        errors.length must be(1)
        errors.head must be(FormError("goodsLocation", "location.error"))
      }

      "location is too long" in {

        val inputData = Location(Some("1234"))
        val errors = Location.form().fillAndValidate(inputData).errors

        errors.length must be(1)
        errors.head must be(FormError("goodsLocation", "location.error"))
      }

      "location contains special characters" in {

        val inputData = Location(Some("1234"))
        val errors = Location.form().fillAndValidate(inputData).errors

        errors.length must be(1)
        errors.head must be(FormError("goodsLocation", "location.error"))
      }
    }

    "return no errors" when {
      "field is empty" in {

        val inputData = Location(None)
        val errors = Location.form().fillAndValidate(inputData).errors

        errors.length must be(0)
      }

      "value is correct" in {

        val inputData = Location(Some("1234abc"))
        val errors = Location.form().fillAndValidate(inputData).errors

        errors.length must be(0)
      }
    }
  }
}
