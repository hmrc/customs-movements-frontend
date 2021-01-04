/*
 * Copyright 2021 HM Revenue & Customs
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

package views

import base.Injector
import models.viewmodels.notificationspage.NotificationsPageSingleElement
import play.api.test.FakeRequest
import play.twirl.api.Html
import testdata.CommonTestData
import testdata.CommonTestData.exampleNotificationPageSingleElement
import views.html.notifications

class NotificationsViewSpec extends ViewSpec with Injector {

  private implicit val request = FakeRequest()

  private val page = instanceOf[notifications]

  "Notification page" should {

    val title = "REQUEST TITLE"
    val timestamp = "TIMESTAMP"
    val content = Html("<span>CONTENT</span>")
    val pageWithoutNotifications = page(
      submissionUcr = CommonTestData.correctUcr,
      submissionElement = NotificationsPageSingleElement(title, timestamp, content),
      elementsToDisplay = Seq.empty
    )

    "contain title" in {
      pageWithoutNotifications.getTitle must containText(messages("notifications.title", CommonTestData.correctUcr))
    }

    "contain header" in {
      pageWithoutNotifications.getElementById("title") must containText(messages("notifications.title", CommonTestData.correctUcr))
    }

    "contain only request element if no notifications are present" in {

      val pageWithData: Html = page(
        submissionUcr = CommonTestData.correctUcr,
        submissionElement = NotificationsPageSingleElement(title, timestamp, content),
        elementsToDisplay = Seq.empty
      )

      pageWithData.getElementById("notifications-request-title") must containText(title)
      pageWithData.getElementById("notifications-request-timestamp") must containText(timestamp)
      pageWithData.getElementById("notifications-request-content") must containHtml(content.toString)
    }

    "contain elements for request and all notifications in correct order" in {

      val requestTitle = "REQUEST TITLE"
      val responseTitle_1 = "RESPONSE TITLE 1"
      val responseTitle_2 = "RESPONSE TITLE 2"
      val elementsToDisplay =
        Seq(exampleNotificationPageSingleElement(title = responseTitle_1), exampleNotificationPageSingleElement(title = responseTitle_2))

      val pageWithData: Html = page(
        submissionUcr = CommonTestData.correctUcr,
        submissionElement = exampleNotificationPageSingleElement(title = requestTitle),
        elementsToDisplay = elementsToDisplay
      )

      pageWithData.getElementById("notifications-request-title") must containText(requestTitle)
      pageWithData.getElementById("title-1") must containText(responseTitle_1)
      pageWithData.getElementById("index-1") must containText("1")
      pageWithData.getElementById("title-2") must containText(responseTitle_2)
      pageWithData.getElementById("index-2") must containText("2")
    }

  }
}
