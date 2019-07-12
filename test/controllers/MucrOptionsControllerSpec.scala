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
import forms.Choice.AllowedChoiceValues
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.{JsObject, JsString}
import play.api.test.Helpers._

class MucrOptionsControllerSpec extends MovementBaseSpec with BeforeAndAfterEach {

  private val uri = uriWithContextPath("/mucr-options")

  override def beforeEach() {
    authorizedUser()
    withCaching(Choice.choiceId, Some(Choice(AllowedChoiceValues.AssociateDUCR)))
  }

  override def afterEach(): Unit = {
    super.afterEach()
    reset(mockSubmissionService, mockCustomsCacheService)
  }

  "Associate DUCR" should {

    "return Ok for GET request" in {
      val result = route(app, getRequest(uri)).get
      status(result) must be(OK)
    }

    
    "return BadRequest for empty MUCR" in {
      val invalidMUCR = JsObject(Map("mucrOptions.mucrReference" -> JsString("")))
      val Some(result) = route(app, postRequest(uri, invalidMUCR))
      status(result) must be(BAD_REQUEST)
    }
    
    "return BadRequest for invalid MUCR" in {
      val invalidMUCR = JsObject(Map("mucrOptions.mucrReference" -> JsString("INVALID-MUCR")))
      val Some(result) = route(app, postRequest(uri, invalidMUCR))
      status(result) must be(BAD_REQUEST)
    }

    "Redirect to next page for a valid MUCR" in {
      val validMUCR = JsObject(Map("mucrOptions.mucrReference" -> JsString("8GB12345612345612345")))
      val Some(result) = route(app, postRequest(uri, validMUCR))
      status(result) must be(SEE_OTHER)
    }
  }
}
