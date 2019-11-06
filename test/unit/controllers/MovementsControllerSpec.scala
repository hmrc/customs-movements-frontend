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

import java.time.Instant

import base.MockCustomsExportsMovement
import controllers.MovementsController
import models.notifications.NotificationFrontendModel
import models.submissions.SubmissionFrontendModel
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, anyString, eq => meq}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import testdata.CommonTestData._
import testdata.MovementsTestData
import testdata.MovementsTestData.exampleSubmissionFrontendModel
import testdata.NotificationTestData.exampleNotificationFrontendModel
import unit.base.ControllerSpec
import views.html.movements

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class MovementsControllerSpec extends ControllerSpec with MockCustomsExportsMovement with ScalaFutures {

  private val mockMovementsPage = mock[movements]

  private val controller =
    new MovementsController(mockAuthAction, mockCustomsExportsMovementConnector, stubMessagesControllerComponents(), mockMovementsPage)(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    when(mockMovementsPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockMovementsPage)

    super.afterEach()
  }

  "Submissions Controller on displayPage" should {

    "return 200 (OK)" in {

      when(mockCustomsExportsMovementConnector.fetchAllSubmissions(any[String])(any())).thenReturn(Future.successful(Seq.empty))
      when(mockCustomsExportsMovementConnector.fetchAllNotificationsForUser(any[String])(any())).thenReturn(Future.successful(Seq.empty))

      val result = controller.displayPage()(getRequest())

      status(result) mustBe OK
    }

    "call connector for all Submissions" in {

      when(mockCustomsExportsMovementConnector.fetchAllSubmissions(any[String])(any())).thenReturn(Future.successful(Seq.empty))
      when(mockCustomsExportsMovementConnector.fetchAllNotificationsForUser(any[String])(any())).thenReturn(Future.successful(Seq.empty))

      controller.displayPage()(getRequest()).futureValue

      val expectedEori = validEori
      verify(mockCustomsExportsMovementConnector).fetchAllSubmissions(meq(expectedEori))(any())
    }

    "call connector for all Notifications" in {

      val submission = MovementsTestData.exampleSubmissionFrontendModel()
      when(mockCustomsExportsMovementConnector.fetchAllSubmissions(any[String])(any())).thenReturn(Future.successful(Seq(submission)))
      when(mockCustomsExportsMovementConnector.fetchAllNotificationsForUser(any[String])(any())).thenReturn(Future.successful(Seq.empty))

      controller.displayPage()(getRequest()).futureValue

      val expectedEori = validEori
      verify(mockCustomsExportsMovementConnector).fetchAllNotificationsForUser(meq(expectedEori))(any())
    }

    "call submissions view, passing Submissions in descending order" when {

      "there are no Notifications for the Submissions" in {

        val submission1 = exampleSubmissionFrontendModel(requestTimestamp = Instant.now().minusSeconds(60))
        val submission2 = exampleSubmissionFrontendModel(requestTimestamp = Instant.now().minusSeconds(30))
        val submission3 = exampleSubmissionFrontendModel(requestTimestamp = Instant.now())

        when(mockCustomsExportsMovementConnector.fetchAllSubmissions(any[String])(any()))
          .thenReturn(Future.successful(Seq(submission1, submission2, submission3)))
        when(mockCustomsExportsMovementConnector.fetchAllNotificationsForUser(any[String])(any())).thenReturn(Future.successful(Seq.empty))

        controller.displayPage()(getRequest()).futureValue

        val viewArguments: Seq[(SubmissionFrontendModel, Seq[NotificationFrontendModel])] = captureViewArguments()

        val submissions: Seq[SubmissionFrontendModel] = viewArguments.map(_._1)
        submissions mustBe Seq(submission3, submission2, submission1)
      }

      "there are Notifications for the Submissions" in {

        val submission1 = exampleSubmissionFrontendModel(conversationId = conversationId, requestTimestamp = Instant.now().minusSeconds(60))
        val submission2 = exampleSubmissionFrontendModel(conversationId = conversationId_2, requestTimestamp = Instant.now().minusSeconds(30))
        val submission3 = exampleSubmissionFrontendModel(conversationId = conversationId_3, requestTimestamp = Instant.now())

        val notification1 = exampleNotificationFrontendModel(conversationId = conversationId)
        val notification2 = exampleNotificationFrontendModel(conversationId = conversationId_2)
        val notification3 = exampleNotificationFrontendModel(conversationId = conversationId_3)
        val notification4 = exampleNotificationFrontendModel(conversationId = conversationId_3)

        when(mockCustomsExportsMovementConnector.fetchAllSubmissions(any[String])(any()))
          .thenReturn(Future.successful(Seq(submission1, submission2, submission3)))
        when(mockCustomsExportsMovementConnector.fetchAllNotificationsForUser(any[String])(any()))
          .thenReturn(Future.successful(Seq(notification1, notification2, notification3, notification4)))

        controller.displayPage()(getRequest()).futureValue

        val viewArguments: Seq[(SubmissionFrontendModel, Seq[NotificationFrontendModel])] = captureViewArguments()

        val submissions: Seq[SubmissionFrontendModel] = viewArguments.map(_._1)
        val notifications: Seq[Seq[NotificationFrontendModel]] = viewArguments.map(_._2)
        submissions mustBe Seq(submission3, submission2, submission1)
        notifications mustBe Seq(Seq(notification4, notification3), Seq(notification2), Seq(notification1))
      }
    }

    "return 200 (OK)" when {

      "display page is invoked with submissions in descending order" in {

        val submission1 = exampleSubmissionFrontendModel(requestTimestamp = Instant.now().minusSeconds(60))
        val submission2 = exampleSubmissionFrontendModel(requestTimestamp = Instant.now().minusSeconds(30))
        val submission3 = exampleSubmissionFrontendModel(requestTimestamp = Instant.now())

        when(mockCustomsExportsMovementConnector.fetchAllSubmissions(anyString())(any()))
          .thenReturn(Future.successful(Seq(submission1, submission2, submission3)))
        when(mockCustomsExportsMovementConnector.fetchAllNotificationsForUser(anyString())(any()))
          .thenReturn(Future.successful(Seq(exampleNotificationFrontendModel())))

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK

        val captor: ArgumentCaptor[Seq[(SubmissionFrontendModel, Seq[NotificationFrontendModel])]] =
          ArgumentCaptor.forClass(classOf[Seq[(SubmissionFrontendModel, Seq[NotificationFrontendModel])]])
        verify(mockMovementsPage).apply(captor.capture())(any(), any())

        val submissions: Seq[SubmissionFrontendModel] = captor.getValue.map(value => value._1)

        submissions must be(Seq(submission3, submission2, submission1))
      }
    }
  }

  private def captureViewArguments(): Seq[(SubmissionFrontendModel, Seq[NotificationFrontendModel])] = {
    val captor: ArgumentCaptor[Seq[(SubmissionFrontendModel, Seq[NotificationFrontendModel])]] =
      ArgumentCaptor.forClass(classOf[Seq[(SubmissionFrontendModel, Seq[NotificationFrontendModel])]])
    verify(mockMovementsPage).apply(captor.capture())(any(), any())
    captor.getValue
  }

}
