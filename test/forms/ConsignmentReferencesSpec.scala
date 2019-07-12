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
      errors must contain(FormError("referenceValue", "mucr.ducr.reference.value.empty"))
    }

    "return error for incorrect reference" in {
      val inputData =
        ConsignmentReferences(Some("GB71757250450281160"), "Incorrect reference", "5123456789-000-123ABC45$%^FIIIII")
      val errors = ConsignmentReferences.form().fillAndValidate(inputData).errors

      errors mustBe List(
        FormError("eori", "consignmentReferences.eori.error"),
        FormError("reference", "consignmentReferences.reference.error"),
        FormError("referenceValue", "mucr.ducr.reference.value.error")
      )
    }

    "no errors for valid MUCR/DUCRs " in {
      val mucrFormat1 = ConsignmentReferences(Some("GB717572504502811"), "D", "5GB123456789000-123ABC456DEFIIIII")
      ConsignmentReferences.form().fillAndValidate(mucrFormat1).errors mustBe empty

      val mucrFormat2 = ConsignmentReferences(Some("GB717572504502811"), "D", "GB/ABC4-ASIUDYFAHSDJF")
      ConsignmentReferences.form().fillAndValidate(mucrFormat2).errors mustBe empty

      val mucrFormat3 = ConsignmentReferences(Some("GB717572504502811"), "D", "GB/1234SG789-1PWER")
      ConsignmentReferences.form().fillAndValidate(mucrFormat3).errors mustBe empty

      val mucrFormat4 = ConsignmentReferences(Some("BL717572504502811"), "D", "A:A8C12345678")
      ConsignmentReferences.form().fillAndValidate(mucrFormat4).errors mustBe empty

      val mucrFormat6 = ConsignmentReferences(Some("FR717572504502811"), "D", "C:XYZ123")
      ConsignmentReferences.form().fillAndValidate(mucrFormat6).errors mustBe empty
    }
  }
}
