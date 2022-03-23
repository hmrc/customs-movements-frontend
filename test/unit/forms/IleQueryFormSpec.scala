/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString}

class IleQueryFormSpec extends BaseSpec {

  "IleQueryForm" should {

    "convert ducr to upper case" in {

      val form = IleQueryForm.form.bind(JsObject(Map("ucr" -> JsString("8gb123457359100-test0001"))), JsonBindMaxChars)

      form.errors mustBe (empty)
      form.value must be(Some("8GB123457359100-TEST0001"))
    }

    "convert mucr to upper case" in {

      val form = IleQueryForm.form.bind(JsObject(Map("ucr" -> JsString("gb/abced1234-15804test"))), JsonBindMaxChars)

      form.errors mustBe (empty)
      form.value must be(Some("GB/ABCED1234-15804TEST"))
    }

    "convert mucr to upper case when is 35 characters long" in {

      val form = IleQueryForm.form.bind(JsObject(Map("ucr" -> JsString("gb/82f9-0n2f6500040010tO120p0a30998"))), JsonBindMaxChars)

      form.errors mustBe (empty)
      form.value must be(Some("GB/82F9-0N2F6500040010TO120P0A30998"))
    }

    "display error when mucr is over 35 characters long" in {

      val form = IleQueryForm.form.bind(JsObject(Map("ucr" -> JsString("gb/82f9-0n2f6500040010tO120p0a309989"))), JsonBindMaxChars)

      form.errors must be(Seq(FormError("ucr", "ileQuery.ucr.incorrect")))
    }
  }

}
