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

import controllers.storage.FlashKeys
import models.ReturnToStartException
import models.cache.JourneyType
import models.cache.JourneyType.JourneyType
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{reset, verify, when}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.controllers.ControllerLayerSpec
import views.html.confirmation_page

class AssociateUcrConfirmationControllerSpec extends ControllerLayerSpec {

  private val confirmationPage = mock[confirmation_page]

  private val controller =
    new AssociateUcrConfirmationController(SuccessfulAuth(), stubMessagesControllerComponents(), confirmationPage)

  override def beforeEach() {
    super.beforeEach()

    reset(confirmationPage)
    when(confirmationPage.apply(any[JourneyType])(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override def afterEach(): Unit = {
    reset(confirmationPage)

    super.afterEach()
  }

  "Associate DUCR Confirmation controller" should {
    val getRequest = FakeRequest("GET", "/")

    "return 200 (OK)" when {

      "display page method is invoked" in {
        val request = getRequest.withFlash(FlashKeys.MOVEMENT_TYPE -> JourneyType.ASSOCIATE_UCR.toString)

        val result = controller.displayPage()(request)

        status(result) must be(OK)
        verify(confirmationPage).apply(meq(JourneyType.ASSOCIATE_UCR))(any(), any())
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

      "journey type is DISSOCIATE_UCR" in {
        val request = getRequest.withFlash(FlashKeys.MOVEMENT_TYPE -> JourneyType.DISSOCIATE_UCR.toString)

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
