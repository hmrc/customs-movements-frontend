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

import controllers.StartController
import forms.Choice
import forms.Choice.Arrival
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.LegacyControllerSpec
import views.html.start_page

import scala.concurrent.ExecutionContext.global

class StartControllerSpec extends LegacyControllerSpec {

  private val startPage = mock[start_page]

  private val controller = new StartController(stubMessagesControllerComponents(), startPage)(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    withCaching(Choice.choiceId, Some(Arrival))
    when(startPage.apply()(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(startPage)

    super.afterEach()
  }

  "Start Controller" should {

    "return 200 (OK)" when {

      "display page method is invoked" in {

        val result = controller.displayStartPage()(getRequest())

        status(result) mustBe OK

        verify(startPage).apply()(any(), any())
      }
    }
  }
}
