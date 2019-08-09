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

package base.testdata

import base.testdata.CommonTestData._
import models.UcrBlock
import models.submissions.{ActionType, SubmissionPresentation}
import play.api.http.{ContentTypes, HeaderNames}
import play.api.mvc.Codec

import scala.xml.Elem

object ConsolidationTestData {

  val ValidMucr = "5GB123456789000-123ABC456DEFIIIII"
  val ValidDucr = "4GB123456789000-123ABC456DEFIIIII"

  val exampleAssociateDucrRequestXml: Elem =
    <inventoryLinkingConsolidationRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
      <messageCode>EAC</messageCode>
      <masterUCR>{ValidMucr}</masterUCR>
      <ucrBlock>
        <ucr>{ValidDucr}</ucr>
        <ucrType>D</ucrType>
      </ucrBlock>
    </inventoryLinkingConsolidationRequest>

  val exampleAssociateDucrRequestSubmission: SubmissionPresentation = SubmissionPresentation(
    eori = validEori,
    conversationId = conversationId,
    actionType = ActionType.DucrAssociation,
    ucrBlocks = Seq(UcrBlock(ucr = ValidMucr, ucrType = "M"), UcrBlock(ucr = ValidDucr, ucrType = "D"))
  )

  val exampleDisassociateDucrRequestXml: Elem =
    <inventoryLinkingConsolidationRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
      <messageCode>EAC</messageCode>
      <ucrBlock>
        <ucr>{ValidDucr}</ucr>
        <ucrType>D</ucrType>
      </ucrBlock>
    </inventoryLinkingConsolidationRequest>

  val exampleShutMucrRequestXml: Elem =
    <inventoryLinkingConsolidationRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
      <messageCode>CST</messageCode>
      <masterUCR>{ValidMucr}</masterUCR>
    </inventoryLinkingConsolidationRequest>

  val validConsolidationRequestHeaders: Seq[(String, String)] =
    Seq(HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8), HeaderNames.ACCEPT -> ContentTypes.XML(Codec.utf_8))

}
