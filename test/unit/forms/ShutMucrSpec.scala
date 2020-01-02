/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.libs.json.{JsValue, Json}

class ShutMucrSpec extends BaseSpec {

  import ShutMucrSpec._

  "ShutMucr mapping" should {

    "return error" when {

      "provided MUCR is empty" in {

        val errors = ShutMucr.form().bind(emptyShutMucrJSON).errors

        errors.length must be(1)
        errors.head must equal(FormError("mucr", "error.mucr.empty"))
      }

      "provided MUCR is in incorrect format" in {

        val errors = ShutMucr.form().bind(incorrectShutMucrJSON).errors

        errors.length must be(1)
        errors.head must equal(FormError("mucr", "error.mucr.format"))
      }
    }

    "return no errors" when {

      "provided MUCR is correct" in {

        ShutMucr.form().bind(correctShutMucrJSON).errors must be(empty)
      }
    }
  }

}

object ShutMucrSpec {

  val correctMucr: String = "GB/44ZKKLA1VD-AWLUD26N35DA"
  val incorrectMucr: String = "GB/44ZKKLA1VD-AWLUD26N35DA!@#$%^&*"
  val correctShutMucrJSON: JsValue = Json.toJson(ShutMucr(correctMucr))
  val incorrectShutMucrJSON: JsValue = Json.toJson(ShutMucr(incorrectMucr))
  val emptyShutMucrJSON: JsValue = Json.toJson(ShutMucr(""))
}
