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
import forms.{Choice, Ducr, MovementFormsAndIds}
import org.scalatest.BeforeAndAfter
import play.api.libs.json.{JsObject, JsString}
import play.api.test.Helpers._

class MovementControllerSpec extends CustomExportsBaseSpec with BeforeAndAfter {

  val goodsDateUri = uriWithContextPath("/goods-date")
  val locationUri = uriWithContextPath("/location")
  val transportUri = uriWithContextPath("/transport")

  before {
    authorizedUser()
  }

  "Movement Controller" when {

    "goods date" should {

      "return http code 200 with success" in {

        withCaching(Some(Choice("EAL")), Choice.choiceId)
        withCaching(None, MovementFormsAndIds.goodsDateId)

        val result = route(app, getRequest(goodsDateUri)).get

        status(result) must be(OK)
      }

      "display form" in {

        withCaching(Some(Choice("EAL")), Choice.choiceId)
        withCaching(None, MovementFormsAndIds.goodsDateId)

        val result = route(app, getRequest(goodsDateUri)).get
        val stringResult = contentAsString(result)

        stringResult must include(messages("movement.date.day"))
        stringResult must include(messages("movement.date.month"))
        stringResult must include(messages("movement.date.year"))
        stringResult must include(messages("movement.date.hour"))
        stringResult must include(messages("movement.date.minute"))
      }

      "validate form with minimum values - incorrect values" in {
        withCaching(None, MovementFormsAndIds.goodsDateId)

        val result = route(app, postRequest(goodsDateUri, wrongMinimumGoodsDate)).get
        val stringResult = contentAsString(result)

        stringResult must include(messages("movement.date.incorrectDay"))
        stringResult must include(messages("movement.date.incorrectMonth"))
        stringResult must include(messages("movement.date.incorrectYear"))
        stringResult must include(messages("movement.date.incorrectHour"))
        stringResult must include(messages("movement.date.incorrectMinutes"))
      }

      "validate form with maximum values - incorrect values" in {
        withCaching(None, MovementFormsAndIds.goodsDateId)

        val result = route(app, postRequest(goodsDateUri, wrongMaximumGoodsDate)).get
        val stringResult = contentAsString(result)

        stringResult must include(messages("movement.date.incorrectDay"))
        stringResult must include(messages("movement.date.incorrectMonth"))
        stringResult must include(messages("movement.date.incorrectHour"))
        stringResult must include(messages("movement.date.incorrectMinutes"))
      }

      "redirect to the next page" in {

        withCaching(None, MovementFormsAndIds.goodsDateId)

        val result = route(app, postRequest(goodsDateUri, goodsDate)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-movements/location"))
      }
    }

    "location" should {

      "return http code 200 with success" in {

        withCaching(Some(Choice("EAL")), Choice.choiceId)
        withCaching(None, MovementFormsAndIds.locationId)

        val result = route(app, getRequest(locationUri)).get

        status(result) must be(OK)
      }

      "display form" in {

        withCaching(Some(Choice("EAL")), Choice.choiceId)
        withCaching(None, MovementFormsAndIds.locationId)

        val result = route(app, getRequest(locationUri)).get
        val stringResult = contentAsString(result)

        stringResult must include(messages("movement.agentLocation"))
        stringResult must include(messages("movement.agentRole"))
        stringResult must include(messages("movement.goodsLocation"))
        stringResult must include(messages("movement.shed"))
      }

      "redirect to the next page with empty input data" in {

        withCaching(Some(Choice("EAL")), Choice.choiceId)
        withCaching(None, MovementFormsAndIds.locationId)

        val result = route(app, postRequest(locationUri, emptyLocation)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-movements/transport"))
      }

      "redirect to the next page with correct input data" in {

        withCaching(Some(Choice("EAL")), Choice.choiceId)
        withCaching(None, MovementFormsAndIds.locationId)

        val result = route(app, postRequest(locationUri, location)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-movements/transport"))
      }
    }

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

        val result = route(app, postRequest(transportUri, incorrectTransport)).get
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
