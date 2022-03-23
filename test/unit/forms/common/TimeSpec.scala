/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.common.Time._
import helpers.FormMatchers
import play.api.data.{Form, FormError}

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

      "hour is empty" in {
        val inputTime = Map(hourKey -> "", minuteKey -> "10", ampmKey -> "AM")
        val errors = form.bind(inputTime).errors

        errors must contain theSameElementsAs List(FormError("hour", "time.hour.missing"))
      }

      "hour is out of range" in {
        val inputTime = Map(hourKey -> "0", minuteKey -> "10", ampmKey -> "AM")
        val errors = form.bind(inputTime).errors

        errors must contain theSameElementsAs List(FormError("hour", "time.hour.error"))
      }

      "minute is empty" in {
        val inputTime = Map(hourKey -> "10", minuteKey -> "", ampmKey -> "AM")
        val errors = form.bind(inputTime).errors

        errors must contain theSameElementsAs List(FormError("minute", "time.minute.missing"))
      }

      "minute is out of range" in {
        val inputTime = Map(hourKey -> "10", minuteKey -> "60", ampmKey -> "AM")
        val errors = form.bind(inputTime).errors

        errors must contain theSameElementsAs List(FormError("minute", "time.minute.error"))
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

    "bind correct time" when {

      "time is am" in {

        val inputTime = Map(hourKey -> "10", minuteKey -> "15", ampmKey -> "AM")
        val boundForm = form.bind(inputTime)

        boundForm.value mustBe Some(Time(LocalTime.of(10, 15)))
      }

      "time is pm" in {

        val inputTime = Map(hourKey -> "8", minuteKey -> "5", ampmKey -> "PM")
        val boundForm = form.bind(inputTime)

        boundForm.value mustBe Some(Time(LocalTime.of(20, 5)))
      }

    }

    "unbind correct time" when {

      "time is am" in {

        val filledForm = form.fill(Time(LocalTime.of(9, 25)))

        filledForm.data(Time.hourKey) mustBe "9"
        filledForm.data(Time.minuteKey) mustBe "25"
        filledForm.data(Time.ampmKey) mustBe "AM"
      }

      "time is pm" in {

        val filledForm = form.fill(Time(LocalTime.of(20, 5)))

        filledForm.data(Time.hourKey) mustBe "8"
        filledForm.data(Time.minuteKey) mustBe "05"
        filledForm.data(Time.ampmKey) mustBe "PM"
      }

      "after midnight" in {

        val filledForm = form.fill(Time(LocalTime.of(0, 5)))

        filledForm.data(Time.hourKey) mustBe "12"
        filledForm.data(Time.minuteKey) mustBe "05"
        filledForm.data(Time.ampmKey) mustBe "AM"
      }
    }
  }
}
