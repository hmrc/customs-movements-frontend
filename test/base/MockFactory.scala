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

package base

import connectors.CustomsDeclareExportsMovementsConnector
import models.notifications.NotificationFrontendModel
import models.submissions.SubmissionFrontendModel
import models.viewmodels.notificationspage.{NotificationPageSingleElementFactory, NotificationsPageSingleElement}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers.NO_CONTENT
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.HttpResponse
import testdata.ConsolidationTestData.exampleShutMucrRequest

import scala.concurrent.Future

object MockFactory extends MockitoSugar {

  def buildCustomsDeclareExportsMovementsConnectorMock: CustomsDeclareExportsMovementsConnector = {
    val customsDeclareExportsMovementsConnector = mock[CustomsDeclareExportsMovementsConnector]

    when(customsDeclareExportsMovementsConnector.sendArrivalDeclaration(any())(any()))
      .thenReturn(Future.successful(HttpResponse(NO_CONTENT)))
    when(customsDeclareExportsMovementsConnector.sendDepartureDeclaration(any())(any()))
      .thenReturn(Future.successful(HttpResponse(NO_CONTENT)))
    when(customsDeclareExportsMovementsConnector.sendConsolidationRequest(any())(any()))
      .thenReturn(Future.successful(exampleShutMucrRequest))

    when(customsDeclareExportsMovementsConnector.fetchNotifications(any())(any()))
      .thenReturn(Future.successful(Seq.empty))
    when(customsDeclareExportsMovementsConnector.fetchAllSubmissions()(any()))
      .thenReturn(Future.successful(Seq.empty))
    when(customsDeclareExportsMovementsConnector.fetchSingleSubmission(any())(any()))
      .thenReturn(Future.successful(None))

    customsDeclareExportsMovementsConnector
  }

  def buildNotificationPageSingleElementFactoryMock = {
    val notificationPageSingleElementFactoryMock = mock[NotificationPageSingleElementFactory]

    when(notificationPageSingleElementFactoryMock.build(any[SubmissionFrontendModel])(any()))
      .thenReturn(NotificationsPageSingleElement("", "", HtmlFormat.empty))
    when(notificationPageSingleElementFactoryMock.build(any[NotificationFrontendModel])(any()))
      .thenReturn(NotificationsPageSingleElement("", "", HtmlFormat.empty))

    notificationPageSingleElementFactoryMock
  }
}
