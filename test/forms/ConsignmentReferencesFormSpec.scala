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

package forms

import base.BaseSpec
import forms.ConsignmentReferencesForm.bindFromRequest
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString}
import play.api.test.FakeRequest

class ConsignmentReferencesFormSpec extends BaseSpec {

  private def request(ducrOrMucr: String, mucr: String = "", ducr: String = "") =
    FakeRequest().withJsonBody(JsObject(Map("reference" -> JsString(ducrOrMucr), "ducrValue" -> JsString(ducr), "mucrValue" -> JsString(mucr))))
  private def requestMucr(value: String) = request("M", mucr = value)
  private def requestDucr(value: String) = request("D", ducr = value)

  val validDucrRequest = requestDucr("9GB123456")
  val validMucrRequest = requestMucr("GB/ABC-12342")

  "Consignment References Form" should {
    "have no errors for valid request" in {
      bindFromRequest(validDucrRequest).errors must be(Seq.empty)
      bindFromRequest(validMucrRequest).errors must be(Seq.empty)
    }

    "have error for missing reference" in {
      bindFromRequest(requestDucr("")).errors must be(Seq(FormError("ducrValue", "consignmentReferences.reference.ducrValue.empty")))
      bindFromRequest(requestMucr("")).errors must be(Seq(FormError("mucrValue", "consignmentReferences.reference.mucrValue.empty")))
    }

    "have error for invalid reference" in {
      bindFromRequest(requestDucr("ABC")).errors must be(Seq(FormError("ducrValue", "consignmentReferences.reference.ducrValue.error")))
      bindFromRequest(requestMucr("123")).errors must be(Seq(FormError("mucrValue", "consignmentReferences.reference.mucrValue.error")))
    }
  }

}
