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

import base.{MovementBaseSpec, ViewValidator}
import forms.Choice
import forms.Choice.AllowedChoiceValues
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.test.Helpers._

class MucrOptionsControllerSpec extends MovementBaseSpec with ViewValidator with BeforeAndAfterEach {

  private val uri = uriWithContextPath("/mucr-options")

  override def beforeEach() {
    authorizedUser()
    withCaching(Choice.choiceId, Some(Choice(AllowedChoiceValues.AssociateDUCR)))
  }

  override def afterEach(): Unit = {
    super.afterEach()
    reset(mockSubmissionService, mockCustomsCacheService)
  }

  "MUCR Options" should {

    "return Ok for GET request" in {
      val result = route(app, getRequest(uri)).get
      status(result) must be(OK)
    }

    "display an error for a missing MUCR" in {
      val invalidMUCR = Json.obj()
      val Some(result) = route(app, postRequest(uri, invalidMUCR))

      status(result) must be(BAD_REQUEST)
      val page = contentAsString(result)
      checkErrorsSummary(page)
      verifyFieldError(page, "newMucr", "Please enter a value")
      verifyFieldError(page, "existingMucr", "Please enter a value")
    }

    "display an error for an empty MUCR" in {
      val invalidMUCR = JsObject(Map("newMucr" -> JsString("")))
      val Some(result) = route(app, postRequest(uri, invalidMUCR))

      status(result) must be(BAD_REQUEST)
      val page = contentAsString(result)
      checkErrorsSummary(page)
      verifyFieldError(page, "newMucr", "Please enter a value")
    }

    "display an error for invalid MUCR" in {
      val invalidMUCR = JsObject(Map("mucrReference" -> JsString("INVALID-MUCR")))
      val Some(result) = route(app, postRequest(uri, invalidMUCR))
      
      status(result) must be(BAD_REQUEST)
      val page = contentAsString(result)
      checkErrorsSummary(page)
      verifyFieldError(page, "existingMucr", "Please enter a valid reference")
    }

    "Redirect to next page for a valid Add to MUCR" in {
      val validMUCR =
        JsObject(Map("existingMucr" -> JsString("8GB12345612345612345")))
      val Some(result) = route(app, postRequest(uri, validMUCR))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) mustBe Some(routes.AssociateDucrController.displayPage().url)
    }

    "Redirect to next page for a valid Create new MUCR" in {
      val validMUCR =
        JsObject(Map("newMucr" -> JsString("8GB12345612345612345")))
      val Some(result) = route(app, postRequest(uri, validMUCR))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) mustBe Some(routes.AssociateDucrController.displayPage().url)

    }
  }
}
