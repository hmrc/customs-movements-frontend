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

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.MINUTES
import java.time.{Instant, LocalDate, ZoneId, ZonedDateTime}

import base.ViewValidator
import base.testdata.CommonTestData.conversationId
import models.{NotificationPresentation, SubmissionPresentation, UcrBlock}
import org.scalatest.{MustMatchers, WordSpec}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import utils.Stubs
import views.html.movements

class MovementsViewSpec extends WordSpec with MustMatchers with Stubs with ViewValidator {

  val messages = stubMessages()
  val page: Html = new movements(mainTemplate)(Seq.empty)(FakeRequest(), messages)

  "Movements page" should {

    "contains title" in {

      page.getElementById("title") must containText(messages("submissions.title"))
    }

    "contains correct table headers" in {

      page.getElementById("ucr") must containText(messages("submissions.ucr"))
      page.getElementById("ucrType") must containText(messages("submissions.submissionType"))
      page.getElementById("submissionAction") must containText(messages("submissions.submissionAction"))
      page.getElementById("dateOfRequest") must containText(messages("submissions.dateOfRequest"))
      page.getElementById("dateOfUpdate") must containText(messages("submissions.dateOfUpdate"))
      page.getElementById("noOfNotifications") must containText(messages("submissions.noOfNotifications"))
    }

    "contains correct submission data" in {
      val dateTime: Instant = ZonedDateTime
        .of(
          LocalDate.parse("2019-10-31", DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay(),
          ZoneId.systemDefault()
        )
        .toInstant
      val pageWithData: Html = new movements(mainTemplate)(
        Seq(
          (
            SubmissionPresentation(
              requestTimestamp = dateTime,
              eori = "",
              conversationId = conversationId,
              ucrBlocks = Seq(UcrBlock(ucr = "4444", ucrType = "M")),
              actionType = "Consolidate"
            ),
            Seq(
              NotificationPresentation(
                timestampReceived = dateTime.plus(10, MINUTES),
                conversationId = conversationId,
                ucrBlocks = Seq(UcrBlock(ucr = "4444", ucrType = "M")),
                roe = None,
                soe = None
              )
            )
          )
        )
      )(FakeRequest(), messages)

      getElementById(pageWithData, s"ucr-$conversationId").text() must be("4444")
      getElementById(pageWithData, s"ucrType-$conversationId").text() must be("MUCR")
      getElementById(pageWithData, s"submissionAction-$conversationId").text() must be("Consolidate")
      getElementById(pageWithData, s"dateOfRequest-$conversationId").text() must be("2019-10-31 00:00")
      getElementById(pageWithData, s"dateOfUpdate-$conversationId").text() must be("2019-10-31 00:10")
      getElementById(pageWithData, s"noOfNotifications-$conversationId").text() must be("1")
    }
  }
}
