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

import java.time.LocalDate

import base.BaseSpec
import forms.common.{Date, Time}

class ArrivalDetailsSpec extends BaseSpec {

  private val date = LocalDate.of(2019, 1, 1)

  private val timeInputData = Time.mapping.withPrefix("timeOfArrival").unbind(Time(Some("1"), Some("1")))

  private val dateInputData = Date.mapping.withPrefix("dateOfArrival").unbind(Date(date))

  "Arrival mapping" should {

    "return errors" when {

      "date is missing" in {

        val errors = MovementDetails.arrivalForm().bind(timeInputData).errors

        errors.length must be(3)
      }

      "time is missing" in {

        val errors = MovementDetails.arrivalForm().bind(dateInputData).errors

        errors.length must be(2)
      }
    }

    "return no errors" when {

      "date and time are provided" in {
        val inputData = timeInputData ++ dateInputData
        val errors = MovementDetails.arrivalForm().bind(inputData).errors

        errors.length must be(0)
      }
    }
  }
}
