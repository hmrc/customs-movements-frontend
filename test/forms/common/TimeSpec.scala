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

package forms.common

import java.time.LocalTime

import base.BaseSpec
import helpers.FormMatchers
import play.api.data.{Form, FormError}

class TimeSpec extends BaseSpec with FormMatchers {

  val form: Form[Time] = Form(Time.mapping)

  "Time" should {

    "return string in HH:mm format" in {

      val time = Time(LocalTime.of(10, 10))

      time.toString must be("10:10")
    }

    "format time when user use values like 1, 2, 3, etc..." in {

      val time = Time(LocalTime.of(1, 1))
      val formattedTime = time.toString

      formattedTime mustEqual "01:01"
    }
  }

  "Time object" should {

    "contain all necessary, correct keys" in {

      Time.hourKey must be("hour")
      Time.minuteKey must be("minute")
    }
  }

  "Time mapping" should {

    "return error" when {

      "hour and minute is empty" in {

        val errors = form.bind(Map.empty[String, String]).errors

        errors must contain theSameElementsAs List(
          FormError("hour", "error.required"),
          FormError("minute", "error.required")
        )
      }

      "hour and minute is incorrect" in {
        val errors = form.bind(Map("hour" -> "24", "minute" -> "60")).errors

        errors must contain theSameElementsAs List(
          FormError("", "time.error.invalid")
        )
      }
    }

    "return no error" when {

      "time has correct values" in {

        val inputTime = Map("hour" -> "10", "minute" -> "10")
        val forms = form.bind(inputTime)

        forms mustBe withoutErrors
      }
    }
  }
}
