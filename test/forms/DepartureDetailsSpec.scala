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

import base.BaseSpec
import forms.common.Date


class DepartureDetailsSpec extends BaseSpec {

  "Departure mapping" should {

    "format the date correctly" when{

      "date is in ISO 8601 format " in {
        val inputData = DepartureDetails(Date(Some(1), Some(1), Some(2019)))
        inputData.toString must be("2019-01-01T00:00")
      }
    }

    "return errors" when {

      "date is missing" in {

        val inputData = DepartureDetails(Date(None, None, None))
        val errors = MovementDetails.departureForm().fillAndValidate(inputData).errors

        errors.length must be(3)
      }
    }

    "return no errors" when {

      "date is correct" in {

        val inputData = DepartureDetails(Date(Some(1), Some(1), Some(2019)))
        val errors = MovementDetails.departureForm().fillAndValidate(inputData).errors

        errors.length must be(0)
      }
    }
  }
}
