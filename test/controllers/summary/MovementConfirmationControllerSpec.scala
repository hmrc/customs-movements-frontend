/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.summary

import controllers.ControllerLayerSpec
import forms.ConsignmentReferences
import models.ReturnToStartException
import models.cache.JourneyType._
import models.confirmation.Confirmation
import models.requests.SessionHelper._
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.GivenWhenThen
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.summary.confirmation_page

class MovementConfirmationControllerSpec extends ControllerLayerSpec with GivenWhenThen with ScalaFutures {

  private val confirmationPage = mock[confirmation_page]

  private val conversationId = "conversationId"
  private val dummyMucr = "dummyMuCR"
  private val dummyUcr = "dummyUCR"
  private val dummyUcrType = "dummyUcrType"
  private val consignmentReferences = ConsignmentReferences(dummyUcrType, dummyUcr)

  private val controller = new MovementConfirmationController(SuccessfulAuth(), stubMessagesControllerComponents(), confirmationPage)

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(confirmationPage)
    when(confirmationPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override def afterEach(): Unit = {
    reset(confirmationPage)
    super.afterEach()
  }

  "MovementConfirmationController.displayPage" should {
    def getRequest(
      maybeJourneyType: Option[JourneyType],
      maybeConversationId: Option[String] = Some(conversationId)
    ): FakeRequest[AnyContentAsEmpty.type] = {
      val sessionValues = List(
        maybeConversationId.map(conversationId => CONVERSATION_ID -> conversationId),
        maybeJourneyType.map(journeyType => JOURNEY_TYPE -> journeyType.toString),
        Some(MUCR -> dummyMucr),
        Some(UCR -> dummyUcr),
        Some(UCR_TYPE -> dummyUcrType)
      ).flatten

      FakeRequest("GET", "/").withSession(sessionValues: _*)
    }

    "return 200 when authenticated" in {
      List(ARRIVE, DEPART, ASSOCIATE_UCR, DISSOCIATE_UCR, SHUT_MUCR).foreach { journeyType =>
        And(s"journey type is $journeyType")
        val result = controller.displayPage(getRequest(Some(journeyType)))

        status(result) mustBe Status.OK
        val confirmation = Confirmation(journeyType, conversationId, Some(consignmentReferences), Some(dummyMucr))
        verify(confirmationPage).apply(meq(confirmation))(any(), any())
      }
    }

    "throw ReturnToStartException" when {

      "journey type is missing" in {
        intercept[RuntimeException] {
          await(controller.displayPage(getRequest(None)))
        } mustBe ReturnToStartException
      }

      "conversationId is missing" in {
        intercept[RuntimeException] {
          await(controller.displayPage(getRequest(Some(ARRIVE), None)))
        } mustBe ReturnToStartException
      }
    }
  }
}
