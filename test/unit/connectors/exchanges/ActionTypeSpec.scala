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

package connectors.exchanges

import connectors.exchanges.ActionType.ConsolidationType._
import connectors.exchanges.ActionType.MovementType._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsError, JsString}

class ActionTypeSpec extends AnyWordSpec with Matchers {

  "ActionType" should {

    "write to JSON format" when {

      "it is Arrival" in {
        val json = ActionType.format.writes(Arrival)
        val expectedJson = JsString("Arrival")

        json must equal(expectedJson)
      }

      "it is Departure" in {
        val json = ActionType.format.writes(Departure)
        val expectedJson = JsString("Departure")

        json must equal(expectedJson)
      }

      "it is DUCR Association" in {
        val json = ActionType.format.writes(DucrAssociation)
        val expectedJson = JsString("DucrAssociation")

        json must equal(expectedJson)
      }

      "it is MUCR Association" in {
        val json = ActionType.format.writes(MucrAssociation)
        val expectedJson = JsString("MucrAssociation")

        json must equal(expectedJson)
      }

      "it is DUCR Parts Association" in {
        val json = ActionType.format.writes(DucrPartAssociation)
        val expectedJson = JsString("DucrPartAssociation")

        json must equal(expectedJson)
      }

      "it is DUCR Disassociation" in {
        val json = ActionType.format.writes(DucrDisassociation)
        val expectedJson = JsString("DucrDisassociation")

        json must equal(expectedJson)
      }

      "it is MUCR Disassociation" in {
        val json = ActionType.format.writes(MucrDisassociation)
        val expectedJson = JsString("MucrDisassociation")

        json must equal(expectedJson)
      }

      "it is DUCR Parts Disassociation" in {
        val json = ActionType.format.writes(DucrPartDisassociation)
        val expectedJson = JsString("DucrPartDisassociation")

        json must equal(expectedJson)
      }

      "it is Shut MUCR" in {
        val json = ActionType.format.writes(ShutMucr)
        val expectedJson = JsString("ShutMucr")

        json must equal(expectedJson)
      }
    }

    "read from Json format" when {

      "it is Arrival" in {
        val actionType = ActionType.format.reads(JsString("Arrival")).get

        actionType must equal(Arrival)
      }

      "it is Departure" in {
        val actionType = ActionType.format.reads(JsString("Departure")).get

        actionType must equal(Departure)
      }

      "it is DUCR Association" in {
        val actionType = ActionType.format.reads(JsString("DucrAssociation")).get

        actionType must equal(DucrAssociation)
      }

      "it is MUCR Association" in {
        val actionType = ActionType.format.reads(JsString("MucrAssociation")).get

        actionType must equal(MucrAssociation)
      }

      "it is DUCR Part Association" in {
        val actionType = ActionType.format.reads(JsString("DucrPartAssociation")).get

        actionType must equal(DucrPartAssociation)
      }

      "it is DUCR Disassociation" in {
        val actionType = ActionType.format.reads(JsString("DucrDisassociation")).get

        actionType must equal(DucrDisassociation)
      }

      "it is MUCR Disassociation" in {
        val actionType = ActionType.format.reads(JsString("MucrDisassociation")).get

        actionType must equal(MucrDisassociation)
      }

      "it is DUCR Part Disassociation" in {
        val actionType = ActionType.format.reads(JsString("DucrPartDisassociation")).get

        actionType must equal(DucrPartDisassociation)
      }

      "it is Shut MUCR" in {
        val actionType = ActionType.format.reads(JsString("ShutMucr")).get

        actionType must equal(ShutMucr)
      }

      "it is Unknown" in {
        val result = ActionType.format.reads(JsString("Unknown"))
        val expectedResult = JsError("Unknown ActionType: [\"Unknown\"]")

        result must equal(expectedResult)
      }
    }
  }
}
