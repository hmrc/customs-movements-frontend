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

package unit.models.submissions

import java.time.Instant

import models.UcrBlock
import models.submissions.{ActionType, SubmissionFrontendModel}
import org.scalatest.OptionValues
import testdata.CommonTestData._
import unit.base.UnitSpec

class SubmissionFrontendModelSpec extends UnitSpec with OptionValues {

  val submissionFrontendModel = SubmissionFrontendModel(
    eori = validEori,
    providerId = None,
    conversationId = conversationId,
    ucrBlocks = Seq(UcrBlock(ucr = correctUcr, ucrType = "M")),
    actionType = ActionType.Arrival,
    requestTimestamp = Instant.now()
  )

  "Submission Frontend Model" should {

    "return correct value for hasMucr method" in {

      submissionFrontendModel.hasMucr mustBe true
    }

    "extract MUCR correctly" in {

      submissionFrontendModel.extractMucr.value mustBe correctUcr
    }

    "extract first UCR" in {

      submissionFrontendModel.extractFirstUcr.value mustBe correctUcr
    }
  }
}
