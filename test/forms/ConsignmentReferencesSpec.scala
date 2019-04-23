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

import base.FormBaseSpec
import forms.ConsignmentReferences.AllowedReferences
import play.api.data.FormError

class ConsignmentReferencesSpec extends FormBaseSpec {

  "Consignment References model" should {
    "contains all allowed values" in {
      val allowedReferences = ConsignmentReferences.allowedReferenceAnswers

      allowedReferences must contain(AllowedReferences.Ducr)
      allowedReferences must contain(AllowedReferences.Mucr)
    }

    "has correct allowed references" in {
      ConsignmentReferences.AllowedReferences.Ducr must be("Ducr")
      ConsignmentReferences.AllowedReferences.Mucr must be("Mucr")
    }

    "contains formId" in {
      ConsignmentReferences.formId must be("ConsignmentReferences")
    }
  }

  "Consignment References mapping" should {
    "return errors for empty fields" in {
      val inputData = ConsignmentReferences("", "", "")
      val errors = ConsignmentReferences.form().fillAndValidate(inputData).errors

      errors.length must be(3)
      errors must contain(FormError("eori", "consignmentReferences.eori.empty"))
      errors must contain(FormError("reference", "consignmentReferences.reference.empty"))
      errors must contain(FormError("referenceValue", "consignmentReferences.reference.value.empty"))
    }

    "return error for incorrect reference" in {
      val inputData = ConsignmentReferences("eori", "Incorrect reference", "12345")
      val errors = ConsignmentReferences.form().fillAndValidate(inputData).errors

      errors.length must be(1)
      errors.head must be(FormError("reference", "consignmentReferences.reference.error"))
    }

    "no errors when data is correct" in {
      val inputData = ConsignmentReferences("eori", "Ducr", "123456")
      val errors = ConsignmentReferences.form().fillAndValidate(inputData).errors

      errors.length must be(0)
    }
  }
}
