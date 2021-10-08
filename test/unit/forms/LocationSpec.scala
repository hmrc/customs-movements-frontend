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

package forms

import base.BaseSpec
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString}

class LocationSpec extends BaseSpec {

  "Location model" should {

    "has correct formId value" in {

      Location.formId must be("Location")
    }
  }

  "Location mapping" should {

    "contains error" when {

      "location is empty" in {

        val inputData = Location("")
        val errors = Location.form().fillAndValidate(inputData).errors

        errors mustBe Seq(FormError("code", "location.code.empty"))
      }

      "location has incorrect country" in {

        val inputData = Location("XXAUcorrect")
        val errors = Location.form().fillAndValidate(inputData).errors

        errors mustBe Seq(FormError("code", "location.code.error"))
      }

      "location has incorrect type" in {

        val inputData = Location("GBEUcorrect")
        val errors = Location.form().fillAndValidate(inputData).errors

        errors mustBe Seq(FormError("code", "location.code.error"))
      }

      "location has incorrect qualifier code" in {

        val inputData = Location("GBAZcorrect")
        val errors = Location.form().fillAndValidate(inputData).errors

        errors mustBe Seq(FormError("code", "location.code.error"))
      }

      "location is shorter than 10 characters" in {

        val inputData = Location("GBAZ123")
        val errors = Location.form().fillAndValidate(inputData).errors

        errors mustBe Seq(FormError("code", "location.code.error"))
      }

      "location is longer than 17 characters" in {

        val inputData = Location("GBAU12345678912345")
        val errors = Location.form().fillAndValidate(inputData).errors

        errors mustBe Seq(FormError("code", "location.code.error"))
      }
    }

    "not contains any errors" when {

      "data is correct" in {

        val inputData = Location("PLAUcorrect")
        val errors = Location.form().fillAndValidate(inputData).errors

        errors.length must be(0)
      }

      "data converted to upper case" in {

        def formData(code: String) =
          JsObject(Map("code" -> JsString(code)))

        val form = Location.form().bind(formData("plaucorrect"), JsonBindMaxChars)

        form.value.map(_.code) must be(Some("PLAUCORRECT"))
      }
    }
  }
}
