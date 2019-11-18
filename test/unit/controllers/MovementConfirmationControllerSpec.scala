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

package unit.controllers

import controllers.MovementConfirmationController
import controllers.storage.FlashKeys
import forms.{Choice, ConsignmentReferences}
import models.ReturnToStartException
import org.mockito.ArgumentMatchers.{any, _}
import org.mockito.Mockito.{verify, when}
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.LegacyControllerSpec
import views.html.movement_confirmation_page

import scala.concurrent.ExecutionContext.Implicits.global

class MovementConfirmationControllerSpec extends LegacyControllerSpec {

  private val page = mock[movement_confirmation_page]

  private def controller() =
    new MovementConfirmationController(mockAuthAction, stubMessagesControllerComponents(), page)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    setupErrorHandler()
    when(page.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  "GET" should {
    implicit val get = FakeRequest("GET", "/")

    "return 200 when authenticated" in {
      authorizedUser()

      val result = controller()
        .display(get.withFlash(FlashKeys.MOVEMENT_TYPE -> Choice.Arrival.value, FlashKeys.UCR_KIND -> "kind", FlashKeys.UCR -> "123"))

      status(result) mustBe Status.OK
      verify(page).apply(refEq(Choice.Arrival), refEq(ConsignmentReferences("kind", "123")))(any(), any())
    }

    "return to start" when {
      "journey type is missing" in {
        authorizedUser()
        intercept[RuntimeException] {
          await(controller().display(get.withFlash(FlashKeys.UCR_KIND -> "kind", FlashKeys.UCR -> "123")))
        } mustBe ReturnToStartException
      }

      "ucr kind is missing" in {
        authorizedUser()
        intercept[RuntimeException] {
          await(controller().display(get.withFlash(FlashKeys.MOVEMENT_TYPE -> Choice.Arrival.value, FlashKeys.UCR -> "123")))
        } mustBe ReturnToStartException
      }

      "ucr is missing" in {
        authorizedUser()
        intercept[RuntimeException] {
          await(controller().display(get.withFlash(FlashKeys.MOVEMENT_TYPE -> Choice.Arrival.value, FlashKeys.UCR_KIND -> "kind")))
        } mustBe ReturnToStartException
      }
    }
  }
}
