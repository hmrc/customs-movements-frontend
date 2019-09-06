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

package views

import models.viewmodels.NotificationsPageSingleElement
import org.scalatest.{MustMatchers, WordSpec}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import testdata.CommonTestData
import testdata.CommonTestData.exampleNotificationPageSingleElement
import utils.Stubs
import views.base.ViewValidator
import views.html.notifications

class NotificationsViewSpec extends WordSpec with MustMatchers with Stubs with ViewValidator {

  private val messages = stubMessages()
  private def page(
    submissionUcr: String = "",
    elementsToDisplay: Seq[NotificationsPageSingleElement] = Seq.empty
  ): Html =
    new notifications(mainTemplate)(submissionUcr, elementsToDisplay)(FakeRequest(), messages)

  "Notification page" should {

    "contain title" in {

      val title = page(submissionUcr = "TEST UCR").getElementById("title")

      title must containText(messages("notifications.title", "TEST UCR"))
    }

    "contain only request element if no notifications are present" in {

      val title = "REQUEST TITLE"
      val timestamp = "TIMESTAMP"
      val content = Html("<p>CONTENT</p>")
      val pageWithData: Html =
        page(CommonTestData.correctUcr, Seq(NotificationsPageSingleElement(title, timestamp, content)))

      getElementById(pageWithData, "index-1").text() must equal("1")
      getElementById(pageWithData, "title-1").text() must equal(title)
      getElementById(pageWithData, "timestampInfo-1").text() must equal(timestamp)
      getElementById(pageWithData, "content-1").html() must equal(content.toString)
    }

    "contain elements for request and all notifications in correct order" in {

      val requestTitle = "REQUEST TITLE"
      val responseTitle_1 = "RESPONSE TITLE 1"
      val responseTitle_2 = "RESPONSE TITLE 2"
      val elementsToDisplay = Seq(
        exampleNotificationPageSingleElement(title = requestTitle),
        exampleNotificationPageSingleElement(title = responseTitle_1),
        exampleNotificationPageSingleElement(title = responseTitle_2)
      )

      val pageWithData: Html = page(CommonTestData.correctUcr, elementsToDisplay)

      getElementById(pageWithData, "title-1").text() must equal(requestTitle)
      getElementById(pageWithData, "index-1").text() must equal("1")
      getElementById(pageWithData, "title-2").text() must equal(responseTitle_1)
      getElementById(pageWithData, "index-2").text() must equal("2")
      getElementById(pageWithData, "title-3").text() must equal(responseTitle_2)
      getElementById(pageWithData, "index-3").text() must equal("3")
    }

  }
}
