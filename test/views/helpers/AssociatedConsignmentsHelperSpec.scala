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

package views.helpers

import base.Injector
import models.notifications.EntryStatus
import models.notifications.queries.{DucrInfo, MucrInfo}
import models.viewmodels.decoder.SOECode
import models.viewmodels.ilequery.IleQueryCodeConverter
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.{MessagesStub, ViewSpec}

class AssociatedConsignmentsHelperSpec extends ViewSpec with MessagesStub with Injector {

  private val converter = instanceOf[IleQueryCodeConverter]
  private implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  "generateRowsForChildUcrs" should {

    "return the correct messages for SOE status" when {

      "a mixture of DUCRs and MUCRs are given" in {
        val ucrInfos = Seq(
          MucrInfo("childUcr", entryStatus = Some(EntryStatus(None, None, Some("0")))),
          DucrInfo("childUcr1", declarationId = "", entryStatus = Some(EntryStatus(None, None, Some("0"))))
        )
        val result = AssociatedConsignmentsHelper.generateRowsForChildUcrs(ucrInfos)(messages, converter)

        val soeCols = result.map(_.last)
        soeCols.head.content.asHtml.text mustBe "0 - " + messages(SOECode.ConsolidationOpen.messageKey)
        soeCols.last.content.asHtml.text mustBe "0 - " + messages(SOECode.NonExistentDeclaration.messageKey)
      }
    }
  }
}
