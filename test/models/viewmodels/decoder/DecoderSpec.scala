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

package models.viewmodels.decoder

import models.viewmodels.decoder.ActionCode.Rejected
import models.viewmodels.decoder.CRCCode.Success
import models.viewmodels.decoder.ErrorCode.MucrNotShutDeparture
import models.viewmodels.decoder.ROECode.DocumentaryControl
import models.viewmodels.decoder.SOECode.Departed
import unit.base.UnitSpec

class DecoderSpec extends UnitSpec {

  val decoder = new Decoder()

  "Decoder" should {

    "find correct crc code" in {

      decoder.crc(Success.code) mustBe Some(Success)
    }

    "find correct roe code" in {

      decoder.roe(DocumentaryControl.code) mustBe Some(DocumentaryControl)
    }

    "find correct soe code" in {

      decoder.soe(Departed.code) mustBe Some(Departed)
    }

    "find correct action code" in {

      decoder.actionCode(Rejected.code) mustBe Some(Rejected)
    }

    "find correct error code" in {
      decoder.errorCode(MucrNotShutDeparture.code) mustBe Some(MucrNotShutDeparture)
    }
  }
}
