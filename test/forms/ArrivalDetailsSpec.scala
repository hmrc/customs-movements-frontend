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

import java.time.{LocalDate, LocalTime, ZoneId}

import base.BaseSpec
import forms.common.{Date, Time}
import play.api.data.Mapping

class ArrivalDetailsSpec extends BaseSpec {

  private val date = LocalDate.now().minusDays(1)

  private val timeMapping = Time.mapping.withPrefix("timeOfArrival")

  private val timeInputData = timeMapping.unbind(Time(LocalTime.of(1, 1)))

  private val dateMapping: Mapping[Date] = Date.mapping.withPrefix("dateOfArrival")

  private val dateInputData = dateMapping.unbind(Date(date))

  val movementDetails = new MovementDetails(ZoneId.of("UTC"))

  "Arrival mapping" should {

    "return errors" when {

      "date is missing" in {

        val errors = movementDetails.arrivalForm().bind(timeInputData).errors

        errors.length must be(3)
      }

      "time is missing" in {

        val errors = movementDetails.arrivalForm().bind(dateInputData).errors

        errors.length must be(2)
      }

      "moment of arrival is in future" in {
        val form = movementDetails.arrivalForm().bind(dateMapping.unbind(Date(LocalDate.now().plusDays(1))) ++ timeInputData)

        form.errors.flatMap(_.messages) must contain("arrival.details.error.future")
      }

      "moment of arrival is more then 60 days in past" in {
        val form = movementDetails.arrivalForm().bind(dateMapping.unbind(Date(LocalDate.now().minusDays(61))) ++ timeInputData)

        form.errors.flatMap(_.messages) must contain("arrival.details.error.overdue")
      }
    }

    "return no errors" when {

      "date and time are provided" in {
        val inputData = timeInputData ++ dateInputData
        val errors = movementDetails.arrivalForm().bind(inputData).errors

        errors.length must be(0)
      }
    }
  }
}
