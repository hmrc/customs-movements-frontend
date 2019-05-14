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

import base.ExportsTestData._
import base.MovementBaseSpec
import forms.Choice.AllowedChoiceValues
import forms.{Choice, GoodsDeparted}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest

import scala.concurrent.Future

class SummaryControllerSpec extends MovementBaseSpec {

  private val uriSummary = uriWithContextPath("/summary")
  private val uriConfirmation = uriWithContextPath("/confirmation")

  private val emptyForm = JsObject(Map("" -> JsString("")))

  trait SetUp {
    authorizedUser()
    withCaching(Choice.choiceId, Some(Choice(AllowedChoiceValues.Arrival)))
  }

  "MovementSummaryController.displaySummary()" when {

    "cannot read data from DB" should {

      "return 500 code and display error page" in new SetUp {

        when(mockCustomsCacheService.fetch(any())(any(), any())).thenReturn(Future.successful(None))

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

    "cannot read data from DB" should {

      "return 500 code" in new SetUp {
        mockCustomsCacheServiceFetchMovementRequestResultWith(None)

        val result = route(app, postRequest(uriSummary, emptyForm)).get

        status(result) must be(INTERNAL_SERVER_ERROR)
      }

      "display error page for DB problem" in new SetUp {
        mockCustomsCacheServiceFetchMovementRequestResultWith(None)

        val result = route(app, postRequest(uriSummary, emptyForm)).get

        contentAsString(result) must include(messagesApi("global.error.heading"))
      }
    }

    "can read data from DB but submission failed" should {

      "return 500 code" in new SetUp {
        mockCustomsCacheServiceFetchMovementRequestResultWith(Some(validMovementRequest("EAL")))
        sendMovementRequest400Response()

        val result = route(app, postRequest(uriSummary, emptyForm)).get

        status(result) must be(INTERNAL_SERVER_ERROR)
        contentAsString(result) must include(messagesApi("global.error.heading"))
      }
    }

    "can read data from DB and submission succeeded" should {

      "redirect to the new page" in new SetUp {
        mockCustomsCacheServiceFetchMovementRequestResultWith(Some(validMovementRequest("EAL")))
        sendMovementRequest202Response

        val result = route(app, postRequest(uriSummary, emptyForm)).get
        val header = result.futureValue.header

        status(result) must be(SEE_OTHER)
        header.headers.get("Location") must be(Some("/customs-movements/confirmation"))
      }
    }

    "MovementSummaryController.displayConfirmation" should {

      "display confirmation page for Arrival" in new SetUp {
        mockCustomsCacheServiceFetchMovementRequestResultWith(Some(validMovementRequest("EAL")))
        mockCustomsCacheServiceClearedSuccessfully()

        val result = route(app, getRequest(uriConfirmation)).get

        contentAsString(result) must include(messagesApi("movement.choice.EAL") + " has been submitted")
      }

      "display confirmation page for Departure" in new SetUp {
        mockCustomsCacheServiceFetchMovementRequestResultWith(Some(validMovementRequest("EDL")))
        mockCustomsCacheServiceClearedSuccessfully()

        val result = route(app, getRequest(uriConfirmation)).get

        contentAsString(result) must include(messagesApi("movement.choice.EDL") + " has been submitted")
      }
    }
  }

  private def mockCustomsCacheServiceFetchMovementRequestResultWith(
    desiredResult: Option[InventoryLinkingMovementRequest]
  ) =
    when(mockCustomsCacheService.fetchMovementRequest(any(), any())(any(), any()))
      .thenReturn(Future.successful(desiredResult))

  private def mockCustomsCacheServiceClearedSuccessfully() =
    when(mockCustomsCacheService.remove(any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(ACCEPTED)))

  private def mockBackOrOutTheUKSuccessful() =
    when(
      mockCustomsCacheService
        .fetchAndGetEntry[GoodsDeparted](any(), ArgumentMatchers.eq(GoodsDeparted.formId))(any(), any(), any())
    ).thenReturn(Future.successful(Some(GoodsDeparted(GoodsDeparted.AllowedPlaces.backIntoTheUk))))

  private def mockBackOrOutTheUKFailed() =
    when(
      mockCustomsCacheService
        .fetchAndGetEntry[GoodsDeparted](any(), ArgumentMatchers.eq(GoodsDeparted.formId))(any(), any(), any())
    ).thenReturn(Future.successful(None))

}
