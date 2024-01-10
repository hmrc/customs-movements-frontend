/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers.routes.SpecificDateTimeController
import forms.DepartureDetails
import forms.common.{Date, Time}
import models.cache.DepartureAnswers
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.data.Form
import play.twirl.api.Html
import testdata.MovementsTestData
import views.html.departure_details

import java.text.DecimalFormat
import java.time.{LocalDate, LocalTime}
import scala.jdk.CollectionConverters.IterableHasAsScala

class DepartureDetailsViewSpec extends ViewSpec with Injector {

  private val page = instanceOf[departure_details]

  private val movementDetails = MovementsTestData.movementDetails

  private val form = movementDetails.departureForm()
  private val consignmentReferencesValue = "M-ref"

  private def createView(form: Form[DepartureDetails])(implicit request: JourneyRequest[_]): Html =
    page(form, consignmentReferencesValue)

  private def convertIntoTwoDigitFormat(input: Int): String = {
    val formatter = new DecimalFormat("00")
    formatter.format(input.toLong)
  }

  private def convertIntoFourDigitFormat(input: Int): String = {
    val formatter = new DecimalFormat("0000")
    formatter.format(input.toLong)
  }

  "Departure Details View" when {

    implicit val request = journeyRequest(DepartureAnswers())

    "the page has errors" should {
      "have the page's title prefixed with 'Error:'" in {
        val view = createView(form.withGlobalError("error.summary.title"))
        view.head.getElementsByTag("title").first.text must startWith("Error: ")
      }
    }

    "provided with empty form" should {
      val emptyView = createView(form)

      "have title" in {
        emptyView.getTitle must containMessage("departureDetails.header")
      }

      "have 'Back' button linking to /specific-date-and-time page" in {
        val backButton = createView(form).getBackButton

        backButton mustBe defined
        backButton.get must haveHref(SpecificDateTimeController.displayPage)
        backButton.get must containMessage("site.back.previousQuestion")
      }

      "have section header" in {
        emptyView.getElementById("section-header") must containMessage("departureDetails.sectionHeading", consignmentReferencesValue)
      }

      "have heading" in {
        emptyView.getElementById("title") must containMessage("departureDetails.header")
      }

      "have date section" which {

        "contains label" in {
          emptyView.getElementsByTag("legend").asScala.exists { elem =>
            elem.text() == messages("departureDetails.date.question")
          }
        }

        "contains hint" in {
          emptyView.getElementById("dateOfDeparture-hint") must containMessage("departureDetails.date.hint")
        }

        "contains input for day" in {
          emptyView.getElementsByAttributeValue("for", "dateOfDeparture_day").first() must containMessage("date.day")
          emptyView.getElementById("dateOfDeparture_day").`val`() mustBe empty
        }

        "contains input for month" in {
          emptyView.getElementsByAttributeValue("for", "dateOfDeparture_month").first() must containMessage("date.month")
          emptyView.getElementById("dateOfDeparture_month").`val`() mustBe empty
        }

        "contains input for year" in {
          emptyView.getElementsByAttributeValue("for", "dateOfDeparture_year").first() must containMessage("date.year")
          emptyView.getElementById("dateOfDeparture_year").`val`() mustBe empty
        }
      }

      "have time section" which {

        "contains label" in {
          emptyView.getElementsByTag("legend").asScala.exists { elem =>
            elem.text() == messages("departureDetails.time.question")
          }
        }

        "contains hint" in {
          emptyView.getElementById("timeOfDeparture-hint") must containMessage("departureDetails.time.hint")
        }

        "contains input for hour" in {
          emptyView.getElementsByAttributeValue("for", "timeOfDeparture_hour").first() must containMessage("time.hour")
          emptyView.getElementById("timeOfDeparture_hour").`val`() mustBe empty
        }

        "contains input for minute" in {
          emptyView.getElementsByAttributeValue("for", "timeOfDeparture_minute").first() must containMessage("time.minute")
          emptyView.getElementById("timeOfDeparture_minute").`val`() mustBe empty
        }
      }

      checkAllSaveButtonsAreDisplayed(createView(form)(journeyRequest(DepartureAnswers(readyToSubmit = Some(true)))))

      checkSaveAndReturnToSummaryButtonIsHidden(emptyView)

    }

    "provided with form containing data" should {
      val date = LocalDate.now().minusDays(1)
      val time = LocalTime.of(1, 2)
      val viewWithData = createView(form.fill(DepartureDetails(Date(date), Time(time))))

      "have value in day field" in {
        viewWithData.getElementById("dateOfDeparture_day").`val`() mustBe convertIntoTwoDigitFormat(date.getDayOfMonth)
      }

      "have value in month field" in {
        viewWithData.getElementById("dateOfDeparture_month").`val`() mustBe convertIntoTwoDigitFormat(date.getMonthValue)
      }

      "have value in year field" in {
        viewWithData.getElementById("dateOfDeparture_year").`val`() mustBe convertIntoFourDigitFormat(date.getYear)
      }

      "have value in hour field" in {
        viewWithData.getElementById("timeOfDeparture_hour").`val`() mustBe time.getHour.toString
      }

      "have value in minute field" in {
        viewWithData.getElementById("timeOfDeparture_minute").`val`() mustBe convertIntoTwoDigitFormat(time.getMinute)
      }
    }

    "provided with Date error" should {
      val viewWithDateError: Document = createView(form.withError("dateOfDeparture", "date.error.invalid"))

      "have error summary" in {
        viewWithDateError must haveGovUkGlobalErrorSummary
      }

      "have field error for Date" in {
        viewWithDateError must haveGovUkFieldError("dateOfDeparture", messages("date.error.invalid"))
        viewWithDateError must haveGovUkGlobalErrorLink("#dateOfDeparture.day", messages("date.error.invalid"))
      }
    }

    "provided with Time error" should {
      val viewWithTimeError: Document = createView(form.withError("timeOfDeparture", "time.error.invalid"))

      "have error summary" in {
        viewWithTimeError must haveGovUkGlobalErrorSummary
      }

      "have field error for Time" in {
        viewWithTimeError must haveGovUkFieldError("timeOfDeparture", messages("time.error.invalid"))
      }
    }

    "provided with form level DateTime error" should {
      val viewWithDateError: Document = createView(
        movementDetails
          .departureForm()
          .withError("dateOfDeparture", "departure.details.error.future")
          .withError("timeOfDeparture", "departure.details.error.future")
      )

      "have single error in summary" in {
        viewWithDateError.getElementsByClass("govuk-list govuk-error-summary__list").text() mustBe (messages("departure.details.error.future"))
      }
    }
  }
}
