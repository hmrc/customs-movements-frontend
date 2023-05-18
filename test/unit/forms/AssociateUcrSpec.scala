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
import play.api.libs.json.Json

class AssociateUcrSpec extends UnitSpec {

  "AssociateUcr" should {

    "apply UcrBlock" when {

      "provided with Mucr" in {
        AssociateUcr.apply(UcrBlock("ucr", UcrType.Mucr)) mustBe AssociateUcr(UcrType.Mucr, "ucr")
      }

      "provided with Ducr" in {
        AssociateUcr.apply(UcrBlock("ucr", UcrType.Ducr)) mustBe AssociateUcr(UcrType.Ducr, "ucr")
      }

      "provided with Ducr Part" in {
        AssociateUcr.apply(UcrBlock("ucr", UcrType.DucrPart)) mustBe AssociateUcr(UcrType.DucrPart, "ucr")
      }
    }

    "convert to upper case" when {

      "provided with Mucr" in {
        val form = AssociateUcr.form.bind(Json.obj("kind" -> "mucr", "mucr" -> "gb/abced1234-15804test"), JsonBindMaxChars)
        form.errors mustBe empty
        form.value.map(_.ucr) must be(Some("GB/ABCED1234-15804TEST"))
      }

      "provided with Mucr that is 35 characters long" in {
        val form = AssociateUcr.form.bind(Json.obj("kind" -> "mucr", "mucr" -> "gb/abced1234-15804test1234567890123"), JsonBindMaxChars)
        form.errors mustBe empty
        form.value.map(_.ucr) must be(Some("GB/ABCED1234-15804TEST1234567890123"))
      }

      "provided with Ducr" in {
        val form = AssociateUcr.form.bind(Json.obj("kind" -> "ducr", "ducr" -> "8gb123457359100-test0001"), JsonBindMaxChars)
        form.errors mustBe empty
        form.value.map(_.ucr) must be(Some("8GB123457359100-TEST0001"))
      }
    }

    "return an error" when {

      "radio option kind not present" in {
        val form = AssociateUcr.form.bind(Json.obj("other" -> ""), JsonBindMaxChars)
        form.errors mustBe Seq(FormError("kind", List("associate.ucr.error.unselected")))
      }

      "radio option kind value neither Mucr or Ducr" in {
        val form = AssociateUcr.form.bind(Json.obj("kind" -> ""), JsonBindMaxChars)
        form.errors mustBe Seq(FormError("kind", List("associate.ucr.error.unselected")))
      }

      "provided with Mucr that is empty" in {
        val form = AssociateUcr.form.bind(Json.obj("kind" -> "mucr", "mucr" -> ""), JsonBindMaxChars)
        form.errors mustBe Seq(FormError("mucr", List("associate.ucr.mucr.error.empty")))
      }

      "provided with Mucr that is invalid" in {
        val form = AssociateUcr.form.bind(Json.obj("kind" -> "mucr", "mucr" -> "invalid"), JsonBindMaxChars)
        form.errors mustBe Seq(FormError("mucr", List("associate.ucr.mucr.error.invalid")))
      }

      "provided with Mucr that is over 35 characters long" in {
        val form = AssociateUcr.form.bind(Json.obj("kind" -> "mucr", "mucr" -> "gb/abced1234-15804test12345678901234"), JsonBindMaxChars)
        form.errors mustBe Seq(FormError("mucr", List("associate.ucr.mucr.error.invalid")))
      }

      "provided with Ducr that is empty" in {
        val form = AssociateUcr.form.bind(Json.obj("kind" -> "ducr", "ducr" -> ""), JsonBindMaxChars)
        form.errors mustBe Seq(FormError("ducr", List("associate.ucr.ducr.error.empty")))
      }

      "provided with Ducr that is invalid" in {
        val form = AssociateUcr.form.bind(Json.obj("kind" -> "ducr", "ducr" -> "invalid"), JsonBindMaxChars)
        form.errors mustBe Seq(FormError("ducr", List("associate.ucr.ducr.error.invalid")))
      }
    }
  }
}
