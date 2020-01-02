/*
 * Copyright 2020 HM Revenue & Customs
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
import java.time.{Instant, LocalDate, LocalDateTime, ZoneId, ZoneOffset}
import java.util.Date

import base.Injector
import controllers.routes
import models.UcrBlock
import models.cache.ArrivalAnswers
import models.notifications.{Entry, Notification, ResponseType}
import models.submissions.{ActionType, Submission}
import org.jsoup.nodes.Document
import play.twirl.api.Html
import testdata.CommonTestData._
import testdata.ConsolidationTestData._
import testdata.MovementsTestData.exampleSubmission
import testdata.NotificationTestData.exampleNotificationFrontendModel
import views.html.movements

class MovementsViewSpec extends ViewSpec with Injector {

  private implicit val implicitFakeRequest = journeyRequest(ArrivalAnswers())
  private val page = instanceOf[movements]

  private val dateTime: Instant = LocalDate.of(2019, 10, 31).atStartOfDay().toInstant(ZoneOffset.UTC)

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

      val doc: Document = emptyPage

      doc.selectFirst(".govuk-table__header.ucr") must containMessage("submissions.ucr")
      doc.selectFirst(".govuk-table__header.submission-type") must containMessage("submissions.submissionType")
      doc.selectFirst(".govuk-table__header.date-of-request") must containMessage("submissions.dateOfRequest")
      doc.selectFirst(".govuk-table__header.submission-action") must containMessage("submissions.submissionAction")
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

      val pageWithData: Document = createView(Seq(shutMucrSubmission -> shutMucrNotifications, arrivalSubmission -> arrivalNotifications))

      val firstDataRowElements = pageWithData.selectFirst(".govuk-table__body .govuk-table__row:nth-child(1)")
      val secondDataRowElements = pageWithData.selectFirst(".govuk-table__body .govuk-table__row:nth-child(2)")

      val formatter = DateTimeFormatter.ofPattern("dd MMM YYYY 'at' HH:mm")

      firstDataRowElements.selectFirst(".ucr").text() mustBe validMucr
      firstDataRowElements.selectFirst(".submission-type").text() mustBe "MUCR"
      firstDataRowElements.selectFirst(".date-of-request").text() mustBe LocalDateTime
        .of(2019, 10, 31, 0, 0)
        .format(formatter) // "31 Oct 2019 at 00:00"
      firstDataRowElements.selectFirst(".submission-action") must containMessage("submissions.shutmucr")

      secondDataRowElements.selectFirst(".ucr").text() mustBe validDucr
      secondDataRowElements.selectFirst(".submission-type").text() mustBe "DUCR"
      secondDataRowElements.selectFirst(".date-of-request").text() mustBe LocalDateTime
        .of(2019, 10, 31, 0, 31)
        .format(formatter) //"31 Oct 2019 at 00:31"
      secondDataRowElements.selectFirst(".submission-action") must containMessage("submissions.arrival")
    }

    "contain MUCR and DUCR if Submission contains both" in {

      val notifications = Seq(
        exampleNotificationFrontendModel(
          conversationId = conversationId,
          responseType = ResponseType.ControlResponse,
          entries = Seq(Entry(ucrBlock = Some(UcrBlock(ucr = validMucr, ucrType = "M"))))
        )
      )

      val pageWithData: Document = createView(Seq(exampleAssociateDucrRequestSubmission -> notifications))

      val firstDataRowElements = pageWithData.selectFirst(".govuk-table__body .govuk-table__row:nth-child(1)")

      val actualUcrs = firstDataRowElements.selectFirst(".ucr").text()
      actualUcrs must include(validMucr)
      actualUcrs must include(validDucr)
      val actualUcrTypes = firstDataRowElements.selectFirst(".submission-type").text()
      actualUcrTypes must include("MUCR")
      actualUcrTypes must include("DUCR")
    }

    "contain link to ViewNotifications page" when {
      "there are Notifications for the Submission" in {

        val submission = exampleSubmission(requestTimestamp = dateTime)
        val notifications = Seq(exampleNotificationFrontendModel(timestampReceived = dateTime.plusSeconds(3)))

        val page: Document = createView(Seq((submission, notifications)))

        val firstDataRowUcrCell = page.selectFirst(".govuk-table__body .govuk-table__row:nth-child(1)")

        firstDataRowUcrCell.selectFirst(".ucr").child(0) must haveHref(routes.NotificationsController.listOfNotifications(conversationId))
      }
    }
  }
}
