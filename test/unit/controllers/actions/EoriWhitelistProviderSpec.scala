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

package unit.controllers.actions

import com.typesafe.config.ConfigException
import controllers.actions.{EoriWhitelist, EoriWhitelistProvider}
import play.api.Configuration
import unit.base.UnitSpec

class EoriWhitelistProviderSpec extends UnitSpec {

  "EoriWhitelistProvider" should {
    "create list from configuration" in {
      val config = Configuration("whitelist.eori.0" -> "12345")
      val provider = new EoriWhitelistProvider(config)
      provider.get() mustBe a[EoriWhitelist]
    }
    "throw exception when there is not whitelist.eori key" in {
      val provider = new EoriWhitelistProvider(Configuration.empty)
      a[ConfigException.Missing] mustBe thrownBy {
        provider.get()
      }
    }
  }

}
