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

package views.ilequery

import base.Injector
import models.notifications.EntryStatus
import models.notifications.queries.MucrInfo
import models.viewmodels.decoder.{ROECode, SOECode}
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.ViewSpec
import views.html.components.ilequery.response_parent
import views.tags.ViewTest

@ViewTest
class ParentViewSpec extends ViewSpec with Injector {

  private implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val page = instanceOf[response_parent]

  private def view(parent: Option[MucrInfo] = None) = page(parent)

  val parent = Some(
    MucrInfo("parentUcr", entryStatus = Some(EntryStatus(None, Some(ROECode.DocumentaryControl), Some(SOECode.ConsolidationOpen.code))))
  )

  val parentNoEntryStatus = Some(MucrInfo("parentUcr", None))

  private def parentElement(html: Html, index: Int) = html.getElementById("parentConsignment").select(s"dl>div:eq($index)>dd").get(0)

  "Parent view" should {

    "not render when no parent" in {

      view().text() mustBe ""
    }

    "render parent consignment link" in {
      val parentRefElement = parentElement(view(parent), 0)

      parentRefElement.getElementsByClass("govuk-link").first() must haveHref(
        controllers.ileQuery.routes.IleQueryController.getConsignmentInformation("parentUcr")
      )
      parentRefElement.text() must be("parentUcr")
    }

    "render parent consignment route" in {
      val parentRouteElement = parentElement(view(parent), 1)

      parentRouteElement.text() must not include "Route"
      parentRouteElement must containMessage(ROECode.DocumentaryControl.messageKey)
    }

    "render parent consignment status" in {
      val parentStatusElement = parentElement(view(parent), 2)

      parentStatusElement.text() must not include "Input status"
      parentStatusElement must containMessage(SOECode.ConsolidationOpen.messageKey)
    }

    "not render rows when status is missing" in {
      val text = view(parentNoEntryStatus).text()
      text must include("parentUcr")
      text must not include ("Route")
      text must not include ("Input status")
    }
  }
}
