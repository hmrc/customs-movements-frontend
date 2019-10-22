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
import forms.Choice.Arrival
import play.api.test.Helpers._
import testdata.MovementsTestData.cacheMapData
import uk.gov.hmrc.http.cache.client.CacheMap
import views.html.summary.arrival_summary_page
import views.spec.UnitViewSpec

class ArrivalSummaryViewSpec extends UnitViewSpec {

  val cachedData = cacheMapData(Arrival)
  val arrivalSummaryPage = new arrival_summary_page(mainTemplate)
  val arrivalSummaryView = arrivalSummaryPage(CacheMap("id", cachedData))(request, messages)
  val arrivalSummaryContent = contentAsString(arrivalSummaryView)

  "Arrival Summary messages" should {

    "have correct content" in {

      val messages = messagesApi.preferred(request)

      messages("summary.arrival.title") mustBe "Is the information provided for this arrival correct?"
      messages("summary.consignmentDetails") mustBe "Consignment details"
      messages("summary.referenceType") mustBe "Consignment type"
      messages("summary.referenceValue") mustBe "Consignment reference"
      messages("summary.arrivalReference.reference") mustBe "Unique reference"
      messages("summary.arrival.date") mustBe "Date of arrival"
      messages("summary.arrival.time") mustBe "Time of arrival"
      messages("summary.goodsLocation") mustBe "Goods location code"
    }
  }

  "Arrival Summary Page" should {

    "display same page title as header" in {

      val view = arrivalSummaryPage(CacheMap("id", cachedData))(request, messagesApi.preferred(request))
      view.title() must include(view.getElementsByTag("h1").text())
    }

    "have correct heading" in {

      arrivalSummaryView.getElementById("title").text() mustBe messages("summary.arrival.title")
    }

    "have correct back link" in {

      arrivalSummaryView.getElementById("link-back") must haveHref(routes.LocationController.displayPage())
    }

    "have correct main buttons" in {

      arrivalSummaryContent must include("site.back")
      arrivalSummaryContent must include("site.confirmAndSubmit")
    }

    "have correct consignment references part" in {

      arrivalSummaryContent must include("summary.consignmentDetails")
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
  }
}
