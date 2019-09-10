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

import controllers.UnauthorisedController
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.unauthorised

import scala.concurrent.ExecutionContext.global

class UnauthorisedControllerSpec extends ControllerSpec {

  val mockUnauthorisedPage = mock[unauthorised]
  val controller = new UnauthorisedController(stubMessagesControllerComponents(), mockUnauthorisedPage)(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(mockUnauthorisedPage.apply()(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockUnauthorisedPage)

    super.afterEach()
  }

  "Unauthorised Controller" must {

    "return 200 for a GET" in {

      val result = controller.onPageLoad()(getRequest())

      status(result) mustBe OK
      verify(mockUnauthorisedPage, times(1)).apply()(any(), any())
    }
  }
}