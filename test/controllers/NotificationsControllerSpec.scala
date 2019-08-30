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

import base.MockAuthConnector
import base.MockFactory._
import base.testdata.CommonTestData.{conversationId, validEori}
import base.testdata.MovementsTestData.exampleSubmissionFrontendModel
import base.testdata.NotificationTestData.exampleNotificationFrontendModel
import connectors.CustomsDeclareExportsMovementsConnector
import models.notifications.{NotificationFrontendModel, ResponseType}
import models.submissions.{ActionType, SubmissionFrontendModel}
import models.viewmodels.{NotificationPageSingleElementFactory, NotificationsPageSingleElement}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import utils.Stubs
import views.html.notifications

import scala.concurrent.{ExecutionContext, Future}

class NotificationsControllerSpec
    extends WordSpec with MustMatchers with MockitoSugar with Stubs with MockAuthConnector with ScalaFutures {

  private trait Test {
    implicit val messages: Messages = stubMessages()
    val customsExportsMovementsConnectorMock: CustomsDeclareExportsMovementsConnector =
      buildCustomsDeclareExportsMovementsConnectorMock
    val notificationPageSingleElementFactoryMock: NotificationPageSingleElementFactory =
      buildNotificationPageSingleElementFactoryMock
    val notificationsPageMock: notifications = mock[notifications]

    val expectedSubmission = exampleSubmissionFrontendModel()
    val expectedNotifications = Seq(
      exampleNotificationFrontendModel(),
      exampleNotificationFrontendModel(responseType = ResponseType.MovementTotalsResponse)
    )
    val singleElementForSubmission = NotificationsPageSingleElement("REQUEST", "", HtmlFormat.empty)
    val singleElementForNotification = NotificationsPageSingleElement("RESPONSE", "", HtmlFormat.empty)

    when(customsExportsMovementsConnectorMock.fetchSingleSubmission(any())(any(), any()))
      .thenReturn(Future.successful(Some(expectedSubmission)))
    when(customsExportsMovementsConnectorMock.fetchNotifications(any())(any(), any()))
      .thenReturn(Future.successful(expectedNotifications))
    when(notificationPageSingleElementFactoryMock.build(any[SubmissionFrontendModel])(any()))
      .thenReturn(singleElementForSubmission)
    when(notificationPageSingleElementFactoryMock.build(any[NotificationFrontendModel])(any()))
      .thenReturn(singleElementForNotification)
    when(notificationsPageMock.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)

    val controller = new NotificationsController(
      mockAuthAction,
      customsExportsMovementsConnectorMock,
      notificationPageSingleElementFactoryMock,
      stubMessagesControllerComponents(),
      notificationsPageMock
    )(ExecutionContext.global)

    authorizedUser()
  }

  "NotificationController on listOfNotifications" when {

    "everything works correctly" should {

      "call CustomsExportsMovementsConnector.fetchSingleSubmission, passing conversation ID provided" in new Test {

        controller.listOfNotifications(conversationId)(FakeRequest()).futureValue

        verify(customsExportsMovementsConnectorMock).fetchSingleSubmission(meq(conversationId))(any(), any())
      }

      "call CustomsExportsMovementsConnector.fetchNotifications, passing conversation ID provided" in new Test {

        controller.listOfNotifications(conversationId)(FakeRequest()).futureValue

        verify(customsExportsMovementsConnectorMock).fetchNotifications(meq(conversationId))(any(), any())
      }

      "call NotificationPageSingleElementFactory, passing models returned by Connector" in new Test {

        controller.listOfNotifications(conversationId)(FakeRequest()).futureValue

        verify(notificationPageSingleElementFactoryMock).build(meq(expectedSubmission))(any())
        expectedNotifications.foreach(
          expNotification => verify(notificationPageSingleElementFactoryMock).build(meq(expNotification))(any())
        )
      }

      "call notification view template, passing UCR related to the submission" in new Test {

        controller.listOfNotifications(conversationId)(FakeRequest()).futureValue

        val expectedUcr: String = expectedSubmission.ucrBlocks.head.ucr

        verify(notificationsPageMock).apply(meq(expectedUcr), any[Seq[NotificationsPageSingleElement]])(any(), any())
      }

      "call notification view template, passing data returned by NotificationsPageSingleElementFactory" in new Test {

        val expectedViewInput: Seq[NotificationsPageSingleElement] =
          singleElementForSubmission +: expectedNotifications.map(_ => singleElementForNotification)

        controller.listOfNotifications(conversationId)(FakeRequest()).futureValue

        verify(notificationsPageMock).apply(any[String], meq(expectedViewInput))(any(), any())
      }

      "return 200 (OK)" in new Test {

        val result = controller.listOfNotifications(conversationId)(FakeRequest())

        status(result) must be(OK)
      }
    }

    "submission is missing UCR" should {

      "call notification view template, passing empty String as UCR" in new Test {

        when(customsExportsMovementsConnectorMock.fetchSingleSubmission(any())(any(), any()))
          .thenReturn(
            Future.successful(
              Some(
                SubmissionFrontendModel(
                  eori = validEori,
                  conversationId = conversationId,
                  ucrBlocks = Seq.empty,
                  actionType = ActionType.Arrival
                )
              )
            )
          )

        controller.listOfNotifications(conversationId)(FakeRequest()).futureValue

        verify(notificationsPageMock).apply(meq(""), any[Seq[NotificationsPageSingleElement]])(any(), any())
      }
    }
  }

}
