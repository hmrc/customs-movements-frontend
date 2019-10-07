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
import models.submissions.{ActionType, SubmissionFrontendModel}
import play.api.http.{ContentTypes, HeaderNames}
import testdata.CommonTestData._

object ConsolidationTestData {

  val ValidMucr = "5GB123456789000-123ABC456DEFIIIII"
  val ValidDucr = "4GB123456789000-123ABC456DEFIIIII"

  val exampleAssociateDucrRequest: ConsolidationRequest =
    ConsolidationRequest(ASSOCIATE_DUCR, Some(ValidMucr), Some(ValidDucr))

  val exampleAssociateDucrRequestSubmission: SubmissionFrontendModel = SubmissionFrontendModel(
    eori = validEori,
    conversationId = conversationId,
    actionType = ActionType.DucrAssociation,
    ucrBlocks = Seq(UcrBlock(ucr = ValidMucr, ucrType = "M"), UcrBlock(ucr = ValidDucr, ucrType = "D"))
  )

  val exampleDisassociateDucrRequest: ConsolidationRequest =
    ConsolidationRequest(DISASSOCIATE_DUCR, None, Some(ValidDucr))

  val exampleShutMucrRequest: ConsolidationRequest = ConsolidationRequest(SHUT_MUCR, Some(ValidMucr), None)

  val validConsolidationRequestHeaders: Seq[(String, String)] =
    Seq(HeaderNames.CONTENT_TYPE -> ContentTypes.JSON, HeaderNames.ACCEPT -> ContentTypes.JSON)

}
