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

package controllers.consolidations

import base.{MovementBaseSpec, ViewValidator}
import controllers.exception.IncompleteApplication
import forms.Choice.AllowedChoiceValues
import forms.{AssociateDucr, Choice, MucrOptions}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.test.Helpers._

class AssociateDucrControllerSpec extends MovementBaseSpec with ViewValidator with BeforeAndAfterEach {

  private val uri = uriWithContextPath("/associate-ducr")

  override def beforeEach() {
    authorizedUser()
    withCaching(Choice.choiceId, Some(Choice(AllowedChoiceValues.AssociateDUCR)))
    withCaching(MucrOptions.formId, Some(MucrOptions("MUCR")))
    withCaching(AssociateDucr.formId)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    reset(mockSubmissionService, mockCustomsCacheService)
  }

  "Associate DUCR GET" should {

    "throw incomplete application when cache empty" in {
      withCaching(MucrOptions.formId, None)
      assertThrows[IncompleteApplication] {
        await(route(app, getRequest(uri)).get)
      }
    }

    "return Ok for GET request" in {
      val result = route(app, getRequest(uri)).get
      status(result) must be(OK)
    }
  }

  "Associate DUCR POST" should {

    "throw incomplete application when cache empty" in {
      withCaching(MucrOptions.formId, None)
      assertThrows[IncompleteApplication] {
        await(route(app, postRequest(uri, Json.obj())).get)
      }
    }

    "display an error for a missing DUCR" in {
      val Some(result) = route(app, postRequest(uri, Json.obj()))

      status(result) must be(BAD_REQUEST)
      val page = htmlBodyOf(result)
      page must haveGlobalErrorSummary
      page must haveFieldError("ducr", "Please enter a value")
    }

    "display an error for a empty DUCR" in {
      val Some(result) = route(app, postRequest(uri, Json.obj("ducr" -> "")))

      status(result) must be(BAD_REQUEST)
      val page = htmlBodyOf(result)
      page must haveGlobalErrorSummary
      page must haveFieldError("ducr", "Please enter a reference")
    }

    "display an error for an invalid DUCR" in {
      val Some(result) = route(app, postRequest(uri, Json.obj("ducr" -> "invalid")))

      status(result) must be(BAD_REQUEST)
      val page = htmlBodyOf(result)
      page must haveGlobalErrorSummary
      page must haveFieldError("ducr", "Please enter a valid reference")
    }

    "Redirect to next page for a valid form" in {
      val validMUCR =
        JsObject(Map("ducr" -> JsString("8GB12345612345612345")))
      val Some(result) = route(app, postRequest(uri, validMUCR))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) mustBe Some(routes.AssociateDucrSummaryController.displayPage().url)

      theFormIDCached mustBe AssociateDucr.formId
      theDataCached mustBe AssociateDucr("8GB12345612345612345")
    }
  }
}
