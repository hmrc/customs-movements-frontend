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

import controllers.routes
import forms.Choice.Departure
import play.api.test.Helpers._
import testdata.MovementsTestData.cacheMapData
import uk.gov.hmrc.http.cache.client.CacheMap
import views.html.summary.departure_summary_page
import views.spec.UnitViewSpec

class DepartureSummaryViewSpec extends UnitViewSpec {

  val cachedData = cacheMapData(Departure)
  val departureSummaryPage = new departure_summary_page(mainTemplate)
  val departureSummaryView = departureSummaryPage(CacheMap("id", cachedData))(request, messages)
  val departureSummaryContent = contentAsString(departureSummaryView)

  "Departure Summary messages" should {

    "have correct content" in {

      val messages = messagesApi.preferred(request)

      messages("summary.departure.title") mustBe "Is the information provided for this departure correct?"
      messages("summary.consignmentDetails") mustBe "Consignment details"
      messages("summary.referenceType") mustBe "Consignment type"
      messages("summary.referenceValue") mustBe "Consignment reference"
      messages("summary.departure.date") mustBe "Date of departure"
      messages("summary.goodsLocation") mustBe "Goods location code"
      messages("summary.modeOfTransport") mustBe "Transport type"
      messages("summary.nationality") mustBe "Transport nationality"
      messages("transport.modeOfTransport.2") mustBe "Rail transport"
    }
  }

  "Departure Summary Page" should {

    "display same page title as header" in {

      val view = departureSummaryPage(CacheMap("id", cachedData))(request, messagesApi.preferred(request))
      view.title() must include(view.getElementsByTag("h1").text())
    }

    "have correct heading" in {

      departureSummaryView.getElementById("title").text() mustBe messages("summary.departure.title")
    }

    "have correct back link" in {

      departureSummaryView.getElementById("link-back") must haveHref(routes.TransportController.displayPage())
    }

    "have correct main buttons" in {

      departureSummaryContent must include("site.back")
      departureSummaryContent must include("site.confirmAndSubmit")
    }

    "have correct consignment references part" in {

      departureSummaryContent must include("summary.consignmentDetails")
      departureSummaryContent must include("summary.referenceType")
      departureSummaryContent must include("consignmentReferences.reference.mucr")
      departureSummaryContent must include("summary.referenceValue")
    }

    "have correct departure details part for depart out" in {

      departureSummaryContent must include("departureDetails.title")
      departureSummaryContent must include("summary.departure.date")
    }

    "have correct location part" in {

      departureSummaryContent must include("location.title")
      departureSummaryContent must include("summary.goodsLocation")
    }

    "have correct transport part for depart out" in {

      departureSummaryContent must include("transport.title")
      departureSummaryContent must include("summary.modeOfTransport")
      departureSummaryContent must include("summary.transportId")
      departureSummaryContent must include("summary.nationality")
      departureSummaryContent must include("transport.modeOfTransport.2")
    }

  }
}
