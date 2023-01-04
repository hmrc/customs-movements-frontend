/*
 * Copyright 2023 HM Revenue & Customs
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

package views.components.ilequery

import base.Injector
import controllers.ileQuery.routes.IleQueryController
import models.notifications.EntryStatus
import models.notifications.queries.{MucrInfo, UcrInfo}
import models.viewmodels.decoder.{ROECode, SOECode}
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import views.ViewSpec
import views.html.components.ilequery.response_associated_consignments
import views.tags.ViewTest

@ViewTest
class AssociatedConsignmentsViewSpec extends ViewSpec with Injector {

  private implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val page = instanceOf[response_associated_consignments]

  private def view(associatedConsignments: Seq[UcrInfo] = Seq.empty) = page(associatedConsignments)

  "Associated consignment " should {

    "not render for empty associated consignments list" in {

      view().getElementById("associatedUcrs") must be(null)
      view().text() mustBe ""
    }

    "render associate consignments section" in {
      val viewWithChild = view(associatedConsignments =
        Seq(MucrInfo("childUcr", entryStatus = Some(EntryStatus(None, Some(ROECode.DocumentaryControl), Some(SOECode.Departed.code)))))
      )
      viewWithChild.getElementById("associatedUcrs") must containMessage("ileQueryResponse.associated")

      val elmChild = viewWithChild.getElementById("associateUcr_0_ucr")
      elmChild must containText("childUcr")
      elmChild.getElementsByClass("govuk-link").first() must haveHref(IleQueryController.getConsignmentData("childUcr"))

      viewWithChild.getElementById("associateUcr_0_roe") must containMessage(ROECode.DocumentaryControl.messageKey)

      viewWithChild.getElementById("associateUcr_0_soe") must containMessage(SOECode.Departed.messageKey)
    }
  }
}
