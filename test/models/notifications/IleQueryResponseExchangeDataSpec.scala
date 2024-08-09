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

package models.notifications

import base.UnitSpec
import models.notifications.queries.IleQueryResponseExchangeData.SuccessfulResponseExchangeData
import models.notifications.queries.{DucrInfo, MucrInfo}
import models.viewmodels.decoder.ROECode.{DocumentaryControl, NoControlRequired, NonBlockingDocumentaryControl, PhysicalExternalPartyControl}

class IleQueryResponseExchangeDataSpec extends UnitSpec {

  "SuccessfulResponseExchangeData on sortedChildrenUcrs" should {

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

  "SuccessfulResponseExchangeData on queriedUcr" when {

    "queried UCR was a DUCR" should {
      "return queriedDucr element" in {

        val queriedDucrInfo = DucrInfo(ucr = "ducr", parentMucr = Some("parent-mucr"), declarationId = "declaration-id")
        val response = SuccessfulResponseExchangeData(queriedDucr = Some(queriedDucrInfo))

        val queriedUcr = response.queriedUcr

        queriedUcr mustBe queriedDucrInfo
      }
    }

    "queried UCR was a MUCR" should {
      "return queriedMucr element" in {

        val queriedMucrInfo = MucrInfo(ucr = "mucr", parentMucr = Some("parent-mucr"))
        val response = SuccessfulResponseExchangeData(queriedMucr = Some(queriedMucrInfo))

        val queriedUcr = response.queriedUcr

        queriedUcr mustBe queriedMucrInfo
      }
    }

    "both queriedDucr and queriedMucr are present" should {
      "throw IllegalStateException" in {

        val queriedDucrInfo = DucrInfo(ucr = "ducr", parentMucr = Some("parent-mucr"), declarationId = "declaration-id")
        val queriedMucrInfo = MucrInfo(ucr = "mucr", parentMucr = Some("parent-mucr"))
        val response = SuccessfulResponseExchangeData(queriedDucr = Some(queriedDucrInfo), queriedMucr = Some(queriedMucrInfo))

        intercept[IllegalStateException](response.queriedUcr)
      }
    }

    "both queriedDucr and queriedMucr are empty" should {
      "throw IllegalStateException" in {

        val response = SuccessfulResponseExchangeData(queriedDucr = None, queriedMucr = None)

        intercept[IllegalStateException](response.queriedUcr)
      }
    }
  }
}
