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

import play.api.i18n.MessagesApi
import play.api.test.Helpers._
import testdata.MovementsTestData.cacheMapData
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.{Injector, Stubs}
import views.base.UnitViewSpec
import views.html.summary.departure_summary_page

class DepartureSummaryViewSpec extends UnitViewSpec with Stubs with Injector {

  val cachedData = cacheMapData("EDL")
  val departureSummaryPage = new departure_summary_page(mainTemplate)
  val departureSummaryView = departureSummaryPage(CacheMap("id", cachedData))(request, messages)
  val departureSummaryContent = contentAsString(departureSummaryView)

  "Departure Summary messages" should {

    "have correct content" in {

      val messages = instanceOf[MessagesApi].preferred(request)

      messages("summary.title") mustBe "Check your answers"
      messages("summary.departure.title") mustBe "Check your answers before departing these goods"
      messages("summary.referenceType") mustBe "Reference Type"
      messages("summary.referenceValue") mustBe "Reference Value"
      messages("summary.departure.date") mustBe "Date of Departure"
      messages("summary.goodsLocation") mustBe "Goods Location"
      messages("summary.modeOfTransport") mustBe "Mode of transport"
      messages("summary.nationality") mustBe "Nationality"
    }
  }

  "Departure Summary Page" should {

    "have correct title" in {

      departureSummaryView.getElementsByTag("title").text() mustBe messages("summary.title")
    }

    "have correct heading" in {

      departureSummaryView.getElementById("title").text() mustBe messages("summary.departure.title")
    }

    "have correct main buttons" in {

      departureSummaryContent must include("site.back")
      departureSummaryContent must include("site.acceptAndSend")
    }

    "have correct consignment references part" in {

      departureSummaryContent must include("consignmentReferences.title")
      departureSummaryContent must include("summary.referenceType")
      departureSummaryContent must include("consignmentReferences.reference.mucr")
      departureSummaryContent must include("summary.referenceValue")
    }

    "have correct departure details part" in {

      departureSummaryContent must include("departureDetails.title")
      departureSummaryContent must include("summary.departure.date")
    }

    "have correct location part" in {

      departureSummaryContent must include("location.title")
      departureSummaryContent must include("summary.goodsLocation")
    }

    "have correct transport part" in {

      departureSummaryContent must include("transport.title")
      departureSummaryContent must include("summary.modeOfTransport")
      departureSummaryContent must include("summary.nationality")
    }
  }
}
