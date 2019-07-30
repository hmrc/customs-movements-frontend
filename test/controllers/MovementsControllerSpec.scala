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

package controllers
import base.{MockCustomsExportsMovement, MovementBaseSpec}
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.movements

import scala.concurrent.{ExecutionContext, Future}

class MovementsControllerSpec extends MovementBaseSpec with MockCustomsExportsMovement {

  trait SetUp {
    val movementsPage = new movements(mainTemplate)

    val controller = new MovementsController(
      mockAuthAction,
      mockCustomsExportsMovementConnector,
      stubMessagesControllerComponents(),
      movementsPage
    )(minimalAppConfig, ExecutionContext.global)

    authorizedUser()
  }

  "Submissions Controller" should {

    "return 200 for get request" when {

      "cache is empty" in new SetUp {

        when(mockCustomsExportsMovementConnector.fetchSubmissions()(any(), any()))
          .thenReturn(Future.successful(Seq.empty))
        when(mockCustomsExportsMovementConnector.fetchNotifications(anyString())(any(), any()))
          .thenReturn(Future.successful(Seq.empty))

        val result = controller.displayPage()(FakeRequest())

        status(result) must be(OK)
      }
    }
  }
}
