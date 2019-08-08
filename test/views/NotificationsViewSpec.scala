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

import base.ViewValidator
import org.scalatest.{MustMatchers, WordSpec}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import utils.Stubs
import views.html.notifications

class NotificationsViewSpec extends WordSpec with MustMatchers with Stubs with ViewValidator {

  val messages = stubMessages()
  val page: Html = new notifications(mainTemplate)(Seq.empty)(FakeRequest(), messages)

  "Notification page" should {

    "contains title" in {

      page.getElementById("title") must containText(messages("notifications.title"))
    }

    "contains correct table headers" in {

      page.getElementById("reference") must containText(messages("notifications.reference"))
      page.getElementById("dateOfResponse") must containText(messages("notifications.dateOfResponse"))
      page.getElementById("roe") must containText(messages("notifications.roe"))
      page.getElementById("soe") must containText(messages("notifications.soe"))
    }
  }
}
