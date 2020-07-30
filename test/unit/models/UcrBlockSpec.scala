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

package models

import forms.UcrType.{Ducr, DucrPart}
import org.scalatest.{MustMatchers, WordSpec}
import testdata.CommonTestData.validDucr

class UcrBlockSpec extends WordSpec with MustMatchers {

  "UcrBlock" when {

    "provided with UCRType code" should {

      "create UcrBlock" in {

        val ucrBlock = UcrBlock(ucr = validDucr, ucrType = Ducr.codeValue)

        ucrBlock.ucr mustBe validDucr
        ucrBlock.ucrType mustBe Ducr.codeValue
      }
    }

    "provided with UCRType" should {

      "create UcrBlock" in {

        val ucrBlock = UcrBlock(ucr = validDucr, ucrType = DucrPart)

        ucrBlock.ucr mustBe validDucr
        ucrBlock.ucrType mustBe DucrPart.codeValue
      }
    }
  }

}
