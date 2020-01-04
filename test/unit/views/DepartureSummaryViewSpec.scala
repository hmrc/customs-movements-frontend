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

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalTime}

import base.Injector
import forms.{ConsignmentReferences, DepartureDetails, Location, Transport}
import forms.common.{Date, Time}
import models.cache.{ArrivalAnswers, DepartureAnswers}
import models.requests.JourneyRequest
import org.jsoup.nodes.Document
import play.api.mvc.AnyContentAsEmpty
import views.html.summary.departure_summary_page

class DepartureSummaryViewSpec extends ViewSpec with Injector {

  private implicit val request: JourneyRequest[AnyContentAsEmpty.type] = journeyRequest(ArrivalAnswers())

  private val page = instanceOf[departure_summary_page]

  private val date = Date(LocalDate.now())
  private val time = Time(LocalTime.now().truncatedTo(ChronoUnit.MINUTES))

  private val answers = DepartureAnswers(
    consignmentReferences = Some(ConsignmentReferences("D", "ref-value")),
    departureDetails = Some(DepartureDetails(date, time)),
    location = Some(Location("location-ref")),
    transport = Some(Transport("1", "transport-nationality", "transport-id"))
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
    val view = page(answers)

    "render title" in {
      view.getTitle must containMessage("summary.departure.title")
    }

    "render heading" in {
      view.getElementById("title") must containMessage("summary.departure.title")
    }

    "render 'Consignment details' section in summary list" in {

      view.getElementsByClass("govuk-heading-m").get(section_consignment_details) must containMessage("summary.consignmentDetails")

      view.getElementsByClass("govuk-summary-list__key").get(answer_consignment_type) must containMessage("summary.referenceType")
      view.getElementsByClass("govuk-summary-list__value").get(answer_consignment_type) must containMessage("consignmentReferences.reference.ducr")

      val changeType = view.getElementsByClass("govuk-link").get(answer_consignment_type)
      changeType must containMessage("site.change")
      changeType must haveHref(controllers.routes.ConsignmentReferencesController.displayPage())

      view.getElementsByClass("govuk-summary-list__key").get(answer_consignment_reference) must containMessage("summary.referenceValue")
      view.getElementsByClass("govuk-summary-list__value").get(answer_consignment_reference).text() mustBe "ref-value"

      val changeRef = view.getElementsByClass("govuk-link").get(answer_consignment_reference)
      changeRef must containMessage("site.change")
      changeRef must haveHref(controllers.routes.ConsignmentReferencesController.displayPage())
    }

    "render 'Departure date and time' section in summary list" in {

      view.getElementsByClass("govuk-heading-m").get(section_depart_datetime) must containMessage("departureDetails.title")

      view.getElementsByClass("govuk-summary-list__key").get(answer_date) must containMessage("summary.departure.date")
      view.getElementsByClass("govuk-summary-list__value").get(answer_date).text mustBe date.toInputFormat

      val changeDate = view.getElementsByClass("govuk-link").get(answer_date)
      changeDate must containMessage("site.change")
      changeDate must haveHref(controllers.routes.MovementDetailsController.displayPage())

      view.getElementsByClass("govuk-summary-list__key").get(answer_time) must containMessage("summary.departure.time")
      view.getElementsByClass("govuk-summary-list__value").get(answer_time).text mustBe time.toInputFormat

      val changeTime = view.getElementsByClass("govuk-link").get(answer_time)
      changeTime must containMessage("site.change")
      changeTime must haveHref(controllers.routes.MovementDetailsController.displayPage())
    }

    "render 'Location' section in summary list" in {

      view.getElementsByClass("govuk-heading-m").get(section_location) must containMessage("location.title")

      view.getElementsByClass("govuk-summary-list__key").get(answer_location) must containMessage("summary.goodsLocation")
      view.getElementsByClass("govuk-summary-list__value").get(answer_location).text mustBe "location-ref"

      val changeDate = view.getElementsByClass("govuk-link").get(answer_location)
      changeDate must containMessage("site.change")
      changeDate must haveHref(controllers.routes.LocationController.displayPage())
    }

    "render 'Transport' section in summary list" in {

      view.getElementsByClass("govuk-heading-m").get(section_transport) must containMessage("transport.title")

      view.getElementsByClass("govuk-summary-list__key").get(answer_transport_type) must containMessage("summary.modeOfTransport")
      view.getElementsByClass("govuk-summary-list__value").get(answer_transport_type) must containMessage("transport.modeOfTransport.1")

      val changeMode = view.getElementsByClass("govuk-link").get(answer_transport_type)
      changeMode must containMessage("site.change")
      changeMode must haveHref(controllers.routes.TransportController.displayPage())

      view.getElementsByClass("govuk-summary-list__key").get(answer_transport_id) must containMessage("summary.transportId")
      view.getElementsByClass("govuk-summary-list__value").get(answer_transport_id).text mustBe "transport-id"

      val changeId = view.getElementsByClass("govuk-link").get(answer_transport_id)
      changeId must containMessage("site.change")
      changeId must haveHref(controllers.routes.TransportController.displayPage())

      view.getElementsByClass("govuk-summary-list__key").get(answer_transport_nationality) must containMessage("summary.nationality")
      view.getElementsByClass("govuk-summary-list__value").get(answer_transport_nationality).text mustBe "transport-nationality"

      val changeNationality = view.getElementsByClass("govuk-link").get(answer_transport_nationality)
      changeNationality must containMessage("site.change")
      changeNationality must haveHref(controllers.routes.TransportController.displayPage())
    }

    "render back button" in {
      val backButton = view.getBackButton

      backButton mustBe defined
      backButton.get must haveHref(controllers.routes.TransportController.displayPage())
    }

    "render 'Confirm and submit' button on page" in {
      view.getElementsByClass("govuk-button").first() must containMessage("site.confirmAndSubmit")
    }
  }

}
