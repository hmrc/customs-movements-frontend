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

package unit.models.requests

import models.requests.MovementType
import models.requests.MovementType._
import play.api.libs.json.{JsError, JsNumber, JsString, JsSuccess}
import unit.base.UnitSpec

class MovementTypeSpec extends UnitSpec {

  private val arrivalCode = "EAL"
  private val departureCode = "EDL"

  "Movement Type" should {

    "contains all values" in {

      MovementType.allValues mustBe Seq(Arrival, Departure)
    }

    "create correct movement type" when {

      "arrival code is an input" in {

        val movementType = MovementType(arrivalCode)

        movementType mustBe Arrival
        movementType.value mustBe arrivalCode
      }

      "departure code is an input" in {

        val movementType = MovementType(departureCode)

        movementType mustBe Departure
        movementType.value mustBe departureCode
      }
    }

    "throw an exception when there is incorrect movement value" in {
      intercept[IllegalArgumentException]{
        MovementType("incorrect")
      }
    }

    "methods toString should return code" in {

      Arrival.toString mustBe arrivalCode
      Departure.toString mustBe departureCode
    }

    "correctly write value to JsValue" in {

      MovementTypeFormat.writes(Arrival) mustBe JsString(arrivalCode)
    }

    "correctly read JsValue" in {

      MovementTypeFormat.reads(JsString(arrivalCode)) mustBe JsSuccess(Arrival)
    }

    "return JsError" when {
      val jsError = JsError("Incorrect movement type")

      "JsString contains incorrect value" in {

        MovementTypeFormat.reads(JsString("incorrect")) mustBe jsError
      }

      "there is a different type of JsValue" in {

        MovementTypeFormat.reads(JsNumber(1)) mustBe jsError
      }
    }
  }
}
