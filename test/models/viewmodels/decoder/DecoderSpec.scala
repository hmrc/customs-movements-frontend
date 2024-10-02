/*
 * Copyright 2024 HM Revenue & Customs
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

import base.UnitSpec
import models.viewmodels.decoder.CRCCode.Success
import models.viewmodels.decoder.ICSCode.InvalidationAtTraderRequest
import models.viewmodels.decoder.ROECode.DocumentaryControl
import models.viewmodels.decoder.SOECode.{ConsolidationOpen, ConsolidationWithEmptyMucr, Departed}
import play.api.{Environment, Mode}
import utils.JsonFile

class DecoderSpec extends UnitSpec {

  private lazy val jsonFile = new JsonFile(Environment.simple(mode = Mode.Test))
  val decoder = new Decoder(jsonFile)

  "Decoder" should {

    "find correct crc code" in {

      decoder.crc(Success.code) mustBe Some(Success)
    }

    "find correct ics code" in {

      decoder.ics(InvalidationAtTraderRequest.code) mustBe Some(InvalidationAtTraderRequest)
    }

    "find correct roe code" in {

      decoder.roe(DocumentaryControl.code) mustBe Some(DocumentaryControl)
    }

    "find correct soe code" in {

      decoder.ducrSoe(Departed.code) mustBe Some(Departed)
    }

    "not find MUCR soe code when provided with DUCR soe code" in {

      decoder.mucrSoe(Departed.code) mustBe None
    }

    "find correct MUCR soe code" in {

      decoder.mucrSoe(ConsolidationOpen.code) mustBe Some(ConsolidationOpen)
    }

    "not find DUCR soe code when provided with MUCR soe code" in {

      decoder.ducrSoe(ConsolidationWithEmptyMucr.code) mustBe None
    }

    "find correct ILE error" in {

      val mucrNotShutDeparture = ILEError("04", "error.ile.MucrNotShutConsolidation")

      decoder.error(mucrNotShutDeparture.code) mustBe Some(mucrNotShutDeparture)
    }
  }
}
