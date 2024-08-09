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

package models.submissions

import base.UnitSpec
import connectors.exchanges.ActionType.MovementType
import models.{now, UcrBlock}
import org.scalatest.OptionValues
import testdata.CommonTestData._

class SubmissionSpec extends UnitSpec with OptionValues {

  val submission = Submission(
    eori = validEori,
    conversationId = conversationId,
    ucrBlocks = Seq(UcrBlock(ucr = correctUcr, ucrType = "M"), UcrBlock(ucr = correctUcr_2, ucrType = "DP")),
    actionType = MovementType.Arrival,
    requestTimestamp = now
  )

  "Submission Frontend Model" should {

    "return correct value for hasMucr method" in {
      submission.hasMucr mustBe true
    }

    "return correct value for hasDucrPart method" in {
      submission.hasDucrPart mustBe true
    }

    "extract MUCR correctly" in {
      submission.extractMucr.value mustBe correctUcr
    }

    "extract first UCR" in {
      submission.extractFirstUcr.value mustBe correctUcr
    }
  }
}
