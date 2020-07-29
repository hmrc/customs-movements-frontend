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

package forms.common

import base.BaseSpec
import helpers.FormMatchers
import play.api.data.{Form, FormError}
import Time._

class TimeSpec extends BaseSpec with FormMatchers {

  val form: Form[Time] = Form(Time.mapping)

  "Time object" should {

    "contain all necessary, correct keys" in {

      hourKey must be("hour")
      minuteKey must be("minute")
      ampmKey must be("ampm")
    }
  }

  "Time mapping" should {

    "return error" when {

      "hour and minute is empty" in {

        val errors = form.bind(Map.empty[String, String]).errors

        errors must contain theSameElementsAs List(
          FormError("hour", "error.required"),
          FormError("minute", "error.required"),
          FormError("ampm", "error.required")
        )
      }

      "hour is incorrect" in {
        val inputTime = Map(hourKey -> "13", minuteKey -> "10", ampmKey -> "AM")
        val errors = form.bind(inputTime).errors

        errors must contain theSameElementsAs List(FormError("", "time.error.invalid"))
      }

      "minute is incorrect" in {
        val inputTime = Map(hourKey -> "10", minuteKey -> "60", ampmKey -> "AM")
        val errors = form.bind(inputTime).errors

        errors must contain theSameElementsAs List(FormError("", "time.error.invalid"))
      }

      "am/pm is incorrect" in {
        val inputTime = Map(hourKey -> "10", minuteKey -> "10", ampmKey -> "am")
        val errors = form.bind(inputTime).errors

        errors must contain theSameElementsAs List(FormError("ampm", "time.ampm.error"))
      }
    }

    "return no error" when {

      "time has correct values" in {

        val inputTime = Map(hourKey -> "10", minuteKey -> "10", ampmKey -> "AM")
        val forms = form.bind(inputTime)

        forms mustBe withoutErrors
      }
    }
  }
}
