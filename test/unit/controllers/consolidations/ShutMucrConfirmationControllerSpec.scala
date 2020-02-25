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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.controllers.ControllerLayerSpec
import views.html.shut_mucr_confirmation

import scala.concurrent.ExecutionContext.global

class ShutMucrConfirmationControllerSpec extends ControllerLayerSpec {

  private val mockShutMucrConfirmationPage = mock[shut_mucr_confirmation]

  private val controller =
    new ShutMucrConfirmationController(SuccessfulAuth(), stubMessagesControllerComponents(), mockShutMucrConfirmationPage)(global)

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(mockShutMucrConfirmationPage.apply()(any(), any())).thenReturn(HtmlFormat.empty)
  }

  "Shut Mucr Confirmation Controller" should {

    "return 200 (OK)" when {

      "display page is invoked" in {

        val result = controller.displayPage()(FakeRequest("GET", "/"))

        status(result) mustBe OK
        verify(mockShutMucrConfirmationPage).apply()(any(), any())
      }
    }
  }
}
