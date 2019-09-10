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

import base.MockCustomsExportsMovement
import controllers.MovementsController
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{reset, when}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.movements

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class MovementsControllerSpec extends ControllerSpec with MockCustomsExportsMovement {

  val mockMovementsPage = mock[movements]

  val controller = new MovementsController(
    mockAuthAction,
    mockCustomsExportsMovementConnector,
    stubMessagesControllerComponents(),
    mockMovementsPage
  )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(mockMovementsPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit =
    reset(mockMovementsPage)

  "Submissions Controller" should {

    "return 200 for get request" when {

      "cache is empty" in {

        when(mockCustomsExportsMovementConnector.fetchAllSubmissions()(any(), any()))
          .thenReturn(Future.successful(Seq.empty))
        when(mockCustomsExportsMovementConnector.fetchNotifications(anyString())(any(), any()))
          .thenReturn(Future.successful(Seq.empty))

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
      }
    }
  }
}
