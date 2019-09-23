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

import controllers.actions.EoriWhitelist
import models.SignedInUser
import testdata.MovementsTestData._
import unit.base.UnitSpec

class EoriWhitelistSpec extends UnitSpec {

  val firstUser: SignedInUser = newUser("1234")

  val secondUser: SignedInUser = newUser("0986")

  "EORI whitelist" when {
    "is empty" should {
      "pass everyone" in {
        val whitelist = new EoriWhitelist(Seq.empty)
        whitelist.allow(firstUser) mustBe true
        whitelist.allow(secondUser) mustBe true
      }
    }
    "has entry in list" should {
      val whitelist = new EoriWhitelist(Seq("1234"))
      "allow users on list" in {
        whitelist.allow(firstUser) mustBe true
      }
      "block user absent on list" in {
        whitelist.allow(secondUser) mustBe false
      }
    }
  }
}
