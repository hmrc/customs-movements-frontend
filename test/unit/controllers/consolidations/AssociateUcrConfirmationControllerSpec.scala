/*
 * Copyright 2022 HM Revenue & Customs
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
import controllers.storage.{FlashExtractor, FlashKeys}
import models.ReturnToStartException
import models.cache.JourneyType
import models.cache.JourneyType.JourneyType
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.confirmation_page

class AssociateUcrConfirmationControllerSpec extends ControllerLayerSpec with ScalaFutures {

  private val flashExtractor = mock[FlashExtractor]
  private val confirmationPage = mock[confirmation_page]

  private val controller =
    new AssociateUcrConfirmationController(SuccessfulAuth(), stubMessagesControllerComponents(), flashExtractor, confirmationPage)

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(flashExtractor, confirmationPage)
    when(flashExtractor.extractMovementType(any[Request[_]])).thenReturn(None)
    when(confirmationPage.apply(any[JourneyType])(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override def afterEach(): Unit = {
    reset(flashExtractor, confirmationPage)

    super.afterEach()
  }

  "Associate DUCR Confirmation controller on displayPage" should {
    val getRequest = FakeRequest("GET", "/")

    "return 200 (OK)" in {

      when(flashExtractor.extractMovementType(any[Request[_]])).thenReturn(Some(JourneyType.ASSOCIATE_UCR))
      val request = getRequest.withFlash(FlashKeys.MOVEMENT_TYPE -> JourneyType.ASSOCIATE_UCR.toString)

      val result = controller.displayPage(request)

      status(result) must be(OK)
      verify(confirmationPage).apply(meq(JourneyType.ASSOCIATE_UCR))(any(), any())
    }

    "call FlashValuesExtractor" in {

      when(flashExtractor.extractMovementType(any[Request[_]])).thenReturn(Some(JourneyType.ASSOCIATE_UCR))
      val request = getRequest.withFlash(FlashKeys.MOVEMENT_TYPE -> JourneyType.ASSOCIATE_UCR.toString)

      controller.displayPage(request).futureValue

      val requestCaptor: ArgumentCaptor[Request[_]] = ArgumentCaptor.forClass(classOf[Request[_]])
      verify(flashExtractor).extractMovementType(requestCaptor.capture())
      requestCaptor.getValue.flash.get(FlashKeys.MOVEMENT_TYPE) mustBe Some(JourneyType.ASSOCIATE_UCR.toString)
    }

    "throw ReturnToStartException" when {

      "journey type is missing" in {

        when(flashExtractor.extractMovementType(any[Request[_]])).thenReturn(None)

        intercept[RuntimeException] {
          await(controller.displayPage(getRequest))
        } mustBe ReturnToStartException
      }

      "journey type is ARRIVAL" in {

        when(flashExtractor.extractMovementType(any[Request[_]])).thenReturn(Some(JourneyType.ARRIVE))
        val request = getRequest.withFlash(FlashKeys.MOVEMENT_TYPE -> JourneyType.ARRIVE.toString)

        intercept[RuntimeException] {
          await(controller.displayPage(request))
        } mustBe ReturnToStartException
      }

      "journey type is DEPART" in {

        when(flashExtractor.extractMovementType(any[Request[_]])).thenReturn(Some(JourneyType.DEPART))
        val request = getRequest.withFlash(FlashKeys.MOVEMENT_TYPE -> JourneyType.DEPART.toString)

        intercept[RuntimeException] {
          await(controller.displayPage(request))
        } mustBe ReturnToStartException
      }

      "journey type is DISSOCIATE_UCR" in {

        when(flashExtractor.extractMovementType(any[Request[_]])).thenReturn(Some(JourneyType.DISSOCIATE_UCR))
        val request = getRequest.withFlash(FlashKeys.MOVEMENT_TYPE -> JourneyType.DISSOCIATE_UCR.toString)

        intercept[RuntimeException] {
          await(controller.displayPage(request))
        } mustBe ReturnToStartException
      }

      "journey type is SHUT_MUCR" in {

        when(flashExtractor.extractMovementType(any[Request[_]])).thenReturn(Some(JourneyType.SHUT_MUCR))
        val request = getRequest.withFlash(FlashKeys.MOVEMENT_TYPE -> JourneyType.SHUT_MUCR.toString)

        intercept[RuntimeException] {
          await(controller.displayPage(request))
        } mustBe ReturnToStartException
      }
    }
  }
}
