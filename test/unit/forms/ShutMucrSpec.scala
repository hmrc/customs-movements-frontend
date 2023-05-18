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

package forms

import base.UnitSpec
import play.api.data.FormError
import play.api.libs.json.{JsValue, Json}

class ShutMucrSpec extends UnitSpec {

  import ShutMucrSpec._

  "ShutMucr mapping" should {

    "return error" when {

      "provided MUCR is empty" in {
        val errors = ShutMucr.form().bind(emptyShutMucrJSON, JsonBindMaxChars).errors

        errors.length must be(1)
        errors.head must equal(FormError("mucr", "error.mucr.empty"))
      }

      "provided MUCR is in incorrect format" in {
        val errors = ShutMucr.form().bind(incorrectShutMucrJSON, JsonBindMaxChars).errors

        errors.length must be(1)
        errors.head must equal(FormError("mucr", "error.mucr.format"))
      }

      "provided MUCR length is over 35 characters long" in {
        val errors = ShutMucr.form().bind(Json.obj("mucr" -> "gb/abced1234-15804test12345678901234"), JsonBindMaxChars).errors

        errors.length must be(1)
        errors.head must equal(FormError("mucr", "error.mucr.format"))
      }
    }

    "return no errors" when {
      "provided MUCR is correct" in {
        ShutMucr.form().bind(correctShutMucrJSON, JsonBindMaxChars).errors must be(empty)
      }
    }

    "convert to upper case" when {

      "provided MUCR is lower case" in {
        val form = ShutMucr.form().bind(Json.obj("mucr" -> " gb/abced1234-15804test "), JsonBindMaxChars)
        form.errors mustBe empty
        form.value.map(_.mucr) must be(Some("GB/ABCED1234-15804TEST"))
      }

      "provided MUCR is lower case and is 35 characters long" in {
        val form = ShutMucr.form().bind(Json.obj("mucr" -> " gb/abced1234-15804test1234567890123 "), JsonBindMaxChars)
        form.errors mustBe empty
        form.value.map(_.mucr) must be(Some("GB/ABCED1234-15804TEST1234567890123"))
      }
    }
  }

}

object ShutMucrSpec {
  val correctMucr: String = " GB/44ZKKLA1VD-AWLUD26N35DA "
  val incorrectMucr: String = "GB/44ZKKLA1VD-AWLUD26N35DA!@#$%^&*"
  val correctShutMucrJSON: JsValue = Json.toJson(ShutMucr(correctMucr))
  val incorrectShutMucrJSON: JsValue = Json.toJson(ShutMucr(incorrectMucr))
  val emptyShutMucrJSON: JsValue = Json.toJson(ShutMucr(" "))
}
