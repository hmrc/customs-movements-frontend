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
import forms.{Choice, DisassociateDucr}
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._

class DisassociateDucrControllerSpec extends MovementBaseSpec {

  private val uri = uriWithContextPath("/disassociate-ducr")

  trait SetUp {
    authorizedUser()
    withCaching(Choice.choiceId, Some(Choice(AllowedChoiceValues.DisassociateDUCR)))
  }

  "Disassociate Ducr Controller" should {

    "return 200 for get request" when {

      "cache is empty" in new SetUp {

        withCaching(DisassociateDucr.formId, None)

        val result = route(app, getRequest(uri)).get

        status(result) must be(OK)
      }

      "cache contains data" in new SetUp {

        withCaching(DisassociateDucr.formId, Some(DisassociateDucr("8GB12345612345612345")))

        val result = route(app, getRequest(uri)).get

        status(result) must be(OK)
      }
    }

    "return BadRequest for incorrect form" in new SetUp {

      val incorrectForm: JsValue = JsObject(
        Map(
          "ducr" -> JsString("abc")
        )
      )

      val result = route(app, postRequest(uri, incorrectForm)).get

      status(result) must be(BAD_REQUEST)
    }

    "redirect to confirmation for correct form" in new SetUp {

      withCaching(DisassociateDucr.formId)

      val correctForm: JsValue =
        JsObject(
          Map(
            "ducr" -> JsString("8GB12345612345612345")
          )
        )

      val result = route(app, postRequest(uri, correctForm)).get

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(routes.DisassociateDucrConfirmationController.displayPage().url))
    }
  }
}
