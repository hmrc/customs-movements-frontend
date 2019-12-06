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

package testdata

import models.UcrBlock
import models.external.requests.ConsolidationRequest
import models.external.requests.ConsolidationType._
import models.submissions.{ActionType, Submission}
import play.api.http.{ContentTypes, HeaderNames}
import testdata.CommonTestData._

object ConsolidationTestData {

  val validMucr = "GB/1234567890-MUCR"
  val validDucr = "4GB123456789000-DUCR"

  val exampleAssociateDucrRequest: ConsolidationRequest =
    ConsolidationRequest(consolidationType = ASSOCIATE_DUCR, eori = validEori, mucr = Some(validMucr), ucr = Some(validDucr))

  val exampleAssociateDucrRequestSubmission: Submission = Submission(
    eori = validEori,
    conversationId = conversationId,
    actionType = ActionType.DucrAssociation,
    ucrBlocks = Seq(UcrBlock(ucr = validMucr, ucrType = "M"), UcrBlock(ucr = validDucr, ucrType = "D"))
  )

  val exampleDisassociateDucrRequest: ConsolidationRequest =
    ConsolidationRequest(consolidationType = DISASSOCIATE_DUCR, eori = validEori, mucr = None, ucr = Some(validDucr))

  val exampleShutMucrRequest: ConsolidationRequest =
    ConsolidationRequest(consolidationType = SHUT_MUCR, eori = validEori, mucr = Some(validMucr), ucr = None)

  val validConsolidationRequestHeaders: Seq[(String, String)] =
    Seq(HeaderNames.CONTENT_TYPE -> ContentTypes.JSON, HeaderNames.ACCEPT -> ContentTypes.JSON)

}
