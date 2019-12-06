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

import java.time.LocalDate

import base.BaseSpec
import play.api.data.{Form, FormError}

class DateSpec extends BaseSpec {

  val form: Form[Date] = Form(Date.mapping)

  "Date" should {

    "correct convert date to 102 format" in {

      val dateInput = Date(LocalDate.of(2019, 1, 1))

      dateInput.to102Format must be("20190101")
    }

    "return string in uuuu-MM-dd format for toString method" in {

      val dateInput = Date(LocalDate.of(2020, 3, 3))

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

      "date has incorrect format" in {

        val dateInput = Map("day" -> "31", "month" -> "2", "year" -> "2020")
        val errors = form.bind(dateInput).errors

        errors.length must be(1)
        errors.head must be(FormError("", "date.error.invalid"))
      }
    }

    "return no error for correct date" in {

      val dateInput = Map("day" -> "15", "month" -> "1", "year" -> "2020")
      val errors = form.bind(dateInput).errors

      errors.length must be(0)
    }
  }
}
