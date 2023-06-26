/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.actions

import base.UnitSpec
import models.SignedInUser
import play.api.Configuration
import testdata.MovementsTestData._

class EoriAllowListSpec extends UnitSpec {

  val firstUser: SignedInUser = newUser("1234")

  val secondUser: SignedInUser = newUser("0986")

  val config = Configuration("allowList.eori.0" -> "1234")

  val emptyListConfig = Configuration("allowList.eori" -> Seq.empty)

  "EORI allow list" when {
    "is empty" should {
      "pass everyone" in {
        val allowList = new EoriAllowList(emptyListConfig)
        allowList.allows(firstUser.eori) mustBe true
        allowList.allows(secondUser.eori) mustBe true
      }
    }
    "has entry in list" should {
      val allowList = new EoriAllowList(config)
      "allow users on list" in {
        allowList.allows(firstUser.eori) mustBe true
      }
      "block user absent on list" in {
        allowList.allows(secondUser.eori) mustBe false
      }
    }
  }
}
