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
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{reset, verify, when}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import testdata.MovementsTestData.exampleSubmissionFrontendModel
import testdata.NotificationTestData.exampleNotificationFrontendModel
import unit.base.ControllerSpec
import views.html.movements

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class MovementsControllerSpec extends ControllerSpec with MockCustomsExportsMovement {

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

  "Submissions Controller" should {

    "return 200 (OK)" when {

      "display page is invoked with submissions in descending order" in {

        val submission1 = exampleSubmissionFrontendModel(requestTimestamp = Instant.now().minusSeconds(60))
        val submission2 = exampleSubmissionFrontendModel(requestTimestamp = Instant.now().minusSeconds(30))
        val submission3 = exampleSubmissionFrontendModel(requestTimestamp = Instant.now())

        when(mockCustomsExportsMovementConnector.fetchAllSubmissions()(any()))
          .thenReturn(Future.successful(Seq(submission1, submission2, submission3)))
        when(mockCustomsExportsMovementConnector.fetchNotifications(anyString())(any(), any()))
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
}
