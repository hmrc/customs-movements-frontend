/*
 * Copyright 2021 HM Revenue & Customs
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

import base.BaseSpec
import play.api.libs.json.{JsObject, JsString}

class MucrOptionsSpec extends BaseSpec {

  "MucrOptions" should {

    "convert new mucr to upper case" in {

      val form = MucrOptions.form.bind(
        JsObject(Map("createOrAdd" -> JsString("create"), "newMucr" -> JsString("gb/abced1234-15804test"), "existingMucr" -> JsString("")))
      )

      form.errors mustBe (empty)
      form.value.map(_.newMucr) must be(Some("GB/ABCED1234-15804TEST"))
    }

    "convert existing mucr to upper case" in {

      val form = MucrOptions.form.bind(
        JsObject(Map("createOrAdd" -> JsString("add"), "newMucr" -> JsString(""), "existingMucr" -> JsString("gb/abced1234-15804test")))
      )

      form.errors mustBe (empty)
      form.value.map(_.existingMucr) must be(Some("GB/ABCED1234-15804TEST"))
    }
  }

}
