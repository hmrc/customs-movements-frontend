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
import models.cache.JourneyType
import play.api.data.FormError
import play.api.libs.json.{JsObject, JsString}

class ConsignmentReferencesSpec extends BaseSpec {

  val validDucr = "9GB123456"
  val validMucr = "GB/ABC-12342"

  val arrive = JourneyType.ARRIVE
  val depart = JourneyType.DEPART

  "Consignment References model" should {

    "contains formId" in {
      ConsignmentReferences.formId must be("ConsignmentReferences")
    }
  }

  "Consignment References mapping" should {
    "return errors for empty fields" in {
      val inputData = ConsignmentReferences("", "")
      val errors = ConsignmentReferences.form(arrive).fillAndValidate(inputData).errors

      errors must be(Seq(FormError("reference", "consignmentReferences.reference.error")))
    }

    "no errors for complete fields " in {
      val inputData = ConsignmentReferences("M", validMucr)
      ConsignmentReferences.form(depart).fillAndValidate(inputData).errors mustBe empty
    }

    "have error for missing Ducr" in {
      val inputData = ConsignmentReferences("D", "")
      val errors = ConsignmentReferences.form(arrive).fillAndValidate(inputData).errors

      errors must be(Seq(FormError("ducrValue", "consignmentReferences.reference.ducrValue.empty")))
    }

    "have error for missing Mucr" in {
      val inputData = ConsignmentReferences("M", "")
      val errors = ConsignmentReferences.form(depart).fillAndValidate(inputData).errors

      errors must be(Seq(FormError("mucrValue", "consignmentReferences.reference.mucrValue.empty")))
    }

    "have error for invalid Ducr" in {
      val inputData = ConsignmentReferences("D", "ABC")
      val errors = ConsignmentReferences.form(arrive).fillAndValidate(inputData).errors

      errors must be(Seq(FormError("ducrValue", "consignmentReferences.reference.ducrValue.error")))
    }

    "have error for invalid Mucr" in {
      val inputData = ConsignmentReferences("M", "ABC")
      val errors = ConsignmentReferences.form(depart).fillAndValidate(inputData).errors

      errors must be(Seq(FormError("mucrValue", "consignmentReferences.reference.mucrValue.error")))
    }

    "have error for Mucr length > 35 characters" in {
      val inputData = ConsignmentReferences("M", "GB/82F9-0N2F6500040010TO120P0A300689")
      val errors = ConsignmentReferences.form(arrive).fillAndValidate(inputData).errors

      errors must be(Seq(FormError("mucrValue", "consignmentReferences.reference.mucrValue.error")))
    }

    "convert ducr to upper case" in {

      val form =
        ConsignmentReferences
          .form(depart)
          .bind(JsObject(Map("reference" -> JsString("D"), "ducrValue" -> JsString("8gb123457359100-test0001"))), JsonBindMaxChars)

      form.errors mustBe empty
      form.value.map(_.referenceValue) must be(Some("8GB123457359100-TEST0001"))
    }

    "convert mucr to upper case" in {

      val form =
        ConsignmentReferences
          .form(arrive)
          .bind(JsObject(Map("reference" -> JsString("M"), "mucrValue" -> JsString("gb/abced1234-15804test"))), JsonBindMaxChars)

      form.errors mustBe empty
      form.value.map(_.referenceValue) must be(Some("GB/ABCED1234-15804TEST"))
    }
  }
}
