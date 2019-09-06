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
import testdata.CommonTestData.conversationId
import testdata.ConsolidationTestData
import testdata.ConsolidationTestData.{exampleAssociateDucrRequestSubmission, ValidDucr, ValidMucr}
import testdata.NotificationTestData.exampleNotificationFrontendModel
import models.UcrBlock
import models.notifications.ResponseType
import models.submissions.{ActionType, SubmissionFrontendModel}
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

    "contain title" in {

      page.getElementById("title") must containText(messages("submissions.title"))
    }

    "contain correct table headers" in {

      page.getElementById("ucr") must containText(messages("submissions.ucr"))
      page.getElementById("ucrType") must containText(messages("submissions.submissionType"))
      page.getElementById("submissionAction") must containText(messages("submissions.submissionAction"))
      page.getElementById("dateOfRequest") must containText(messages("submissions.dateOfRequest"))
      page.getElementById("noOfNotifications") must containText(messages("submissions.noOfNotifications"))
    }

    "contain correct submission data" in {
      val dateTime: Instant = ZonedDateTime
        .of(
          LocalDate.parse("2019-10-31", DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay(),
          ZoneId.systemDefault()
        )
        .toInstant
      val submission = SubmissionFrontendModel(
        requestTimestamp = dateTime,
        eori = "",
        conversationId = conversationId,
        ucrBlocks = Seq(UcrBlock(ucr = "4444", ucrType = "M")),
        actionType = ActionType.ShutMucr
      )
      val notifications = Seq(
        exampleNotificationFrontendModel(
          timestampReceived = dateTime.plus(10, MINUTES),
          conversationId = conversationId,
          responseType = ResponseType.ControlResponse,
          ucrBlocks = Seq(UcrBlock(ucr = "4444", ucrType = "M"))
        )
      )

      val pageWithData: Html = new movements(mainTemplate)(Seq(submission -> notifications))(FakeRequest(), messages)

      getElementById(pageWithData, s"ucr-$conversationId").text() must be("4444")
      getElementById(pageWithData, s"ucrType-$conversationId").text() must be("MUCR")
      getElementById(pageWithData, s"submissionAction-$conversationId").text() must be("submissions.shutmucr")
      getElementById(pageWithData, s"dateOfRequest-$conversationId").text() must be("31 Oct 2019 at 00:00")
      getElementById(pageWithData, s"noOfNotifications-$conversationId").text() must be("1")
    }

    "contain MUCR and DUCR if Submission contains both" in {
      val notifications = Seq(
        exampleNotificationFrontendModel(
          conversationId = conversationId,
          responseType = ResponseType.ControlResponse,
          ucrBlocks = Seq(UcrBlock(ucr = ConsolidationTestData.ValidMucr, ucrType = "M"))
        )
      )

      val pageWithData: Html = new movements(mainTemplate)(Seq(exampleAssociateDucrRequestSubmission -> notifications))(
        FakeRequest(),
        messages
      )

      val actualUcrs = getElementById(pageWithData, s"ucr-$conversationId").text()
      actualUcrs must include(ValidMucr)
      actualUcrs must include(ValidDucr)
      val actualUcrTypes = getElementById(pageWithData, s"ucrType-$conversationId").text()
      actualUcrTypes must include("MUCR")
      actualUcrTypes must include("DUCR")
    }
  }
}
