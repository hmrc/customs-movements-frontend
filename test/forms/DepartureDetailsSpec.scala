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

import java.time.{LocalDate, LocalTime}

import base.BaseSpec
import forms.common.{Date, Time}
import helpers.FormMatchers
import testdata.MovementsTestData

class DepartureDetailsSpec extends BaseSpec with FormMatchers {

  val movementDetails = MovementsTestData.movementDetails

  "Departure mapping" should {

    "format the date correctly" when {

      "date is in ISO 8601 format " in {
        val inputData = DepartureDetails(Date(LocalDate.of(2019, 1, 1)), Time(LocalTime.of(0, 0)))
        inputData.toString must be("2019-01-01T00:00:00")
      }

    }

    "return errors" when {

      "there is no data" in {
        val inputData = Map.empty[String, String]
        val errors = movementDetails.departureForm().bind(inputData).errors
        errors must have length 5
      }

      "time is missing" in {
        val inputData = Date.mapping.withPrefix("dateOfDeparture").unbind(Date(LocalDate.now()))
        val errors = movementDetails.departureForm().bind(inputData).errors
        errors must have length 2
      }

      "date is missing" in {
        val inputData = Time.mapping.withPrefix("timeOfDeparture").unbind(Time(LocalTime.now()))
        val errors = movementDetails.departureForm().bind(inputData).errors
        errors must have length 3
      }
    }

    "return no errors" when {

      "date is correct" in {
        val inputData = Date.mapping.withPrefix("dateOfDeparture").unbind(Date(LocalDate.now().minusDays(1))) ++
          Time.mapping.withPrefix("timeOfDeparture").unbind(Time(LocalTime.now()))
        val form = movementDetails.departureForm().bind(inputData)
        form mustBe withoutErrors
      }
    }
  }
}
