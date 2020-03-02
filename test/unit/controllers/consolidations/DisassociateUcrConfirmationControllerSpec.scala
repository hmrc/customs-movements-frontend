/*
 * Copyright 2020 HM Revenue & Customs
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

import controllers.ControllerLayerSpec
import controllers.storage.FlashKeys
import models.ReturnToStartException
import models.cache.JourneyType
import models.cache.JourneyType.JourneyType
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{reset, verify, when}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.confirmation_page

import scala.concurrent.ExecutionContext.global

class DisassociateUcrConfirmationControllerSpec extends ControllerLayerSpec {

  private val confirmationPage = mock[confirmation_page]

  private val controller =
    new DisassociateUcrConfirmationController(SuccessfulAuth(), stubMessagesControllerComponents(), confirmationPage)(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(confirmationPage)
    when(confirmationPage.apply(any[JourneyType])(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(confirmationPage)

    super.afterEach()
  }

  "Disassociate Ducr Confirmation Controller" should {
    val getRequest = FakeRequest("GET", "/")

    "return 200 (OK)" when {

      "display page method is invoked" in {
        val result = controller.displayPage()(getRequest.withFlash(FlashKeys.MOVEMENT_TYPE -> JourneyType.DISSOCIATE_UCR.toString))

        status(result) mustBe OK
        verify(confirmationPage).apply(meq(JourneyType.DISSOCIATE_UCR))(any(), any())
      }
    }

    "throw ReturnToStartException" when {

      "journey type is missing" in {
        intercept[RuntimeException] {
          await(controller.displayPage()(getRequest))
        } mustBe ReturnToStartException
      }

      "journey type is ARRIVAL" in {
        val request = getRequest.withFlash(FlashKeys.MOVEMENT_TYPE -> JourneyType.ARRIVE.toString)

        intercept[RuntimeException] {
          await(controller.displayPage()(request))
        } mustBe ReturnToStartException
      }

      "journey type is DEPART" in {
        val request = getRequest.withFlash(FlashKeys.MOVEMENT_TYPE -> JourneyType.DEPART.toString)

        intercept[RuntimeException] {
          await(controller.displayPage()(request))
        } mustBe ReturnToStartException
      }

      "journey type is ASSOCIATE_UCR" in {
        val request = getRequest.withFlash(FlashKeys.MOVEMENT_TYPE -> JourneyType.ASSOCIATE_UCR.toString)

        intercept[RuntimeException] {
          await(controller.displayPage()(request))
        } mustBe ReturnToStartException
      }

      "journey type is SHUT_MUCR" in {
        val request = getRequest.withFlash(FlashKeys.MOVEMENT_TYPE -> JourneyType.SHUT_MUCR.toString)

        intercept[RuntimeException] {
          await(controller.displayPage()(request))
        } mustBe ReturnToStartException
      }
    }
  }
}
