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
import forms.ConsignmentReferences.AllowedReferences
import play.api.data.FormError

class ConsignmentReferencesSpec extends BaseSpec {

  "Consignment References model" should {
    "contains all allowed values" in {
      val allowedReferences = ConsignmentReferences.allowedReferenceAnswers

      allowedReferences must contain(AllowedReferences.Ducr)
      allowedReferences must contain(AllowedReferences.Mucr)
    }

    "has correct allowed references" in {
      ConsignmentReferences.AllowedReferences.Ducr must be("D")
      ConsignmentReferences.AllowedReferences.Mucr must be("M")
    }

    "contains formId" in {
      ConsignmentReferences.formId must be("ConsignmentReferences")
    }
  }

  "Consignment References mapping" should {
    "return errors for empty fields" in {
      val inputData = ConsignmentReferences(Some(""), "", "")
      val errors = ConsignmentReferences.form().fillAndValidate(inputData).errors

      errors.length must be(3)
      errors must contain(FormError("eori", "consignmentReferences.eori.error"))
      errors must contain(FormError("reference", "consignmentReferences.reference.empty"))
      errors must contain(FormError("referenceValue", "consignmentReferences.reference.value.empty"))
    }

    "return error for incorrect reference" in {
      val inputData =
        ConsignmentReferences(Some("GB71757250450281160"), "Incorrect reference", "5123456789-000-123ABC45$%^FIIIII")
      val errors = ConsignmentReferences.form().fillAndValidate(inputData).errors

      errors.length must be(3)
      errors(0) must be(FormError("eori", "consignmentReferences.eori.error"))
      errors(1) must be(FormError("reference", "consignmentReferences.reference.error"))
      errors(2) must be(FormError("referenceValue", "consignmentReferences.reference.value.error"))
    }

    "no errors when data is correct with different valid MUCR/DUCRs" in {
      val inputData1 = ConsignmentReferences(Some("GB717572504502811"), "D", "5GB123456789000-123ABC456DEFIIIII")
      val errors1 = ConsignmentReferences.form().fillAndValidate(inputData1).errors
      errors1.length must be(0)

      val inputData2 = ConsignmentReferences(Some("GB717572504502811"), "D", "GB/ABC4-ASIUDYFAHSDJF")
      val errors2 = ConsignmentReferences.form().fillAndValidate(inputData2).errors
      errors2.length must be(0)

      val inputData3 = ConsignmentReferences(Some("GB717572504502811"), "D", "GB/1234SG789-1PWER")
      val errors3 = ConsignmentReferences.form().fillAndValidate(inputData3).errors
      errors3.length must be(0)

      val inputData4 = ConsignmentReferences(Some("GB717572504502811"), "D", "A:A8C12345678")
      val errors4 = ConsignmentReferences.form().fillAndValidate(inputData4).errors
      errors4.length must be(0)

      val inputData5 = ConsignmentReferences(Some("GB717572504502811"), "D", "C:XYZ123")
      val errors5 = ConsignmentReferences.form().fillAndValidate(inputData5).errors
      errors5.length must be(0)
    }
  }
}
