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

package models

import forms.UcrType.Ducr
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import testdata.CommonTestData.{validDucr, validDucrPartId}

class UcrBlockSpec extends AnyWordSpec with Matchers {

  "UcrBlock on fullUcr" when {

    "provided with UCR only" should {

      "return String containing only this UCR" in {

        val ucrBlock = UcrBlock(ucr = validDucr, ucrType = Ducr.codeValue)
        val expectedResult = validDucr

        ucrBlock.fullUcr mustBe expectedResult
      }
    }

    "provided with UCR and UcrPartNo" should {

      "return String containing both elements separated by '-'" in {

        val ucrBlock = UcrBlock(ucr = validDucr, ucrPartNo = Some(validDucrPartId), ucrType = Ducr.codeValue)
        val expectedResult = s"$validDucr-$validDucrPartId"

        ucrBlock.fullUcr mustBe expectedResult
      }
    }
  }

}
