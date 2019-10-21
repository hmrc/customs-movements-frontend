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

import forms.Choice.{Arrival, ChoiceValueFormat, Departure}
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.{JsError, JsNumber, JsObject, JsString, JsSuccess, JsValue}

class ChoiceSpec extends WordSpec with MustMatchers with OptionValues {
  import ChoiceSpec._

  "Validation defined in Choice mapping" should {

    "attach errors to form" when {
      "provided with empty input" in {
        val form = Choice.form().bind(emptyChoiceJSON)

        form.hasErrors mustBe true
        form.errors.length must equal(1)
        form.errors.head.message must equal("choicePage.input.error.empty")
      }

      "provided with an incorrect value" in {
        val form = Choice.form().bind(incorrectChoiceJSON)

        form.hasErrors mustBe true
        form.errors.length must equal(1)
        form.errors.head.message must equal("choicePage.input.error.incorrectValue")
      }
    }

    "not attach any error" when {
      "provided with valid input" in {
        val form = Choice.form().bind(correctChoiceJSON)

        form.hasErrors mustBe false
      }
    }
  }

  "Choice model for apply and unapply" should {

    "have correctly prepared unapply method" in {

      Choice.unapply(Arrival).value mustBe "arrival"
    }

    "correctly map input to choice" in {

      Choice.apply("arrival") mustBe Arrival
    }

    "throw an exception during apply method when choice is incorrect" in {

      val exception = intercept[IllegalArgumentException] {
        Choice.apply("incorrect")
      }

      exception.getMessage mustBe "Incorrect choice"
    }
  }

  "ChoiceValueFormat" should {

    "return JsSuccess" when {

      "the choice is JsString and has correct value" in {

        ChoiceValueFormat.reads(JsString("arrival")) mustBe JsSuccess(Arrival)
      }
    }

    "return JsError" when {

      "the choice is JsString, but has incorrect value" in {

        ChoiceValueFormat.reads(JsString("incorrect")) mustBe JsError("Incorrect choice")
      }

      "the choice is different than JsString" in {

        ChoiceValueFormat.reads(JsNumber(10)) mustBe JsError("Incorrect choice")
      }
    }

    "correctly write object as JsValue" in {

      ChoiceValueFormat.writes(Arrival) mustBe JsString("arrival")
    }
  }

  "Choice" should {

    "return correct information about itself" in {

      Arrival.isArrival mustBe true
      Arrival.isDeparture mustBe false
      Departure.isArrival mustBe false
      Departure.isDeparture mustBe true
    }
  }
}

object ChoiceSpec {

  val correctChoiceJSON: JsValue = createChoiceJSON(Arrival.value)
  val incorrectChoiceJSON: JsValue = createChoiceJSON("InvalidChoice")
  val emptyChoiceJSON: JsValue = createChoiceJSON()

  def createChoiceJSON(choiceValue: String = ""): JsValue =
    JsObject(Map("choice" -> JsString(choiceValue)))
}
