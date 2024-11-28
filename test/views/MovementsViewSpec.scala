/*
 * Copyright 2024 HM Revenue & Customs
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
import connectors.exchanges.ActionType.{ConsolidationType, MovementType}
import forms.UcrType
import models.UcrBlock
import models.submissions.Submission
import org.jsoup.nodes.Document
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import play.twirl.api.Html
import testdata.CommonTestData._
import testdata.ConsolidationTestData._
import views.helpers.ViewDates
import views.html.movements

import java.time.temporal.ChronoUnit.MINUTES
import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}

class MovementsViewSpec extends ViewSpec with Injector {

  implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val page = instanceOf[movements]
  private val dateTime: Instant = LocalDate.of(2019, 10, 31).atStartOfDay().toInstant(ZoneOffset.UTC)

  private def createView(submissions: Seq[Submission] = Seq.empty): Html =
    page(submissions)(request, messages)

  "Movements page" should {
    val emptyPage = createView()

    "contain title" in {
      emptyPage.getTitle must containMessage("submissions.title")
    }

    "contain back button" in {
      val backButton = emptyPage.getElementById("back-link")

      backButton must containMessage("site.back")
      backButton must haveHref(backButtonDefaultCall)
    }

    "contain header" in {
      emptyPage.getElementById("title") must containMessage("submissions.title")
    }

    "contain information paragraph" in {
      emptyPage.getElementsByClass("govuk-body-l").first() must containMessage("submissions.summary.1")
      emptyPage.getElementsByClass("govuk-body-l").first() must containMessage("submissions.summary.2")
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
        ucrBlocks = Seq(UcrBlock(ucr = validMucr, ucrType = UcrType.Mucr.codeValue)),
        actionType = ConsolidationType.ShutMucr
      )

      val arrivalSubmission = Submission(
        requestTimestamp = dateTime.plus(31, MINUTES),
        eori = "",
        conversationId = conversationId_2,
        ucrBlocks = Seq(UcrBlock(ucr = validDucr, ucrType = UcrType.Ducr.codeValue)),
        actionType = MovementType.Arrival
      )

      val departureSubmission = Submission(
        requestTimestamp = dateTime.plus(33, MINUTES),
        eori = "",
        conversationId = conversationId_3,
        ucrBlocks = Seq(UcrBlock(ucr = validWholeDucrParts, ucrType = UcrType.DucrPart.codeValue)),
        actionType = MovementType.Departure
      )

      val associateSubmission = Submission(
        requestTimestamp = dateTime.plus(30, MINUTES),
        eori = "",
        conversationId = conversationId_4,
        ucrBlocks = Seq(UcrBlock(ucr = validDucr, ucrType = UcrType.Ducr.codeValue), UcrBlock(ucr = validMucr, ucrType = UcrType.Mucr.codeValue)),
        actionType = ConsolidationType.DucrAssociation
      )

      val pageWithData: Document = createView(Seq(shutMucrSubmission, arrivalSubmission, departureSubmission, associateSubmission))

      val firstDataRowElements = pageWithData.selectFirst(".govuk-table__body .govuk-table__row:nth-child(1)")
      val secondDataRowElements = pageWithData.selectFirst(".govuk-table__body .govuk-table__row:nth-child(2)")
      val thirdDataRowElements = pageWithData.selectFirst(".govuk-table__body .govuk-table__row:nth-child(3)")
      val fourthDataRowElements = pageWithData.selectFirst(".govuk-table__body .govuk-table__row:nth-child(4)")

      firstDataRowElements.selectFirst(".ucr").text() mustBe s"$validMucr ${messages("submissions.hidden.text", validMucr)}"
      firstDataRowElements.selectFirst(".submission-type") must containMessage("submissions.submissionType.M")
      firstDataRowElements.selectFirst(".date-of-request").text() mustBe ViewDates.formatDateAtTime(
        LocalDateTime
          .of(2019, 10, 31, 0, 0)
      ) // "31 Oct 2019 at 00:00"
      firstDataRowElements.selectFirst(".submission-action") must containMessage("submissions.shutmucr")

      secondDataRowElements.selectFirst(".ucr").text() mustBe s"$validDucr ${messages("submissions.hidden.text", validDucr)}"
      secondDataRowElements.selectFirst(".submission-type") must containMessage("submissions.submissionType.D")
      secondDataRowElements.selectFirst(".date-of-request").text() mustBe ViewDates.formatDateAtTime(
        LocalDateTime
          .of(2019, 10, 31, 0, 31)
      ) // "31 Oct 2019 at 00:31"
      secondDataRowElements.selectFirst(".submission-action") must containMessage("submissions.arrival")

      thirdDataRowElements.selectFirst(".ucr").text() mustBe s"$validWholeDucrParts ${messages("submissions.hidden.text", validWholeDucrParts)}"
      thirdDataRowElements.selectFirst(".submission-type") must containMessage("submissions.submissionType.DP")
      thirdDataRowElements.selectFirst(".date-of-request").text() mustBe ViewDates.formatDateAtTime(
        LocalDateTime
          .of(2019, 10, 31, 0, 33)
      ) // "31 Oct 2019 at 00:33"
      thirdDataRowElements.selectFirst(".submission-action") must containMessage("submissions.departure")

      fourthDataRowElements
        .selectFirst(".ucr")
        .text() mustBe s"$validDucr $validMucr ${messages("submissions.hidden.text", validDucr + ", " + validMucr)}"
      fourthDataRowElements.selectFirst(".submission-type") must containMessage("submissions.submissionType.D")
      fourthDataRowElements.selectFirst(".date-of-request").text() mustBe ViewDates.formatDateAtTime(
        LocalDateTime
          .of(2019, 10, 31, 0, 30)
      ) // "31 Oct 2019 at 00:30"
      fourthDataRowElements.selectFirst(".submission-action") must containMessage("submissions.ducrassociation")
    }

    "contain MUCR and DUCR if Submission contains both" in {
      val pageWithData: Document = createView(Seq(exampleAssociateDucrRequestSubmission))

      val firstDataRowElements = pageWithData.selectFirst(".govuk-table__body .govuk-table__row:nth-child(1)")

      val actualUcrs = firstDataRowElements.selectFirst(".ucr").text()
      actualUcrs must include(validMucr)
      actualUcrs must include(validDucr)
      val actualUcrTypes = firstDataRowElements.selectFirst(".submission-type").text()
      actualUcrTypes must include("MUCR")
      actualUcrTypes must include("DUCR")
    }
  }
}
