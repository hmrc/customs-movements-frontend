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
import forms.Choice
import forms.Choice._
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify}
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.{JsObject, JsString}
import play.api.test.Helpers._

class ChoiceControllerSpec extends MovementBaseSpec with BeforeAndAfterEach {

  private val choiceUri = uriWithContextPath("/choice")

  override def beforeEach {
    authorizedUser()
    withCaching[Choice](Choice.choiceId, None)
  }

  override def afterEach {
    reset(mockCustomsCacheService)
  }

  "Choice Controller on GET" should {

    "return 200 status code" in {
      val Some(result) = route(app, getRequest(choiceUri))
      status(result) must be(OK)
    }

    "read item from cache and display it" in {

      val cachedData = Choice("EAL")
      withCaching[Choice](Choice.choiceId, Some(cachedData))

      val Some(result) = route(app, getRequest(choiceUri))
      status(result) must be(OK)

      val stringResult = contentAsString(result)
      stringResult must include("""value="EAL" checked="checked"""")
    }
  }

  "ChoiceController on POST" should {

    "display the choice page with error" when {

      "no value provided for choice" in {

        val emptyForm = JsObject(Map("" -> JsString("")))
        val result = route(app, postRequest(choiceUri, emptyForm)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("choicePage.input.error.empty"))
      }

      "wrong value provided for choice" in {

        val wrongForm = JsObject(Map("choice" -> JsString("test")))
        val result = route(app, postRequest(choiceUri, wrongForm)).get

        status(result) must be(BAD_REQUEST)
        contentAsString(result) must include(messages("choicePage.input.error.incorrectValue"))
      }
    }

    "save the choice data to the cache" in {

      withCaching(Choice.choiceId)

      val validChoiceForm = JsObject(Map("choice" -> JsString("EDL")))
      route(app, postRequest(choiceUri, validChoiceForm)).get.futureValue

      verify(mockCustomsCacheService)
        .cache[Choice](any(), ArgumentMatchers.eq(Choice.choiceId), any())(any(), any(), any())
    }

    "redirect to arrival page when 'Arrival' is selected" in {

      withCaching(Choice.choiceId)

      val correctForm = JsObject(Map("choice" -> JsString(AllowedChoiceValues.Arrival)))
      val Some(result) = route(app, postRequest(choiceUri, correctForm))

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(routes.ConsignmentReferencesController.displayPage().url))
    }

    "redirect to departure page when 'Departure' is selected" in {

      withCaching(Choice.choiceId)

      val correctForm = JsObject(Map("choice" -> JsString(AllowedChoiceValues.Departure)))
      val Some(result) = route(app, postRequest(choiceUri, correctForm))

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(routes.ConsignmentReferencesController.displayPage().url))
    }

    "redirect to associate page when 'Associate' is selected" in {
      withCaching(Choice.choiceId)

      val associateChoice = JsObject(Map("choice" -> JsString(AllowedChoiceValues.AssociateDUCR)))
      val Some(result) = route(app, postRequest(choiceUri, associateChoice))

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(routes.AssociateDucrController.displayPage().url))
    }

    "redirect to disassociate page when 'Disassociate' is selected" in {

      withCaching(Choice.choiceId)

      val correctForm = JsObject(Map("choice" -> JsString(AllowedChoiceValues.DisassociateDUCR)))
      val result = route(app, postRequest(choiceUri, correctForm)).get

      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(routes.DisassociateDucrController.displayPage().url))
    }

  }
}
