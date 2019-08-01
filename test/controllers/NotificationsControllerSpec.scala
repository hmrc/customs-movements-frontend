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

import base.{MockAuthConnector, MockCustomsExportsMovement}
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.Stubs
import views.html.notifications

import scala.concurrent.{ExecutionContext, Future}

class NotificationsControllerSpec
    extends WordSpec with MustMatchers with MockitoSugar with Stubs with MockAuthConnector
    with MockCustomsExportsMovement {

  trait SetUp {
    val notificationsPage = new notifications(mainTemplate)

    val controller = new NotificationsController(
      mockAuthAction,
      mockCustomsExportsMovementConnector,
      stubMessagesControllerComponents(),
      notificationsPage
    )(ExecutionContext.global)

    authorizedUser()
  }

  "Notification controller" should {

    "return 200 (OK)" when {

      "method listOfNotifications will be invoked with conversationId" in new SetUp {
        when(mockCustomsExportsMovementConnector.fetchNotifications(anyString())(any(), any()))
          .thenReturn(Future.successful(Seq.empty))

        val result = controller.listOfNotifications("convId")(FakeRequest())

        status(result) must be(OK)
      }
    }
  }
}
