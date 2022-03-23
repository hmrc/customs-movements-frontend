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

package models.notifications.queries
import java.time.ZonedDateTime

import base.UnitSpec

class UcrInfoSpec extends UnitSpec {

  "UcrInfo" should {

    "extract most recent movements transport" in {

      val transport1 = Transport(modeOfTransport = None, nationality = Some("FR"), transportId = Some("Wagon1"))
      val transport2 = Transport(modeOfTransport = None, nationality = Some("PL"), transportId = Some("Wagon2"))
      val transport3 = Transport(modeOfTransport = None, nationality = Some("DK"), transportId = Some("Wagon3"))

      val depart1 = MovementInfo(
        messageCode = "EDL",
        goodsLocation = "GBAUFDSASFDFDF",
        movementDateTime = Some(ZonedDateTime.parse("2019-10-30T09:17:00Z").toInstant),
        transportDetails = Some(transport1)
      )
      val depart2 = MovementInfo(
        messageCode = "EDL",
        goodsLocation = "GBAUFDSASFDFDF",
        movementDateTime = Some(ZonedDateTime.parse("2019-11-04T08:52:00Z").toInstant),
        transportDetails = Some(transport2)
      )
      val depart3 = MovementInfo(
        messageCode = "EDL",
        goodsLocation = "GBAUFDSASFDFDF",
        movementDateTime = Some(ZonedDateTime.parse("2019-10-04T02:51:00Z").toInstant),
        transportDetails = Some(transport3)
      )
      val arrival = MovementInfo(
        messageCode = "EAL",
        goodsLocation = "GBAUFDSASFDFDF",
        movementDateTime = Some(ZonedDateTime.parse("2020-10-30T09:17:00Z").toInstant)
      )

      MucrInfo("ucr", movements = Seq(depart1, depart2, arrival, depart3)).transport must be(Some(transport2))
    }
  }

}
