/*
 * Copyright 2022 HM Revenue & Customs
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
import controllers.routes.{ConsignmentReferencesController, LocationController, MovementDetailsController, TransportController}
import forms._
import forms.common.{Date, Time}
import models.UcrBlock
import models.cache.DepartureAnswers
import org.scalatestplus.mockito.MockitoSugar
import views.html.summary.departure_summary_page

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalTime}

class DepartureSummaryViewSpec extends ViewSpec with Injector with MockitoSugar {

  private implicit val request = journeyRequest(DepartureAnswers())

  private val viewDates = new ViewDates()

  private val page = instanceOf[departure_summary_page]

  private val date = Date(LocalDate.now())
  private val time = Time(LocalTime.now().truncatedTo(ChronoUnit.MINUTES))

  private val answers = DepartureAnswers(
    consignmentReferences = Some(ConsignmentReferences("D", "ref-value")),
    departureDetails = Some(DepartureDetails(date, time)),
    location = Some(Location("location-ref")),
    transport = Some(Transport("1", "transport-id", "GB"))
  )

  private val section_consignment_details = 0
  private val section_depart_datetime = 1
  private val section_location = 2
  private val section_transport = 3

  private val answer_consignment_type = 0
  private val answer_consignment_reference = 1
  private val answer_date = 2
  private val answer_time = 3
  private val answer_location = 4
  private val answer_transport_type = 5
  private val answer_transport_id = 6
  private val answer_transport_nationality = 7

  "View" should {

    "render title" in {
      page(answers).getTitle must containMessage("summary.departure.title")
    }

    "render heading" in {
      page(answers).getElementById("title") must containMessage("summary.departure.title")
    }

    "render 'Consignment details' section in summary list" in {
      val answer_consignment_type_link_index = answer_consignment_type + 1
      val answer_consignment_reference_link_index = answer_consignment_reference + 1

      val view = page(answers)
      view.getElementsByClass("govuk-heading-m").get(section_consignment_details) must containMessage("summary.consignmentDetails")

      view.getElementsByClass("govuk-summary-list__key").get(answer_consignment_type) must containMessage("summary.referenceType")
      view.getElementsByClass("govuk-summary-list__value").get(answer_consignment_type) must containMessage("consignment.references.ducr")

      val changeType = view.getElementsByClass("govuk-link").get(answer_consignment_type_link_index)
      changeType must containMessage("site.change")
      changeType must haveHref(ConsignmentReferencesController.displayPage())

      view.getElementsByClass("govuk-summary-list__key").get(answer_consignment_reference) must containMessage("summary.referenceValue")
      view.getElementsByClass("govuk-summary-list__value").get(answer_consignment_reference).text() mustBe "ref-value"

      val changeRef = view.getElementsByClass("govuk-link").get(answer_consignment_reference_link_index)
      changeRef must containMessage("site.change")
      changeRef must haveHref(ConsignmentReferencesController.displayPage())
    }

    "render correct Consignment type" when {

      "provided with DUCR" in {
        val view = page(answers.copy(consignmentReferences = Some(ConsignmentReferences(reference = UcrType.Ducr, "ref-value"))))

        view.getElementsByClass("govuk-summary-list__value").get(answer_consignment_type) must containMessage("consignment.references.ducr")
      }

      "provided with MUCR" in {
        val view = page(answers.copy(consignmentReferences = Some(ConsignmentReferences(reference = UcrType.Mucr, "ref-value"))))

        view.getElementsByClass("govuk-summary-list__value").get(answer_consignment_type) must containMessage("consignment.references.mucr")
      }

      "provided with DUCR Part" in {
        val view = page(answers.copy(consignmentReferences = Some(ConsignmentReferences(reference = UcrType.DucrPart, "ref-value"))))

        view.getElementsByClass("govuk-summary-list__value").get(answer_consignment_type) must containMessage("consignment.references.ducrPart")
      }
    }

    "render 'Departure date and time' section in summary list" in {
      val answer_date_link_index = answer_date + 1
      val answer_time_link_index = answer_time + 1

      val view = page(answers)
      view.getElementsByClass("govuk-heading-m").get(section_depart_datetime) must containMessage("departureDetails.title")

      view.getElementsByClass("govuk-summary-list__key").get(answer_date) must containMessage("summary.departure.date")
      view.getElementsByClass("govuk-summary-list__value").get(answer_date).text mustBe viewDates.formatDate(date.date)

      val changeDate = view.getElementsByClass("govuk-link").get(answer_date_link_index)
      changeDate must containMessage("site.change")
      changeDate must haveHref(MovementDetailsController.displayPage())

      view.getElementsByClass("govuk-summary-list__key").get(answer_time) must containMessage("summary.departure.time")
      view.getElementsByClass("govuk-summary-list__value").get(answer_time).text mustBe viewDates.formatTime(time.time)

      val changeTime = view.getElementsByClass("govuk-link").get(answer_time_link_index)
      changeTime must containMessage("site.change")
      changeTime must haveHref(MovementDetailsController.displayPage())
    }

    "render 'Location' section in summary list" in {
      val answer_location_link_index = answer_location + 1

      val view = page(answers)
      view.getElementsByClass("govuk-heading-m").get(section_location) must containMessage("location.title")

      view.getElementsByClass("govuk-summary-list__key").get(answer_location) must containMessage("summary.goodsLocation")
      view.getElementsByClass("govuk-summary-list__value").get(answer_location).text mustBe "location-ref"

      val changeDate = view.getElementsByClass("govuk-link").get(answer_location_link_index)
      changeDate must containMessage("site.change")
      changeDate must haveHref(LocationController.displayPage())
    }

    "render 'Transport' section in summary list" in {
      val answer_transport_type_link_index = answer_transport_type + 1
      val answer_transport_id_link_index = answer_transport_id + 1
      val answer_transport_nationality_link_index = answer_transport_nationality + 1

      val view = page(answers)
      view.getElementsByClass("govuk-heading-m").get(section_transport) must containMessage("transport.title")

      view.getElementsByClass("govuk-summary-list__key").get(answer_transport_type) must containMessage("summary.modeOfTransport")
      view.getElementsByClass("govuk-summary-list__value").get(answer_transport_type) must containMessage("transport.modeOfTransport.1")

      val changeMode = view.getElementsByClass("govuk-link").get(answer_transport_type_link_index)
      changeMode must containMessage("site.change")
      changeMode must haveHref(TransportController.displayPage())

      view.getElementsByClass("govuk-summary-list__key").get(answer_transport_id) must containMessage("summary.transportId")
      view.getElementsByClass("govuk-summary-list__value").get(answer_transport_id).text mustBe "transport-id"

      val changeId = view.getElementsByClass("govuk-link").get(answer_transport_id_link_index)
      changeId must containMessage("site.change")
      changeId must haveHref(TransportController.displayPage())

      view.getElementsByClass("govuk-summary-list__key").get(answer_transport_nationality) must containMessage("summary.nationality")
      view
        .getElementsByClass("govuk-summary-list__value")
        .get(answer_transport_nationality)
        .text mustBe "United Kingdom, Great Britain, Northern Ireland - GB"

      val changeNationality = view.getElementsByClass("govuk-link").get(answer_transport_nationality_link_index)
      changeNationality must containMessage("site.change")
      changeNationality must haveHref(TransportController.displayPage())
    }

    "render back button" in {
      val backButton = page(answers).getBackButton

      backButton mustBe defined
      backButton.get must haveHref(TransportController.displayPage())
    }

    "render 'Confirm and submit' button on page" in {
      page(answers).getElementsByClass("govuk-button").first() must containMessage("site.confirmAndSubmit")
    }

    "render change consignment links on NON-'Find-a-consignment' journeys" in {
      val links = page(answers).getElementsByClass("govuk-link")
      links.toString must include(ConsignmentReferencesController.displayPage().url)
    }

    "not render change consignment links on a 'Find-a-consignment' journey" in {
      implicit val request = journeyRequest(DepartureAnswers(), Some(UcrBlock("ucr", UcrType.Ducr)), true)
      val links = page(answers).getElementsByClass("govuk-link")
      links.toString must not include ConsignmentReferencesController.displayPage().url
    }
  }
}
