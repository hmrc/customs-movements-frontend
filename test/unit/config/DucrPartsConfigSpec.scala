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

package config

import base.UnitSpec
import com.typesafe.config.{Config, ConfigFactory}
import play.api.Configuration

class DucrPartsConfigSpec extends UnitSpec {

  "DucrPartsConfig on isDucrPartsEnabled" should {

    "return true" when {

      "FeatureSwitchConfig returns FeatureStatus enabled" in {

        val config: Config = ConfigFactory.parseString("microservice.services.features.ducrParts=enabled")
        val featureSwitchConfig = new FeatureSwitchConfig(Configuration(config))
        val ducrPartsConfig = new DucrPartsConfig(featureSwitchConfig)

        ducrPartsConfig.isDucrPartsEnabled mustBe true
      }
    }

    "return false" when {

      "FeatureSwitchConfig returns FeatureStatus disabled" in {

        val config: Config = ConfigFactory.parseString("microservice.services.features.ducrParts=disabled")
        val featureSwitchConfig = new FeatureSwitchConfig(Configuration(config))
        val ducrPartsConfig = new DucrPartsConfig(featureSwitchConfig)

        ducrPartsConfig.isDucrPartsEnabled mustBe false
      }
    }
  }
}
