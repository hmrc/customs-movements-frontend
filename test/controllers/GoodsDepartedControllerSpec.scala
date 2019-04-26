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

package controllers

import base.MovementBaseSpec
import forms.{Choice, GoodsDeparted}
import forms.GoodsDeparted.AllowedPlaces._
import forms.Choice.AllowedChoiceValues
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class GoodsDepartedControllerSpec extends MovementBaseSpec {

  val uri = uriWithContextPath("/goods-departed")

  trait SetUp {
    authorizedUser()
  }

  trait DepartureSetUp extends SetUp {
    withCaching(Choice.choiceId, Some(Choice(AllowedChoiceValues.Departure)))
  }

  trait ArrivalSetUp extends SetUp {
    withCaching(Choice.choiceId, Some(Choice(AllowedChoiceValues.Arrival)))
  }

  "Goods Departed Controller" should {

    "return 200 for get request" when {

      "cache is empty" in new DepartureSetUp {

        withCaching(GoodsDeparted.formId, None)

        val result = route(app, getRequest(uri)).get

        status(result) must be(OK)
      }

      "cache contains data" in new DepartureSetUp {
        withCaching(GoodsDeparted.formId, Some(GoodsDeparted(outOfTheUk)))

        val result = route(app, getRequest(uri)).get

        status(result) must be(OK)
      }
    }

    "return BadRequest" when {

      "user is during arrival journey" in new ArrivalSetUp {

        val result = route(app, getRequest(uri)).get

        status(result) must be(BAD_REQUEST)
      }

      "form is incorrect" in new DepartureSetUp {

        withCaching(GoodsDeparted.formId)

        val incorrectForm: JsValue = JsObject(Map("departedPlace" -> JsString("123456")))

        val result = route(app, postRequest(uri, incorrectForm)).get

        status(result) must be(BAD_REQUEST)
      }
    }

    "redirect to date of departure page for correct form" in new DepartureSetUp {

      withCaching(GoodsDeparted.formId)

      val correctForm: JsValue = JsObject(Map("departedPlace" -> JsString(outOfTheUk)))

      val result = route(app, postRequest(uri, correctForm)).get
      val headers = result.futureValue.header.headers

      status(result) must be(SEE_OTHER)
      headers.get("Location") must be(Some("/customs-movements/movement-details"))
    }
  }
}
