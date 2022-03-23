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

package models.notifications

import models.notifications.ResponseType._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsError, JsString}

class ResponseTypeSpec extends AnyWordSpec with Matchers {

  "ResponseType" should {

    "write to JSON format" when {

      "it is ControlResponse" in {
        val json = ResponseType.format.writes(ControlResponse)
        val expectedJson = JsString("inventoryLinkingControlResponse")

        json must equal(expectedJson)
      }

      "it is MovementResponse" in {
        val json = ResponseType.format.writes(MovementResponse)
        val expectedJson = JsString("inventoryLinkingMovementResponse")

        json must equal(expectedJson)
      }

      "it is MovementTotalsResponse" in {
        val json = ResponseType.format.writes(MovementTotalsResponse)
        val expectedJson = JsString("inventoryLinkingMovementTotalsResponse")

        json must equal(expectedJson)
      }
    }

    "read from JSON format" when {

      "it is ControlResponse" in {
        val responseType = ResponseType.format.reads(JsString("inventoryLinkingControlResponse")).get
        val expectedResponseType = ControlResponse

        responseType must equal(expectedResponseType)
      }

      "it is MovementResponse" in {
        val responseType = ResponseType.format.reads(JsString("inventoryLinkingMovementResponse")).get
        val expectedResponseType = MovementResponse

        responseType must equal(expectedResponseType)
      }

      "it is MovementTotalsResponse" in {
        val responseType = ResponseType.format.reads(JsString("inventoryLinkingMovementTotalsResponse")).get
        val expectedResponseType = MovementTotalsResponse

        responseType must equal(expectedResponseType)
      }

      "it is Unknown" in {
        val result = ResponseType.format.reads(JsString("inventoryLinkingSomeOtherResponse"))
        val expectedResult = JsError("Unknown ResponseType")

        result must equal(expectedResult)
      }
    }
  }

}
