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

import base.FormBaseSpec
import play.api.data.{Form, FormError}

class TimeSpec extends FormBaseSpec {

  val form: Form[Time] = Form(Time.mapping)

  "Time" should {

    "return string in HH:mm format" in {

      val time = Time(Some("10"), Some("10"))

      time.toString must be("10:10")
    }

    "format time when user use values like 1, 2, 3, etc..." in {

      val time = Time(Some("1"), Some("1"))
      val formattedTime = time.formatTime()

      formattedTime must be(Time(Some("01"), Some("01")))
    }

    "format time when values contain more than two digits (started with 0)" in {

      val time = Time(Some("00001"), Some("000001"))
      val formattedTime = time.formatTime()

      formattedTime must be(Time(Some("01"), Some("01")))
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

        val inputTime = Time(None, None)
        val errors = form.fillAndValidate(inputTime).errors

        errors.length must be(2)
        errors(0) must be(FormError("hour", "dateTime.time.hour.empty"))
        errors(1) must be(FormError("minute", "dateTime.time.minute.empty"))
      }

      "hour and minute is incorrect" in {

        val inputTime = Time(Some("24"), Some("60"))
        val errors = form.fillAndValidate(inputTime).errors

        errors.length must be(2)
        errors(0) must be(FormError("hour", "dateTime.time.hour.error"))
        errors(1) must be(FormError("minute", "dateTime.time.minute.error"))
      }
    }

    "return no error" when {

      "time has correct values" in {

        val inputTime = Time(Some("10"), Some("10"))
        val errors = form.fillAndValidate(inputTime).errors

        errors.length must be(0)
      }
    }
  }
}
