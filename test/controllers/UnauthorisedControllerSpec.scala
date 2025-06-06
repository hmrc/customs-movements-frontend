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

package controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.unauthorised

class UnauthorisedControllerSpec extends ControllerLayerSpec {

  private val mockUnauthorisedPage = mock[unauthorised]
  private val controller =
    new UnauthorisedController(stubMessagesControllerComponents(), mockUnauthorisedPage)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(mockUnauthorisedPage.apply()(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockUnauthorisedPage)
    super.afterEach()
  }

  "Unauthorised Controller" must {
    "return 200 (OK)" when {
      "on page load method is invoked" in {
        val result = controller.onPageLoad()(getRequest())

        status(result) mustBe OK
        verify(mockUnauthorisedPage).apply()(any(), any())
      }
    }
  }
}
