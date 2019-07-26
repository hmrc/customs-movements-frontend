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

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import base.ViewValidator
import models.{Notification, Submission}
import org.scalatest.{MustMatchers, WordSpec}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import utils.Stubs
import views.html.submissions

class SubmissionsViewSpec extends WordSpec with MustMatchers with Stubs with ViewValidator {

  val messages = stubMessages()
  val page: Html = new submissions(mainTemplate)(Seq.empty)(FakeRequest(), minimalAppConfig, messages)

  "Submission page" should {

    "contains title" in {

      page.getElementById("title") must containText(messages("submissions.title"))
    }

    "contains correct table headers" in {

      page.getElementById("ucr") must containText(messages("submissions.ucr"))
      page.getElementById("submissionType") must containText(messages("submissions.submissionType"))
      page.getElementById("submissionAction") must containText(messages("submissions.submissionAction"))
      page.getElementById("dateUpdated") must containText(messages("submissions.dateUpdated"))
      page.getElementById("status") must containText(messages("submissions.status"))
      page.getElementById("noOfNotifications") must containText(messages("submissions.noOfNotifications"))
    }

    "contains correct submission data" in {
      val dateTime = LocalDate.parse("2019-10-31", DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay()
      val pageWithData: Html = new submissions(mainTemplate)(
        Seq(
          (
            Submission(
              conversationId = "conversationId",
              ucr = "4444",
              submissionType = "M",
              submissionAction = "Consolidate",
              dateUpdated = dateTime,
              status = Some("Cleared")
            ),
            Seq(Notification(dateTimeReceived = dateTime, conversationId = "conversationId"))
          )
        )
      )(FakeRequest(), minimalAppConfig, messages)

      getElementById(pageWithData, "ucr-conversationId").text() must be("4444")
      getElementById(pageWithData, "submissionType-conversationId").text() must be("M")
      getElementById(pageWithData, "submissionAction-conversationId").text() must be("Consolidate")
      getElementById(pageWithData, "dateUpdated-conversationId").text() must be("2019-10-31 00:00")
      getElementById(pageWithData, "status-conversationId").text() must be("Cleared")
      getElementById(pageWithData, "noOfNotifications-conversationId").text() must be("1")
    }
  }
}
