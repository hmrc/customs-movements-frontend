/*
 * Copyright 2022 HM Revenue & Customs
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
import views.html.notifications

import scala.jdk.CollectionConverters.ListHasAsScala

class NotificationsViewSpec extends ViewSpec with Injector {

  private implicit val request = FakeRequest()

  private val page = instanceOf[notifications]

  "Notification page" should {

    val pageWithoutNotifications = page(submissionUcr = CommonTestData.validMucr, elementsToDisplay = Seq.empty)

    "contain title" in {
      pageWithoutNotifications.getTitle must containText(messages("notifications.title", CommonTestData.correctUcr))
    }

    "contain header" in {
      val text = messages("notifications.title", CommonTestData.correctUcr)
      pageWithoutNotifications.getElementById("title") must containText(text)
    }

    "contains the right UCR label" when {
      "UCR is a MUCR" in {
        val ucr = CommonTestData.validMucr
        val pageWithMUCR = page(submissionUcr = ucr, elementsToDisplay = Seq.empty)

        val text = messages("notifications.mucr", ucr)

        pageWithMUCR.getElementsByClass("notifications-ucr").first() must containText(text)
      }

      "UCR is a DUCR" in {
        val ucr = CommonTestData.validDucr
        val pageWithMUCR = page(submissionUcr = ucr, elementsToDisplay = Seq.empty)

        val text = messages("notifications.ducr", ucr)

        pageWithMUCR.getElementsByClass("notifications-ucr").first() must containText(text)
      }
    }

    "contain elements for all notifications in correct order" in {
      def notificationsPageElement(index: Int): NotificationsPageSingleElement =
        NotificationsPageSingleElement(title = s"title $index", timestampInfo = s"timestamp $index", content = Html(s"<p>CONTENT $index</p>"))

      val elementsToDisplay = (0 to 2).map(notificationsPageElement)

      val pageWithData = page(submissionUcr = CommonTestData.correctUcr, elementsToDisplay = elementsToDisplay)

      for ((element, index) <- pageWithData.getElementsByClass("hmrc-timeline__event").asScala.toList.zipWithIndex) {
        element.tagName mustBe "li"
        val children = element.children.asScala.toList
        children.size mustBe 3
        children(0).tagName mustBe "h2"
        children(0).text mustBe s"title $index"

        children(1).tagName mustBe "time"
        children(1).text mustBe s"timestamp $index"

        children(2).tagName mustBe "div"
        children(2).text mustBe s"CONTENT $index"
      }
    }

    "display a 'Print' button" in {
      pageWithoutNotifications.getElementsByClass("gem-c-print-link__button").size() mustBe 1
    }
  }
}
