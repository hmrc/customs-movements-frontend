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
import forms.Choice.AllowedChoiceValues
import forms.{Choice, ConsignmentReferences}
import org.scalatest.OptionValues
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class ConsignmentReferencesControllerSpec extends MovementBaseSpec with OptionValues {

  val uri = uriWithContextPath("/consignment-references")

  trait SetUp {
    authorizedUser()
  }

  trait ArrivalSetUp extends SetUp {
    withCaching(Choice.choiceId, Some(Choice(AllowedChoiceValues.Arrival)))
  }

  trait DepartureSetUp extends SetUp {
    withCaching(Choice.choiceId, Some(Choice(AllowedChoiceValues.Departure)))
  }

  "Consignment Reference Controller" should {

    "return 200 for get request" when {

      "cache is empty" in new ArrivalSetUp {

        withCaching(ConsignmentReferences.formId, None)

        val result = route(app, getRequest(uri)).get

        status(result) must be(OK)
      }

      "cache contains data" in new ArrivalSetUp {

        withCaching(ConsignmentReferences.formId, Some(ConsignmentReferences("D", "123456")))

        val result = route(app, getRequest(uri)).get

        status(result) must be(OK)
      }
    }

    "return BadRequest for incorrect form" in new ArrivalSetUp {

      val incorrectForm: JsValue = JsObject(
        Map(
          "eori" -> JsString("GB717572504502811"),
          "reference" -> JsString("reference"),
          "referenceValue" -> JsString("")
        )
      )

      val result = route(app, postRequest(uri, incorrectForm)).get

      status(result) must be(BAD_REQUEST)
    }

    "redirect to goods date for correct form in arrival journey" in new ArrivalSetUp {

      withCaching(ConsignmentReferences.formId)

      val correctForm: JsValue =
        JsObject(
          Map(
            "eori" -> JsString("GB717572504502811"),
            "reference" -> JsString("D"),
            "referenceValue" -> JsString("5GB123456789000-123ABC456DEFIIIII")
          )
        )

      val result = route(app, postRequest(uri, correctForm)).get
      val headers = result.futureValue.header.headers

      status(result) must be(SEE_OTHER)
      redirectLocation(result).value mustBe "/customs-movements/arrival-reference"
    }

    "redirect to location for correct form in departure journey" in new DepartureSetUp {

      withCaching(ConsignmentReferences.formId)

      val correctForm: JsValue =
        JsObject(
          Map(
            "eori" -> JsString("GB717572504502811"),
            "reference" -> JsString("D"),
            "referenceValue" -> JsString("5GB123456789000-123ABC456DEFIIIII")
          )
        )

      val result = route(app, postRequest(uri, correctForm)).get
      val headers = result.futureValue.header.headers

      status(result) must be(SEE_OTHER)
      redirectLocation(result).value mustBe "/customs-movements/location"
    }
  }
}
