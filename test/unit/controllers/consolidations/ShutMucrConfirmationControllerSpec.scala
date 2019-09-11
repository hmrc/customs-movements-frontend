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

package unit.controllers.consolidations

import controllers.consolidations.ShutMucrConfirmationController
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.shut_mucr_confirmation

import scala.concurrent.ExecutionContext.global

class ShutMucrConfirmationControllerSpec extends ControllerSpec {

  val mockShutMucrConfirmationPage = mock[shut_mucr_confirmation]

  val controller = new ShutMucrConfirmationController(
    mockAuthAction,
    stubMessagesControllerComponents(),
    mockShutMucrConfirmationPage
  )(global)

  override def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(mockShutMucrConfirmationPage.apply()(any(), any(), any())).thenReturn(HtmlFormat.empty)
  }

  "ShutMucr Confirmation Controller on GET" should {

    "return Ok code" in {

      val result = controller.displayPage()(getRequest())

      status(result) mustBe OK
    }
  }
}
