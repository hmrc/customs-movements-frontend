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
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.HttpFilters

class ApplicationFiltersIntegrationTest extends WordSpec with GuiceOneAppPerSuite with MustMatchers {

  "Application filter" should {
    "contains whitespace filter" in {
      atLeast(1, app.injector.instanceOf[HttpFilters].filters) mustBe a[WhitelistIpFilter]
    }
  }

}
