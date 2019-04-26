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
import forms.{Choice, Transport}
import forms.Transport.ModesOfTransport.Sea
import forms.Choice.AllowedChoiceValues
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class TransportControllerSpec extends MovementBaseSpec {

  val uri = uriWithContextPath("/transport")

  trait SetUp {
    authorizedUser()
    withCaching(Choice.choiceId, Some(Choice(AllowedChoiceValues.Arrival)))
  }

  "Transport Controller" should {

    "return 200 for get request" when {

      "cache is empty" in new SetUp {

        withCaching(Transport.formId, None)

        val result = route(app, getRequest(uri)).get

        status(result) must be(OK)
      }

      "cache contains data" in new SetUp {

        withCaching(Transport.formId, Some(Transport(Sea, "PL")))

        val result = route(app, getRequest(uri)).get

        status(result) must be(OK)
      }
    }

    "return BadRequest for incorrect form" in {

      withCaching(Transport.formId)

      val incorrectForm: JsValue = JsObject(
        Map("modeOfTransport" -> JsString("transport"), "nationality" -> JsString("Country"))
      )

      val result = route(app, postRequest(uri, incorrectForm)).get

      status(result) must be(BAD_REQUEST)
    }

    "redirect to summary page for correct form" in {

      withCaching(Transport.formId)

      val incorrectForm: JsValue = JsObject(
        Map("modeOfTransport" -> JsString(Sea), "nationality" -> JsString("PL"))
      )

      val result = route(app, postRequest(uri, incorrectForm)).get
      val headers = result.futureValue.header.headers

      status(result) must be(SEE_OTHER)
      headers.get("Location") must be(Some("/customs-movements/summary"))
    }
  }
}
