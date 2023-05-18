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

package forms

import base.UnitSpec
import models.UcrBlock
import play.api.data.FormError
import testdata.CommonTestData.{validDucr, validDucrPartId, validWholeDucrParts}

class DucrPartDetailsSpec extends UnitSpec {

  val ducr = s" $validDucr "
  val ducrPartId = s" $validDucrPartId "

  "DucrPartDetails mapping" should {

    "return errors" when {

      "provided with empty DUCR" in {
        val input = Map("ducr" -> "", "ducrPartId" -> "")
        val result = DucrPartDetails.mapping.bind(input)
        result.isLeft mustBe true
        result.swap.getOrElse(Seq.empty[FormError]).size mustBe 2
        result.swap.getOrElse(Seq.empty[FormError]) must contain(FormError("ducr", Seq("ducrPartDetails.ducr.empty")))
      }

      "provided with incorrect DUCR" in {
        val input = Map("ducr" -> "incorrect!@#$%^", "ducrPartId" -> "incorrect!@#$%^")
        val result = DucrPartDetails.mapping.bind(input)
        result.isLeft mustBe true
        result.swap.getOrElse(Seq.empty[FormError]).size mustBe 2
        result.swap.getOrElse(Seq.empty[FormError]) must contain(FormError("ducr", Seq("ducrPartDetails.ducr.invalid")))
      }

      "provided with empty DUCR Part ID" in {
        val input = Map("ducr" -> validDucr, "ducrPartId" -> "")
        val result = DucrPartDetails.mapping.bind(input)
        result.isLeft mustBe true
        result.swap.getOrElse(Seq.empty[FormError]).size mustBe 1
        result.swap.getOrElse(Seq.empty[FormError]) must contain(FormError("ducrPartId", Seq("ducrPartDetails.ducrPartId.empty")))
      }

      "provided with incorrect DUCR Part ID" in {
        val input = Map("ducr" -> validDucr, "ducrPartId" -> "incorrect!@#$%^")
        val result = DucrPartDetails.mapping.bind(input)
        result.isLeft mustBe true
        result.swap.getOrElse(Seq.empty[FormError]).size mustBe 1
        result.swap.getOrElse(Seq.empty[FormError]) must contain(FormError("ducrPartId", Seq("ducrPartDetails.ducrPartId.invalid")))
      }
    }

    "return no errors" when {

      "provided with correct both DUCR and DUCR Part ID" in {
        val input = Map("ducr" -> ducr, "ducrPartId" -> ducrPartId)
        val result = DucrPartDetails.mapping.bind(input)
        result.isRight mustBe true
      }

      "provided with correct lower cased both DUCR and DUCR Part ID" in {
        val input = Map("ducr" -> ducr.toLowerCase, "ducrPartId" -> ducrPartId.toLowerCase)
        val result = DucrPartDetails.mapping.bind(input)
        result.isRight mustBe true
      }
    }

    "convert to upper case" when {

      "provided with DUCR containing lower case characters" in {
        val input = Map("ducr" -> ducr.toLowerCase, "ducrPartId" -> ducrPartId.toLowerCase)
        val result = DucrPartDetails.mapping.bind(input)
        result.isRight mustBe true
        result.getOrElse(DucrPartDetails("", "")).ducr mustBe validDucr.toUpperCase
      }

      "provided with DUCR Part ID containing lower case characters" in {
        val input = Map("ducr" -> ducr.toLowerCase, "ducrPartId" -> ducrPartId.toLowerCase)
        val result = DucrPartDetails.mapping.bind(input)
        result.isRight mustBe true
        result.getOrElse(DucrPartDetails("", "")).ducrPartId mustBe validDucrPartId.toUpperCase
      }
    }
  }

  "DucrPartDetails on toUcrBlock" should {

    "return UcrBlock with correct type field" in {
      val ducrPartDetails = DucrPartDetails(ducr = validDucr, ducrPartId = validDucrPartId)
      val expectedType = UcrType.DucrPart.codeValue
      ducrPartDetails.toUcrBlock.ucrType mustBe expectedType
    }

    "return UcrBlock with correct ucr field" in {
      val ducrPartDetails = DucrPartDetails(ducr = validDucr, ducrPartId = validDucrPartId)
      val expectedUcr = validWholeDucrParts
      ducrPartDetails.toUcrBlock.ucr mustBe expectedUcr
    }
  }

  "DucrPartDetails on apply" should {

    "throw IllegalArgumentException" when {
      "provided with UcrBlock of type different than DucrParts" in {
        val ucrBlock = UcrBlock(ucrType = UcrType.Ducr, ucr = validDucr)
        intercept[IllegalArgumentException](DucrPartDetails(ucrBlock))
      }
    }

    "return DucrPartDetails with correct ducr" in {
      val ucrBlock = UcrBlock(ucrType = UcrType.DucrPart, ucr = validWholeDucrParts)
      val expectedDucr = validDucr
      DucrPartDetails(ucrBlock).ducr mustBe expectedDucr
    }

    "return DucrPartDetails with correct ducrPartId" in {
      val ucrBlock = UcrBlock(ucrType = UcrType.DucrPart, ucr = validWholeDucrParts)
      val expectedDucrPartId = validDucrPartId
      DucrPartDetails(ucrBlock).ducrPartId mustBe expectedDucrPartId
    }
  }
}
