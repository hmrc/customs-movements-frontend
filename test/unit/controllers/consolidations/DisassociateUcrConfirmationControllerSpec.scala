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

import controllers.consolidations.DisassociateUcrConfirmationController
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.controllers.ControllerLayerSpec
import views.html.disassociate_ucr_confirmation

import scala.concurrent.ExecutionContext.global

class DisassociateUcrConfirmationControllerSpec extends ControllerLayerSpec {

  private val mockDisassociateDucrConfirmationPage = mock[disassociate_ucr_confirmation]

  private val controller =
    new DisassociateUcrConfirmationController(SuccessfulAuth(), stubMessagesControllerComponents(), mockDisassociateDucrConfirmationPage)(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(mockDisassociateDucrConfirmationPage.apply()(any(), any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockDisassociateDucrConfirmationPage)
    super.afterEach()
  }

  "Disassociate Ducr Confirmation Controller" should {

    "return 200 (OK)" when {

      "display page method is invoked" in {

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        verify(mockDisassociateDucrConfirmationPage).apply()(any(), any(), any())
      }
    }
  }
}
