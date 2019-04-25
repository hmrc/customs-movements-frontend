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

class DateSpec extends FormBaseSpec {

  val form: Form[Date] = Form(Date.mapping)

  "Date" should {

    "correct convert date to 102 format" in {

      val dateInput = Date(Some(1), Some(1), Some(2019))

      dateInput.to102Format must be("20190101")
    }

    "return string in uuuu-MM-dd format for toString method" in {

      val dateInput = Date(Some(3), Some(3), Some(2020))

      dateInput.toString must be("2020-03-03")
    }
  }

  "Object Date" should {

    "contains all necessary, correct keys" in {

      Date.dayKey must be("day")
      Date.monthKey must be("month")
      Date.yearKey must be("year")
    }
  }

  "Date mapping" should {

    "return error" when {

      "day, month and year are empty" in {

        val dateInput = Date(None, None, None)
        val errors = form.fillAndValidate(dateInput).errors

        errors.length must be(3)
        errors(0) must be(FormError("day", "dateTime.date.day.empty"))
        errors(1) must be(FormError("month", "dateTime.date.month.empty"))
        errors(2) must be(FormError("year", "dateTime.date.year.empty"))
      }

      "day, month and year are incorrect" in {

        val dateInput = Date(Some(35), Some(15), Some(1900))
        val errors = form.fillAndValidate(dateInput).errors

        errors.length must be(3)
        errors(0) must be(FormError("day", "dateTime.date.day.error"))
        errors(1) must be(FormError("month", "dateTime.date.month.error"))
        errors(2) must be(FormError("year", "dateTime.date.year.error"))
      }

      "date in under the limit" in {

        val dateInput = Date(Some(0), Some(0), Some(1999))
        val errors = form.fillAndValidate(dateInput).errors

        errors.length must be(3)
        errors(0) must be(FormError("day", "dateTime.date.day.error"))
        errors(1) must be(FormError("month", "dateTime.date.month.error"))
        errors(2) must be(FormError("year", "dateTime.date.year.error"))
      }

      "date is above the limit" in {

        val dateInput = Date(Some(32), Some(13), Some(2100))
        val errors = form.fillAndValidate(dateInput).errors

        errors.length must be(3)
        errors(0) must be(FormError("day", "dateTime.date.day.error"))
        errors(1) must be(FormError("month", "dateTime.date.month.error"))
        errors(2) must be(FormError("year", "dateTime.date.year.error"))
      }

      "date has incorrect format" in {

        val dateInput = Map("day" -> "31", "month" -> "2", "year" -> "2020")
        val errors = form.bind(dateInput).errors

        errors.length must be(1)
        errors.head must be(FormError("", "dateTime.date.error.format"))
      }
    }

    "return no error for correct date" in {

      val dateInput = Date(Some(15), Some(1), Some(2020))
      val errors = form.fillAndValidate(dateInput).errors

      errors.length must be(0)
    }
  }
}
