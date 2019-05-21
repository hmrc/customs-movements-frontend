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
import forms.{Choice, GoodsDeparted}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfter
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class SummaryControllerSpec extends MovementBaseSpec with BeforeAndAfter {

  before {
    reset(mockCustomsCacheService)
    reset(mockSubmissionService)
  }
  private val uriSummary = uriWithContextPath("/summary")

  private val emptyForm = JsObject(Map("" -> JsString("")))

  trait SetUp {
    authorizedUser()
    withCaching(Choice.choiceId, Some(Choice(AllowedChoiceValues.Arrival)))

  }

  "MovementSummaryController.displaySummary()" when {

    "cannot read data from DB" should {

      "return 500 code and display error page" in new SetUp {
        when(mockCustomsCacheService.fetch(any())(any(), any()))
          .thenReturn(Future.successful(None))
        val result = route(app, getRequest(uriSummary)).get

        status(result) must be(INTERNAL_SERVER_ERROR)
        contentAsString(result) must include(messagesApi("global.error.heading"))
      }
    }

    "can read data from DB" should {

      "return 200 code" in new SetUp {
        when(mockCustomsCacheService.fetch(any())(any(), any()))
          .thenReturn(Future.successful(Some(CacheMap("id", Map.empty[String, JsValue]))))

        val result = route(app, getRequest(uriSummary)).get

        status(result) must be(OK)
      }
    }
  }

  "MovementSummaryController.submitMovementRequest" when {

    "Submission of data failed" should {

      "return 500 code" in new SetUp {
        mockSubmission(INTERNAL_SERVER_ERROR)

        val result = route(app, postRequest(uriSummary, emptyForm)).get

        status(result) must be(INTERNAL_SERVER_ERROR)
      }
    }

    "Submission succeeded" should {

      "redirect to the new page" in new SetUp {
        mockSubmission()
        mockCustomsCacheServiceClearedSuccessfully

        val result = route(app, postRequest(uriSummary, emptyForm)).get
        val header = result.futureValue.header

        status(result) must be(OK)
        contentAsString(result) must include(messagesApi("movement.choice.EAL") + " has been submitted")
      }
    }
  }

}
