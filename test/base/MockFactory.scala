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

package base

import models.notifications.Notification
import models.submissions.Submission
import models.viewmodels.notificationspage.{NotificationPageSingleElementFactory, NotificationsPageSingleElement}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, when}
import play.twirl.api.HtmlFormat

object MockFactory {

  private lazy val notificationsPageElement = NotificationsPageSingleElement("", "", HtmlFormat.empty)

  def buildNotificationPageSingleElementFactoryMock: NotificationPageSingleElementFactory = {
    val notificationPageSingleElementFactoryMock = mock[NotificationPageSingleElementFactory]

    when(notificationPageSingleElementFactoryMock.build(any[Submission], any[Int])(any())).thenReturn(notificationsPageElement)
    when(notificationPageSingleElementFactoryMock.build(any[Notification], any[Submission])(any())).thenReturn(notificationsPageElement)

    notificationPageSingleElementFactoryMock
  }
}
