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

import base.FormBaseSpec
import forms.GoodsDeparted.AllowedPlaces
import play.api.data.FormError

class GoodsDepartedSpec extends FormBaseSpec {

  "Goods Departed model" should {

    "has correct formId value" in {

      GoodsDeparted.formId must be("GoodsDeparted")
    }

    "has correct values for allowed places" in {

      GoodsDeparted.AllowedPlaces.outOfTheUk must be("OutOfTheUk")
      GoodsDeparted.AllowedPlaces.backIntoTheUk must be("BackIntoTheUk")
    }

    "has list contained allowed places" in {

      GoodsDeparted.allowedPlaces.length must be(2)
      GoodsDeparted.allowedPlaces must contain(AllowedPlaces.outOfTheUk)
      GoodsDeparted.allowedPlaces must contain(AllowedPlaces.backIntoTheUk)
    }
  }

  "Goods Departed mapping" should {

    "return error" when {

      "departed place is empty" in {

        val inputData = GoodsDeparted("")
        val errors = GoodsDeparted.form().fillAndValidate(inputData).errors

        errors.length must be(1)
        errors.head must be(FormError("departedPlace", "goodsDeparted.departedPlace.empty"))
      }

      "departed place is incorrect" in {

        val inputData = GoodsDeparted("incorrect")
        val errors = GoodsDeparted.form().fillAndValidate(inputData).errors

        errors.length must be(1)
        errors.head must be(FormError("departedPlace", "goodsDeparted.departedPlace.error"))
      }
    }

    "return no error" when {

      "value is correct" in {

        val inputData = GoodsDeparted(AllowedPlaces.outOfTheUk)
        val errors = GoodsDeparted.form().fillAndValidate(inputData).errors

        errors.length must be(0)
      }
    }
  }
}
