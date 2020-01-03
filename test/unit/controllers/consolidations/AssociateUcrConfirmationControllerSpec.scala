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

package unit.controllers.consolidations

import controllers.consolidations.AssociateUcrConfirmationController
import controllers.storage.FlashKeys
import models.ReturnToStartException
import models.cache.JourneyType
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.controllers.ControllerLayerSpec
import views.html.associate_ucr_confirmation

class AssociateUcrConfirmationControllerSpec extends ControllerLayerSpec {

  private val page = mock[associate_ucr_confirmation]

  private val controller =
    new AssociateUcrConfirmationController(SuccessfulAuth(), stubMessagesControllerComponents(), page)

  override def beforeEach() {
    super.beforeEach()
    when(page.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override def afterEach(): Unit = {
    reset(page)

    super.afterEach()
  }

  "Associate DUCR Confirmation controller" should {
    implicit val get = FakeRequest("GET", "/")

    "return 200 (OK)" when {

      "display page method is invoked" in {
        val result = controller.displayPage()(get.withFlash(FlashKeys.CONSOLIDATION_KIND -> "kind", FlashKeys.UCR -> "123"))

        status(result) must be(OK)
        verify(page).apply(any(), any())(any(), any())
      }
    }

    "return to start" when {
      "ucr kind is missing" in {
        intercept[RuntimeException] {
          await(controller.displayPage()(get.withFlash(FlashKeys.UCR -> "123")))
        } mustBe ReturnToStartException
      }

      "ucr is missing" in {
        intercept[RuntimeException] {
          await(controller.displayPage()(get.withFlash(FlashKeys.CONSOLIDATION_KIND -> "kind")))
        } mustBe ReturnToStartException
      }
    }
  }
}
