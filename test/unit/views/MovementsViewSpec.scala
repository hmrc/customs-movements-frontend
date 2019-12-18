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

import controllers.routes
import models.UcrBlock
import models.notifications.{Entry, Notification, ResponseType}
import models.submissions.{ActionType, Submission}
import play.api.test.FakeRequest
import play.twirl.api.Html
import testdata.CommonTestData._
import testdata.ConsolidationTestData._
import testdata.MovementsTestData.exampleSubmission
import testdata.NotificationTestData.exampleNotificationFrontendModel
import views.html.movements

class MovementsViewSpec extends ViewSpec {

  private implicit val implicitFakeRequest = FakeRequest()

  private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy 'at' HH:mm").withZone(ZoneId.of("Europe/London"))
  private val dateTime: Instant = LocalDate.of(2019, 10, 31).atStartOfDay().toInstant(ZoneOffset.UTC)

  private def createView(submissions: Seq[(Submission, Seq[Notification])] = Seq.empty): Html =
    new movements(mainTemplate, dateTimeFormatter)(submissions)(FakeRequest(), messages)

  "Movements page" should {
    val emptyPage = createView()

    "contain title" in {
      emptyPage.getTitle must containMessage("submissions.title")
    }

    "contain back button" in {
      emptyPage.getBackButton mustBe defined
      emptyPage.getBackButton.get must containMessage("site.back.toStartPage")
      emptyPage.getBackButton.get must haveHref(controllers.routes.ChoiceController.displayChoiceForm())
    }

    "contain header" in {
      emptyPage.getElementById("title") must containMessage("submissions.title")
    }

    "contain information paragraph" in {
      emptyPage.getElementsByClass("govuk-body-l").first() must containMessage("submissions.summary")
    }

    "contain correct table headers" in {

      emptyPage.getElementById("ucr") must containMessage("submissions.ucr")
      emptyPage.getElementById("ucrType") must containMessage("submissions.submissionType")
      emptyPage.getElementById("submissionAction") must containMessage("submissions.submissionAction")
      emptyPage.getElementById("dateOfRequest") must containMessage("submissions.dateOfRequest")
    }

    "contain correct submission data" in {
      val shutMucrSubmission = Submission(
        requestTimestamp = dateTime,
        eori = "",
        conversationId = conversationId,
        ucrBlocks = Seq(UcrBlock(ucr = validMucr, ucrType = "M")),
        actionType = ActionType.ShutMucr
      )
      val shutMucrNotifications = Seq(
        exampleNotificationFrontendModel(
          timestampReceived = dateTime.plus(10, MINUTES),
          conversationId = conversationId,
          responseType = ResponseType.ControlResponse,
          entries = Seq(Entry(ucrBlock = Some(UcrBlock(ucr = validMucr, ucrType = "M"))))
        )
      )

      val arrivalSubmission = Submission(
        requestTimestamp = dateTime.plus(31, MINUTES),
        eori = "",
        conversationId = conversationId_2,
        ucrBlocks = Seq(UcrBlock(ucr = validDucr, ucrType = "D")),
        actionType = ActionType.Arrival
      )
      val arrivalNotifications = Seq(
        exampleNotificationFrontendModel(
          timestampReceived = dateTime.plus(35, MINUTES),
          conversationId = conversationId_2,
          responseType = ResponseType.ControlResponse,
          entries = Seq(Entry(ucrBlock = Some(UcrBlock(ucr = validDucr, ucrType = "D"))))
        )
      )

      val pageWithData: Html = createView(Seq(shutMucrSubmission -> shutMucrNotifications, arrivalSubmission -> arrivalNotifications))

      pageWithData.getElementById(s"ucr-$conversationId").text() must be(validMucr)
      pageWithData.getElementById(s"ucrType-$conversationId").text() must be("MUCR")
      pageWithData.getElementById(s"submissionAction-$conversationId") must containMessage("submissions.shutmucr")
      pageWithData.getElementById(s"dateOfRequest-$conversationId").text() must be("31 Oct 2019 at 00:00")

      pageWithData.getElementById(s"ucr-$conversationId_2").text() must be(validDucr)
      pageWithData.getElementById(s"ucrType-$conversationId_2").text() must be("DUCR")
      pageWithData.getElementById(s"submissionAction-$conversationId_2") must containMessage("submissions.arrival")
      pageWithData.getElementById(s"dateOfRequest-$conversationId_2").text() must be("31 Oct 2019 at 00:31")
    }

    "contain MUCR and DUCR if Submission contains both" in {
      val notifications = Seq(
        exampleNotificationFrontendModel(
          conversationId = conversationId,
          responseType = ResponseType.ControlResponse,
          entries = Seq(Entry(ucrBlock = Some(UcrBlock(ucr = validMucr, ucrType = "M"))))
        )
      )

      val pageWithData: Html = createView(Seq(exampleAssociateDucrRequestSubmission -> notifications))

      val actualUcrs = pageWithData.getElementById(s"ucr-$conversationId").text()
      actualUcrs must include(validMucr)
      actualUcrs must include(validDucr)
      val actualUcrTypes = pageWithData.getElementById(s"ucrType-$conversationId").text()
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
