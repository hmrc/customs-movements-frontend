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
import forms.GoodsDeparted
import forms.GoodsDeparted.AllowedPlaces
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.test.Helpers._
import testdata.MovementsTestData.cacheMapData
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.{Injector, Stubs}
import views.base.UnitViewSpec
import views.html.summary.departure_summary_page

class DepartureSummaryViewSpec extends UnitViewSpec with Stubs with Injector {

  val cachedDataOutOfUk = cacheMapData(Departure)
  val cachedDataBackIntoUk = cacheMapData(Departure) + (GoodsDeparted.formId -> Json.toJson(
    GoodsDeparted(AllowedPlaces.backIntoTheUk)
  ))
  val departureSummaryPage = new departure_summary_page(mainTemplate)
  val departureSummaryViewOut = departureSummaryPage(CacheMap("id", cachedDataOutOfUk))(request, messages)
  val departureSummaryContentOut = contentAsString(departureSummaryViewOut)
  val departureSummaryViewIn = departureSummaryPage(CacheMap("id", cachedDataBackIntoUk))(request, messages)
  val departureSummaryContentIn = contentAsString(departureSummaryViewIn)

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
      messages("summary.nationality") mustBe "Nationality"
      messages("goodsDeparted.departedPlace.outOfTheUk") mustBe "Out of the UK"
      messages("goodsDeparted.departedPlace.backIntoTheUk") mustBe "Back into the UK"
      messages("transport.modeOfTransport.2") mustBe "Rail transport"
    }
  }

  "Departure Summary Page" should {

    "have correct title" in {

      departureSummaryViewOut.getElementsByTag("title").text() mustBe messages("summary.title")
    }

    "have correct heading" in {

      departureSummaryViewOut.getElementById("title").text() mustBe messages("summary.departure.title")
    }

    "have correct back link for depart out of uk" in {

      departureSummaryViewOut.getElementById("link-back") must haveHref(routes.TransportController.displayPage())
    }

    "have correct back link for depart into of uk" in {

      departureSummaryViewIn.getElementById("link-back") must haveHref(routes.MovementDetailsController.displayPage())
    }

    "have correct main buttons" in {

      departureSummaryContentOut must include("site.back")
      departureSummaryContentOut must include("site.acceptAndSend")
    }

    "have correct consignment references part" in {

      departureSummaryContentOut must include("consignmentReferences.title")
      departureSummaryContentOut must include("summary.referenceType")
      departureSummaryContentOut must include("consignmentReferences.reference.mucr")
      departureSummaryContentOut must include("summary.referenceValue")
    }

    "have correct departure details part for depart out" in {

      departureSummaryContentOut must include("departureDetails.title")
      departureSummaryContentOut must include("summary.departure.date")
      departureSummaryContentOut must include("summary.departure.goodsDeparted")
      departureSummaryContentOut must include("goodsDeparted.departedPlace.outOfTheUk")
    }

    "have correct departure details part for depart in" in {

      departureSummaryContentIn must include("departureDetails.title")
      departureSummaryContentIn must include("summary.departure.date")
      departureSummaryContentIn must include("summary.departure.goodsDeparted")
      departureSummaryContentIn must include("goodsDeparted.departedPlace.backIntoTheUk")
    }

    "have correct location part" in {

      departureSummaryContentOut must include("location.title")
      departureSummaryContentOut must include("summary.goodsLocation")
    }

    "have correct transport part for depart out" in {

      departureSummaryContentOut must include("transport.title")
      departureSummaryContentOut must include("summary.modeOfTransport")
      departureSummaryContentOut must include("summary.transportId")
      departureSummaryContentOut must include("summary.nationality")
      departureSummaryContentOut must include("transport.modeOfTransport.2")
    }

    "not have correct transport part for depart in" in {

      departureSummaryContentIn must not include ("transport.title")
      departureSummaryContentIn must not include ("summary.modeOfTransport")
      departureSummaryContentIn must not include ("summary.transportId")
      departureSummaryContentIn must not include ("summary.nationality")
    }
  }
}
