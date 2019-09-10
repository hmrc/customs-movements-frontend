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
import views.html.summary.arrival_summary_page

class ArrivalSummaryViewSpec extends UnitViewSpec with Stubs with Injector {

  val cachedData = cacheMapData("EAL")
  val arrivalSummaryPage = new arrival_summary_page(mainTemplate)
  val arrivalSummaryView = arrivalSummaryPage(CacheMap("id", cachedData))(request, messages)
  val arrivalSummaryContent = contentAsString(arrivalSummaryView)

  "Arrival Summary messages" should {

    "have correct content" in {

      val messages = instanceOf[MessagesApi].preferred(request)

      messages("summary.title") mustBe "Check your answers"
      messages("summary.arrival.title") mustBe "Check your answers before arriving these goods"
      messages("summary.referenceType") mustBe "Reference Type"
      messages("summary.referenceValue") mustBe "Reference Value"
      messages("summary.arrivalReference.reference") mustBe "Reference"
      messages("summary.arrival.date") mustBe "Date of Arrival"
      messages("summary.arrival.time") mustBe "Time of Arrival"
      messages("summary.goodsLocation") mustBe "Goods Location"
      messages("summary.modeOfTransport") mustBe "Mode of transport"
      messages("summary.nationality") mustBe "Nationality"
    }
  }

  "Arrival Summary Page" should {

    "have correct title" in {

      arrivalSummaryView.getElementsByTag("title").text() mustBe messages("summary.title")
    }

    "have correct heading" in {

      arrivalSummaryView.getElementById("title").text() mustBe messages("summary.arrival.title")
    }

    "have correct main buttons" in {

      arrivalSummaryContent must include("site.back")
      arrivalSummaryContent must include("site.acceptAndSend")
    }

    "have correct consignment references part" in {

      arrivalSummaryContent must include("consignmentReferences.title")
      arrivalSummaryContent must include("summary.referenceType")
      arrivalSummaryContent must include("consignmentReferences.reference.mucr")
      arrivalSummaryContent must include("summary.referenceValue")
    }

    "have correct arrival reference part" in {

      arrivalSummaryContent must include("arrivalReference")
      arrivalSummaryContent must include("summary.arrivalReference.reference")
    }

    "have correct arrival details part" in {

      arrivalSummaryContent must include("arrivalDetails.title")
      arrivalSummaryContent must include("summary.arrival.date")
      arrivalSummaryContent must include("summary.arrival.time")
    }

    "have correct location part" in {

      arrivalSummaryContent must include("location.title")
      arrivalSummaryContent must include("summary.goodsLocation")
    }

    "have correct transport part" in {

      arrivalSummaryContent must include("transport.title")
      arrivalSummaryContent must include("summary.modeOfTransport")
      arrivalSummaryContent must include("summary.nationality")
    }
  }
}

