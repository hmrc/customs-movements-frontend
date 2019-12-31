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

import base.Injector
import controllers.routes
import models.UcrBlock
import models.cache.ArrivalAnswers
import models.notifications.{Entry, Notification, ResponseType}
import models.submissions.{ActionType, Submission}
import play.twirl.api.Html
import testdata.CommonTestData._
import testdata.ConsolidationTestData._
import testdata.MovementsTestData.exampleSubmission
import testdata.NotificationTestData.exampleNotificationFrontendModel
import views.html.movements

class MovementsViewSpec extends ViewSpec with Injector {

  private implicit val implicitFakeRequest = journeyRequest(ArrivalAnswers())

  private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy 'at' HH:mm").withZone(ZoneId.of("Europe/London"))
  private val dateTime: Instant = LocalDate.of(2019, 10, 31).atStartOfDay().toInstant(ZoneOffset.UTC)

  private val page = instanceOf[movements]

  private def createView(submissions: Seq[(Submission, Seq[Notification])] = Seq.empty): Html = page(submissions)

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

      val allHeaders = emptyPage.getElementsByClass("govuk-table__header")

      allHeaders.get(0) must containMessage("submissions.ucr")
      allHeaders.get(1) must containMessage("submissions.submissionType")
      allHeaders.get(2) must containMessage("submissions.dateOfRequest")
      allHeaders.get(3) must containMessage("submissions.submissionAction")
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

      val allRows = pageWithData.getElementsByClass("govuk-table__row")
      allRows.size mustBe 3

      val firstDataRowElements = allRows.get(1).getElementsByClass("govuk-table__cell")
      val secondDataRowElements = allRows.get(2).getElementsByClass("govuk-table__cell")

      firstDataRowElements.get(0).text() mustBe validMucr
      firstDataRowElements.get(1).text() mustBe "MUCR"
      firstDataRowElements.get(2).text() mustBe "31 Oct 2019 at 00:00"
      firstDataRowElements.get(3) must containMessage("submissions.shutmucr")

      secondDataRowElements.get(0).text() mustBe validDucr
      secondDataRowElements.get(1).text() mustBe "DUCR"
      secondDataRowElements.get(2).text() mustBe "31 Oct 2019 at 00:31"
      secondDataRowElements.get(3) must containMessage("submissions.arrival")
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

      val allRows = pageWithData.getElementsByClass("govuk-table__row")
      val firstDataRowElements = allRows.get(1).getElementsByClass("govuk-table__cell")

      val actualUcrs = firstDataRowElements.get(0).text()
      actualUcrs must include(validMucr)
      actualUcrs must include(validDucr)
      val actualUcrTypes = firstDataRowElements.get(1).text()
      actualUcrTypes must include("MUCR")
      actualUcrTypes must include("DUCR")
    }

    "contain link to ViewNotifications page" when {
      "there are Notifications for the Submission" in {

        val submission = exampleSubmission(requestTimestamp = dateTime)
        val notifications = Seq(exampleNotificationFrontendModel(timestampReceived = dateTime.plusSeconds(3)))

        val page = createView(Seq((submission, notifications)))

        val allRows = page.getElementsByClass("govuk-table__row")
        val firstDataRowUcrCell = allRows.get(1).getElementsByClass("govuk-table__cell").get(0)

        firstDataRowUcrCell.child(0) must haveHref(routes.NotificationsController.listOfNotifications(conversationId))
      }
    }
  }
}
