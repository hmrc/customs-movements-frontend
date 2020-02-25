/*
 * Copyright 2020 HM Revenue & Customs
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
import helpers.views.ConsignmentReferencesMessages
import play.api.data.FormError

class ConsignmentReferencesSpec extends BaseSpec with ConsignmentReferencesMessages {

  val validDucr = "9GB123456"
  val validMucr = "GB/ABC-12342"

  "Consignment References model" should {

    "contains formId" in {
      ConsignmentReferences.formId must be("ConsignmentReferences")
    }
  }

  "Consignment References mapping" should {
    "return errors for empty fields" in {
      val inputData = ConsignmentReferences("", "")
      val errors = ConsignmentReferences.form().fillAndValidate(inputData).errors

      errors must be(Seq(FormError("reference", referenceError)))
    }

    "no errors for complete fields " in {
      val inputData = ConsignmentReferences("M", validMucr)
      ConsignmentReferences.form().fillAndValidate(inputData).errors mustBe empty
    }

    "have error for missing Ducr" in {
      val inputData = ConsignmentReferences("D", "")
      val errors = ConsignmentReferences.form().fillAndValidate(inputData).errors

      errors must be(Seq(FormError("ducrValue", referenceDucrEmpty)))
    }

    "have error for missing Mucr" in {
      val inputData = ConsignmentReferences("M", "")
      val errors = ConsignmentReferences.form().fillAndValidate(inputData).errors

      errors must be(Seq(FormError("mucrValue", referenceMucrEmpty)))
    }

    "have error for invalid Ducr" in {
      val inputData = ConsignmentReferences("D", "ABC")
      val errors = ConsignmentReferences.form().fillAndValidate(inputData).errors

      errors must be(Seq(FormError("ducrValue", referenceDucrError)))
    }

    "have error for invalid Mucr" in {
      val inputData = ConsignmentReferences("M", "ABC")
      val errors = ConsignmentReferences.form().fillAndValidate(inputData).errors

      errors must be(Seq(FormError("mucrValue", referenceMucrError)))
    }
  }
}
