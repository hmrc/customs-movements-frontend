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
import forms.common.{Date, Time}

class ArrivalDetailsSpec extends BaseSpec {

  "Arrival mapping" should {

    "return errors" when {

      "date is missing" in {

        val inputData = ArrivalDetails(Date(None, None, None), None)
        val errors = MovementDetails.arrivalForm().fillAndValidate(inputData).errors

        errors.length must be(3)
      }
    }

    "return no errors" when {

      "only mandatory field is filled" in {

        val inputData = ArrivalDetails(Date(Some(1), Some(1), Some(2019)), None)
        val errors = MovementDetails.arrivalForm().fillAndValidate(inputData).errors

        errors.length must be(0)
      }

      "date and time are provided" in {

        val inputData = ArrivalDetails(Date(Some(1), Some(1), Some(2019)), Some(Time(Some("1"), Some("1"))))
        val errors = MovementDetails.arrivalForm().fillAndValidate(inputData).errors

        errors.length must be(0)
      }
    }
  }
}
