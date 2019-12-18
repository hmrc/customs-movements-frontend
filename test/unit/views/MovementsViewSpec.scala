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
import java.time.{Instant, LocalDate, ZoneId, ZoneOffset}

import base.BaseSpec
import controllers.routes
import models.UcrBlock
import models.notifications.{Entry, Notification, ResponseType}
import models.submissions.{ActionType, Submission}
import play.api.test.FakeRequest
import play.twirl.api.Html
import testdata.CommonTestData.conversationId
import testdata.ConsolidationTestData
import testdata.ConsolidationTestData._
import testdata.MovementsTestData.exampleSubmission
import testdata.NotificationTestData.exampleNotificationFrontendModel
import views.html.movements
import views.spec.ViewValidator

class MovementsViewSpec extends BaseSpec with ViewTemplates with ViewValidator with MessagesStub {

  private implicit val implicitFakeRequest = FakeRequest()

  private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy 'at' HH:mm").withZone(ZoneId.of("Europe/London"))
  private val dateTime: Instant = LocalDate.of(2019, 10, 31).atStartOfDay().toInstant(ZoneOffset.UTC)

  private def createView(submissions: Seq[(Submission, Seq[Notification])] = Seq.empty): Html =
    new movements(mainTemplate, dateTimeFormatter)(submissions)(FakeRequest(), messages)

  "Movements page" should {

    "contain title" in {

      createView().getElementById("title") must containMessage("submissions.title")
    }

    "contain correct table headers" in {

      val page = createView()

      page.getElementById("ucr") must containMessage("submissions.ucr")
      page.getElementById("ucrType") must containMessage("submissions.submissionType")
      page.getElementById("submissionAction") must containMessage("submissions.submissionAction")
      page.getElementById("dateOfRequest") must containMessage("submissions.dateOfRequest")
    }

    "contain correct submission data" in {
      val submission = Submission(
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
          entries = Seq(Entry(ucrBlock = Some(UcrBlock(ucr = ConsolidationTestData.validMucr, ucrType = "M"))))
        )
      )

      val pageWithData: Html = createView(Seq(submission -> notifications))

      getElementById(pageWithData, s"ucr-$conversationId").text() must be("4444")
      getElementById(pageWithData, s"ucrType-$conversationId").text() must be("MUCR")
      getElementById(pageWithData, s"submissionAction-$conversationId") must containMessage("submissions.shutmucr")
      getElementById(pageWithData, s"dateOfRequest-$conversationId").text() must be("31 Oct 2019 at 00:00")
    }

    "contain MUCR and DUCR if Submission contains both" in {
      val notifications = Seq(
        exampleNotificationFrontendModel(
          conversationId = conversationId,
          responseType = ResponseType.ControlResponse,
          entries = Seq(Entry(ucrBlock = Some(UcrBlock(ucr = ConsolidationTestData.validMucr, ucrType = "M"))))
        )
      )

      val pageWithData: Html = createView(Seq(exampleAssociateDucrRequestSubmission -> notifications))

      val actualUcrs = getElementById(pageWithData, s"ucr-$conversationId").text()
      actualUcrs must include(validMucr)
      actualUcrs must include(validDucr)
      val actualUcrTypes = getElementById(pageWithData, s"ucrType-$conversationId").text()
      actualUcrTypes must include("MUCR")
      actualUcrTypes must include("DUCR")
    }

    "contain link to ViewNotifications page" when {
      "there are Notifications for the Submission" in {

        val submission = exampleSubmission(requestTimestamp = dateTime)
        val notifications = Seq(exampleNotificationFrontendModel(timestampReceived = dateTime.plusSeconds(3)))

        val page = createView(Seq((submission, notifications)))

        page.getElementById(s"ucr-$conversationId").child(0) must haveHref(routes.NotificationsController.listOfNotifications(conversationId))
      }
    }
  }
}
