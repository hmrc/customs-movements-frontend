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

package views.components.ilequery

import java.time.ZonedDateTime

import base.Injector
import models.notifications.queries.MovementInfo
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import views.ViewSpec
import views.html.components.ilequery.response_previous_movements
import views.tags.ViewTest

@ViewTest
class PreviousMovementsViewSpec extends ViewSpec with Injector {

  private implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val page = instanceOf[response_previous_movements]

  private def view(movements: Seq[MovementInfo] = Seq.empty) = page(movements)

  val arrival =
    MovementInfo(messageCode = "EAL", goodsLocation = "GBAUFXTFXTFXT", movementDateTime = Some(ZonedDateTime.parse("2019-10-23T12:34:18Z").toInstant))
  val retro =
    MovementInfo(messageCode = "RET", goodsLocation = "GBAUDFGFSHFKD", movementDateTime = Some(ZonedDateTime.parse("2019-11-04T16:27:18Z").toInstant))
  val depart = MovementInfo(
    messageCode = "EDL",
    goodsLocation = "GBAUFDSASFDFDF",
    movementDateTime = Some(ZonedDateTime.parse("2019-10-30T09:17:18Z").toInstant)
  )

  "Previous movements " should {

    "not render for empty movement list" in {

      view().text() mustBe ""
    }

    "render arrival movement" in {
      val arrivalView = view(movements = Seq(arrival))
      arrivalView.getElementById("movement_type_0") must containMessage("ileQueryResponse.previousMovements.type.eal")
      arrivalView.getElementById("movement_date_0").text() must be("23 October 2019 at 1:34pm")
      arrivalView.getElementById("goods_location_0").text() must be("GBAUFXTFXTFXT")
    }

    "render departure movement" in {
      val arrivalView = view(movements = Seq(depart))
      arrivalView.getElementById("movement_type_0") must containMessage("ileQueryResponse.previousMovements.type.edl")
      arrivalView.getElementById("movement_date_0").text() must be("30 October 2019 at 9:17am")
      arrivalView.getElementById("goods_location_0").text() must be("GBAUFDSASFDFDF")
    }

    "render retrospective arrival" in {
      val arrivalView = view(movements = Seq(retro))
      arrivalView.getElementById("movement_type_0") must containMessage("ileQueryResponse.previousMovements.type.ret")
      arrivalView.getElementById("movement_date_0").text() must be("4 November 2019 at 4:27pm")
      arrivalView.getElementById("goods_location_0").text() must be("GBAUDFGFSHFKD")
    }

    "render movements by date order" in {
      val movementsView = view(movements = Seq(arrival, retro, depart))
      movementsView.getElementById("movement_date_0").text() must be("4 November 2019 at 4:27pm")
      movementsView.getElementById("movement_date_1").text() must be("30 October 2019 at 9:17am")
      movementsView.getElementById("movement_date_2").text() must be("23 October 2019 at 1:34pm")
    }

  }
}
