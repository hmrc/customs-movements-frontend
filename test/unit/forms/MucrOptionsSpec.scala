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
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString}

class MucrOptionsSpec extends UnitSpec {

  "MucrOptions" should {

    "convert new mucr to upper case" in {

      val form = MucrOptions.form.bind(
        JsObject(Map("createOrAdd" -> JsString("create"), "newMucr" -> JsString("gb/abced1234-15804test"), "existingMucr" -> JsString(""))),
        JsonBindMaxChars
      )

      form.errors mustBe empty
      form.value.map(_.mucr) must be(Some("GB/ABCED1234-15804TEST"))
    }

    "convert existing mucr to upper case" in {

      val form = MucrOptions.form.bind(
        JsObject(Map("createOrAdd" -> JsString("add"), "newMucr" -> JsString(""), "existingMucr" -> JsString("gb/abced1234-15804test"))),
        JsonBindMaxChars
      )

      form.errors mustBe empty
      form.value.map(_.mucr) must be(Some("GB/ABCED1234-15804TEST"))
    }

    "return an error" when {
      "radio option createOrAdd not present" in {
        val form = MucrOptions.form.bind(JsObject(Map("other" -> JsString(""))), JsonBindMaxChars)

        form.errors mustBe Seq(FormError("createOrAdd", List("mucrOptions.error.unselected")))
      }

      "radio option createOrAdd value neither create or add" in {
        val form = MucrOptions.form.bind(JsObject(Map("createOrAdd" -> JsString(""))), JsonBindMaxChars)

        form.errors mustBe Seq(FormError("createOrAdd", List("mucrOptions.error.unselected")))
      }

      "provided with newMucr that is empty" in {
        val form = MucrOptions.form.bind(JsObject(Map("createOrAdd" -> JsString("create"), "newMucr" -> JsString(""))), JsonBindMaxChars)

        form.errors mustBe Seq(FormError("newMucr", List("mucrOptions.reference.value.error.empty")))
      }

      "provided with newMucr that is invalid" in {
        val form = MucrOptions.form.bind(JsObject(Map("createOrAdd" -> JsString("create"), "newMucr" -> JsString("invalid"))), JsonBindMaxChars)

        form.errors mustBe Seq(FormError("newMucr", List("mucrOptions.reference.value.error.invalid")))
      }

      "provided with existingMucr that is empty" in {
        val form = MucrOptions.form.bind(JsObject(Map("createOrAdd" -> JsString("add"), "existingMucr" -> JsString(""))), JsonBindMaxChars)

        form.errors mustBe Seq(FormError("existingMucr", List("mucrOptions.reference.value.error.empty")))
      }

      "provided with existingMucr that is invalid" in {
        val form = MucrOptions.form.bind(JsObject(Map("createOrAdd" -> JsString("add"), "existingMucr" -> JsString("invalid"))), JsonBindMaxChars)

        form.errors mustBe Seq(FormError("existingMucr", List("mucrOptions.reference.value.error.invalid")))
      }
    }
  }
}
