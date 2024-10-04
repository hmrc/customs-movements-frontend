/*
 * Copyright 2024 HM Revenue & Customs
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

package utils

import play.api.{Environment, Mode}
import base.UnitSpec
import models.viewmodels.decoder.ILEError

class JsonFileSpec extends UnitSpec {

  private lazy val jsonFile = new JsonFile(Environment.simple(mode = Mode.Test))

  "JsonFile getJsonArrayFromFile" should {
    "successfully read a file" when {
      "file is populated with one entry" in {
        val result = jsonFile.getJsonArrayFromFile("oneCode.json", ILEError.format)
        val expectedResult = List(ILEError("001", "English"))
        result must be(expectedResult)
      }

      "file is populated with multiple entry" in {
        val result = jsonFile.getJsonArrayFromFile("manyCodes.json", ILEError.format)
        val expectedResult = List(ILEError("001", "English"), ILEError("002", "English"), ILEError("003", "English"))

        result must be(expectedResult)
      }
    }

    "throw an exception" when {
      "file does not exist" in {
        val file = "imaginary"
        val thrown = intercept[Exception](jsonFile.getJsonArrayFromFile(file, ILEError.format))
        thrown.getMessage must be(s"$file could not be read!")
      }

      "file is empty" in {
        val file = "empty.json"
        val thrown = intercept[IllegalArgumentException](jsonFile.getJsonArrayFromFile(file, ILEError.format))
        thrown.getMessage must be(s"Failed to read JSON file: '$file'")
      }

      "one or more codes are badly formed" in {
        val file = "malformedCodes.json"
        val thrown = intercept[IllegalArgumentException](jsonFile.getJsonArrayFromFile(file, ILEError.format))
        thrown.getMessage must be(s"One or more entries could not be parsed in JSON file: '$file'")
      }
    }
  }
}
