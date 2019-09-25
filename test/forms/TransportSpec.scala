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
import forms.Transport.ModesOfTransport._
import play.api.data.FormError
import utils.TestDataHelper
import utils.TestDataHelper.createRandomAlphanumericString

class TransportSpec extends BaseSpec {

  "Transport model" should {

    "has correct formId value" in {

      Transport.formId must be("Transport")
    }

    "has correct values of Mode of Transport" in {

      Sea must be("1")
      Rail must be("2")
      Road must be("3")
      Air must be("4")
      PostalOrMail must be("5")
      FixedInstallations must be("6")
      InlandWaterway must be("7")
      Other must be("8")
    }

    "contains all allowed modes of transport" in {

      Transport.allowedModeOfTransport.length must be(8)
      Transport.allowedModeOfTransport must contain(Sea)
      Transport.allowedModeOfTransport must contain(Rail)
      Transport.allowedModeOfTransport must contain(Road)
      Transport.allowedModeOfTransport must contain(Air)
      Transport.allowedModeOfTransport must contain(PostalOrMail)
      Transport.allowedModeOfTransport must contain(FixedInstallations)
      Transport.allowedModeOfTransport must contain(InlandWaterway)
      Transport.allowedModeOfTransport must contain(Other)
    }

  }

  "Transport mapping" should {

    "return error" when {

      "mode of transport, reference and nationality are empty" in {

        val inputData = Transport("", "", "")
        val errors = Transport.form.fillAndValidate(inputData).errors

        errors.length must be(3)
        errors(0) must be(FormError("modeOfTransport", "transport.modeOfTransport.error"))
        errors(1) must be(FormError("nationality", "transport.nationality.empty"))
        errors(2) must be(FormError("transportId", "transport.transportId.empty"))
      }

      "mode of transport, reference and nationality are incorrect" in {

        val inputData = Transport("incorrect", "incorrect", createRandomAlphanumericString(36))
        val errors = Transport.form.fillAndValidate(inputData).errors

        errors.length must be(3)
        errors(0) must be(FormError("modeOfTransport", "transport.modeOfTransport.error"))
        errors(1) must be(FormError("nationality", "transport.nationality.error"))
        errors(2) must be(FormError("transportId", "transport.transportId.error"))
      }
    }

    "return no error" when {

      "values are correct for different country codes" in {
        val transportPoland = Transport(Sea, "PL", "Reference")
        Transport.form.fillAndValidate(transportPoland).errors mustBe empty

        val transportFinland = Transport(Rail, "FI", "SHIP-123")
        Transport.form.fillAndValidate(transportFinland).errors mustBe empty
      }
    }
  }
}
