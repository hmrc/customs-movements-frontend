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

package models.notifications

import models.notifications.queries.IleQueryResponseExchangeData.SuccessfulResponseExchangeData
import models.notifications.queries.{DucrInfo, MucrInfo}
import models.viewmodels.decoder.ROECode.{DocumentaryControl, NoControlRequired, NonBlockingDocumentaryControl, PhysicalExternalPartyControl}
import unit.base.UnitSpec

class IleQueryResponseExchangeDataSpec extends UnitSpec {

  "Query response data" should {

    "sort children ucrs base on the ROE status" in {

      val childDucrs = Seq(NonBlockingDocumentaryControl, PhysicalExternalPartyControl).map { roe =>
        DucrInfo(ucr = "UCR1", declarationId = "decId", entryStatus = Some(EntryStatus(roe = Some(roe))))
      }

      val childMucrs = Seq(NoControlRequired, DocumentaryControl).map { roe =>
        MucrInfo(ucr = "UCR1", entryStatus = Some(EntryStatus(roe = Some(roe))))
      }

      val response = SuccessfulResponseExchangeData(childDucrs = childDucrs, childMucrs = childMucrs)

      val expectedStatusOrder =
        Seq(PhysicalExternalPartyControl, DocumentaryControl, NonBlockingDocumentaryControl, NoControlRequired)

      response.sortedChildrenUcrs.map(_.entryStatus.flatMap(_.roe).get) mustBe expectedStatusOrder
    }
  }
}
