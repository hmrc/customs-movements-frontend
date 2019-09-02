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

package filters

import org.scalatest.{MustMatchers, WordSpec}
import play.api.Configuration
import play.api.test.NoMaterializer

class WhitelistIPFilterProviderSpec extends WordSpec with MustMatchers {

  val configuration = Configuration(
    "ip-whitelist.enabled" -> "true",
    "ip-whitelist.list.0" -> "127.0.0.1",
    "ip-whitelist.exclude.0" -> "/healthcheck",
    "ip-whitelist.exclude.1" -> "/ping/ping"
  )

  val provider = new WhitelistIpFilterProvider(configuration, NoMaterializer)

  "WhitelistIPFilter Provider" should {
    "load ip whitelist from configuration key" in {
      val filter = provider.get()
      filter.enabled mustBe true
      filter.whitelist mustEqual Seq("127.0.0.1")
      filter.excludeList must contain("/healthcheck")
      filter.excludeList must contain("/ping/ping")
    }
  }

}
