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

import base.CustomExportsBaseSpec
import base.ExportsTestData._
import forms.{Choice, MovementFormsAndIds}
import org.scalatest.BeforeAndAfter
import play.api.libs.json.{JsObject, JsString}
import play.api.test.Helpers._

class MovementControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {

  val locationUri = uriWithContextPath("/location")
  val transportUri = uriWithContextPath("/transport")

  before {
    authorizedUser()
  }

  "Movement Controller" when {

    "transport" should {

      "return http code 200 with success" in {
        withCaching(Some(Choice("EAL")), Choice.choiceId)
        withCaching(None, MovementFormsAndIds.transportId)

        val result = route(app, getRequest(transportUri)).get

        status(result) must be(OK)
      }

      "display form" in {
        withCaching(Some(Choice("EAL")), Choice.choiceId)
        withCaching(None, MovementFormsAndIds.transportId)

        val result = route(app, getRequest(transportUri)).get
        val stringResult = contentAsString(result)

        stringResult must include(messages("movement.transport.id"))
        stringResult must include(messages("movement.transport.mode"))
        stringResult must include(messages("movement.transport.nationality"))
      }

      "validate input data - incorrect input data" in {
        withCaching(Some(Choice("EAL")), Choice.choiceId)
        withCaching(None, MovementFormsAndIds.transportId)

        val result =
          route(app, postRequest(transportUri, incorrectTransport)).get
        val stringResult = contentAsString(result)

        stringResult must include("Maximum length is 1")
        stringResult must include("Maximum length is 2")
      }

      "redirect to the next page with empty input data" in {
        withCaching(Some(Choice("EAL")), Choice.choiceId)
        withCaching(None, MovementFormsAndIds.transportId)

        val result = route(app, postRequest(transportUri, JsObject(Map("" -> JsString(""))))).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-movements/summary"))
      }

      "redirect to the next page with correct input data" in {
        withCaching(Some(Choice("EAL")), Choice.choiceId)
        withCaching(None, MovementFormsAndIds.transportId)

        val result = route(app, postRequest(transportUri, correctTransport)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-movements/summary"))
      }
    }
  }
}
