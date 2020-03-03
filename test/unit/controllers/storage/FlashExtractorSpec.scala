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

package controllers.storage

import base.UnitSpec
import models.cache.JourneyType
import play.api.test.FakeRequest

class FlashExtractorSpec extends UnitSpec {

  private val flashValuesExtractor = new FlashExtractor

  "FlashValuesExtractor on extractValue" when {

    "the flash has no given key" should {
      "return None" in {

        val key = "ANY_FLASH_KEY"
        val request = FakeRequest().withFlash()

        flashValuesExtractor.extractValue(key, request) mustBe None
      }
    }

    "the flash contains given key" should {
      "return corresponding value" in {

        val key = "ANY_FLASH_KEY"
        val request = FakeRequest().withFlash(key -> "ANY_VALUE")

        flashValuesExtractor.extractValue(key, request) mustBe Some("ANY_VALUE")
      }
    }
  }

  "FlashValuesExtractor on extractMovementType" when {

    "the flash has no MOVEMENT_TYPE key" should {
      "return None" in {

        val request = FakeRequest().withFlash()

        flashValuesExtractor.extractMovementType(request) mustBe None
      }
    }

    "the flash contains MOVEMENT_TYPE key" should {
      "return corresponding JourneyType" in {

        val request = FakeRequest().withFlash(FlashKeys.MOVEMENT_TYPE -> JourneyType.ARRIVE.toString)

        flashValuesExtractor.extractMovementType(request) mustBe Some(JourneyType.ARRIVE)
      }
    }
  }

}
