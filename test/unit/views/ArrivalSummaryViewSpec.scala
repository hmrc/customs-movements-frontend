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

import base.OverridableInjector
import config.IleQueryConfig
import forms.common.{Date, Time}
import forms.{ArrivalDetails, ConsignmentReferences, Location, UcrType}
import models.cache.ArrivalAnswers
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import views.html.summary.arrival_summary_page

class ArrivalSummaryViewSpec extends ViewSpec with MockitoSugar with BeforeAndAfterEach {

  private implicit val request = journeyRequest(ArrivalAnswers())

  private val appConfig = mock[IleQueryConfig]
  private val injector = new OverridableInjector(bind[IleQueryConfig].toInstance(appConfig))

  private val page = injector.instanceOf[arrival_summary_page]

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(appConfig.isIleQueryEnabled).thenReturn(false)
  }

  override def afterEach(): Unit = {
    reset(appConfig)

    super.afterEach()
  }

  private val date = Date(LocalDate.now())
  private val time = Time(LocalTime.now().truncatedTo(ChronoUnit.MINUTES))

  private val answers = ArrivalAnswers(
    consignmentReferences = Some(ConsignmentReferences("D", "ref-value")),
    arrivalDetails = Some(ArrivalDetails(date, time)),
    location = Some(Location("location-ref"))
  )

  private val section_consignment_details = 0
  private val section_arrival_datetime = 1
  private val section_location = 2

  private val answer_consignment_type = 0
  private val answer_consignment_reference = 1
  private val answer_date = 2
  private val answer_time = 3
  private val answer_location = 4

  "View" should {

    "render title" in {

      page(answers).getTitle must containMessage("summary.arrival.title")
    }

    "render heading" in {

      page(answers).getElementById("title") must containMessage("summary.arrival.title")
    }

    "render 'Consignment details' section in summary list" in {

      val answer_consignment_type_link_index = answer_consignment_type + 1
      val answer_consignment_reference_link_index = answer_consignment_reference + 1

      val view = page(answers)
      view.getElementsByClass("govuk-heading-m").get(section_consignment_details) must containMessage("summary.consignmentDetails")

      view.getElementsByClass("govuk-summary-list__key").get(answer_consignment_type) must containMessage("summary.referenceType")
      view.getElementsByClass("govuk-summary-list__value").get(answer_consignment_type) must containMessage("consignmentReferences.reference.ducr")

      val changeType = view.getElementsByClass("govuk-link").get(answer_consignment_type_link_index)
      changeType must containMessage("site.change")
      changeType must haveHref(controllers.routes.ConsignmentReferencesController.displayPage())

      view.getElementsByClass("govuk-summary-list__key").get(answer_consignment_reference) must containMessage("summary.referenceValue")
      view.getElementsByClass("govuk-summary-list__value").get(answer_consignment_reference).text() mustBe "ref-value"

      val changeRef = view.getElementsByClass("govuk-link").get(answer_consignment_reference_link_index)
      changeRef must containMessage("site.change")
      changeRef must haveHref(controllers.routes.ConsignmentReferencesController.displayPage())
    }

    "render correct Consignment type" when {

      "provided with DUCR" in {

        val view = page(answers.copy(consignmentReferences = Some(ConsignmentReferences(reference = UcrType.Ducr, "ref-value"))))

        view.getElementsByClass("govuk-summary-list__value").get(answer_consignment_type) must containMessage("consignmentReferences.reference.ducr")
      }

      "provided with MUCR" in {

        val view = page(answers.copy(consignmentReferences = Some(ConsignmentReferences(reference = UcrType.Mucr, "ref-value"))))

        view.getElementsByClass("govuk-summary-list__value").get(answer_consignment_type) must containMessage("consignmentReferences.reference.mucr")
      }

      "provided with DUCR Part" in {

        val view = page(answers.copy(consignmentReferences = Some(ConsignmentReferences(reference = UcrType.DucrPart, "ref-value"))))

        view.getElementsByClass("govuk-summary-list__value").get(answer_consignment_type) must containMessage(
          "consignmentReferences.reference.ducrPart"
        )
      }
    }

    "render 'Arrival date and time' section in summary list" in {

      val answer_date_link_index = answer_date + 1
      val answer_time_link_index = answer_time + 1

      val view = page(answers)
      view.getElementsByClass("govuk-heading-m").get(section_arrival_datetime) must containMessage("arrivalDetails.title")

      view.getElementsByClass("govuk-summary-list__key").get(answer_date) must containMessage("summary.arrival.date")
      view.getElementsByClass("govuk-summary-list__value").get(answer_date).text mustBe date.date.format(ViewDates.dateFormatter)

      val changeDate = view.getElementsByClass("govuk-link").get(answer_date_link_index)
      changeDate must containMessage("site.change")
      changeDate must haveHref(controllers.routes.MovementDetailsController.displayPage())

      view.getElementsByClass("govuk-summary-list__key").get(answer_time) must containMessage("summary.arrival.time")
      view.getElementsByClass("govuk-summary-list__value").get(answer_time).text mustBe time.toInputFormat

      val changeTime = view.getElementsByClass("govuk-link").get(answer_time_link_index)
      changeTime must containMessage("site.change")
      changeTime must haveHref(controllers.routes.MovementDetailsController.displayPage())
    }

    "render 'Location' section in summary list" in {

      val answer_location_link_index = answer_location + 1

      val view = page(answers)
      view.getElementsByClass("govuk-heading-m").get(section_location) must containMessage("location.title")

      view.getElementsByClass("govuk-summary-list__key").get(answer_location) must containMessage("summary.goodsLocation")
      view.getElementsByClass("govuk-summary-list__value").get(answer_location).text mustBe "location-ref"

      val changeDate = view.getElementsByClass("govuk-link").get(answer_location_link_index)
      changeDate must containMessage("site.change")
      changeDate must haveHref(controllers.routes.LocationController.displayPage())
    }

    "render back button" in {

      val backButton = page(answers).getBackButton

      backButton mustBe defined
      backButton.get must haveHref(controllers.routes.LocationController.displayPage())
    }

    "render 'Confirm and submit' button on page" in {

      page(answers).getElementsByClass("govuk-button").first() must containMessage("site.confirmAndSubmit")
    }

    "render change consignment links when ileQuery disabled" in {

      when(appConfig.isIleQueryEnabled).thenReturn(false)

      val links = page(answers).getElementsByClass("govuk-link")

      links.toString must include(controllers.routes.ConsignmentReferencesController.displayPage().url)
    }

    "not render change consignment links when ileQuery enabled" in {

      when(appConfig.isIleQueryEnabled).thenReturn(true)

      val links = page(answers).getElementsByClass("govuk-link")

      links.toString must not include controllers.routes.ConsignmentReferencesController.displayPage().url
    }
  }

}
