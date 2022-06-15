/*
 * Copyright 2022 HM Revenue & Customs
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
import models.notifications.EntryStatus
import models.notifications.queries.{MovementInfo, MucrInfo, Transport}
import models.viewmodels.decoder.ROECode.UnknownRoe
import models.viewmodels.decoder.{ICSCode, ROECode, SOECode}
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.ViewSpec
import views.html.components.ilequery.response_mucr_summary
import views.tags.ViewTest

@ViewTest
class MucrSummaryViewSpec extends ViewSpec with Injector {

  private implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val page = instanceOf[response_mucr_summary]

  val status = EntryStatus(Some(ICSCode.InvalidationByCustoms.code), Some(ROECode.DocumentaryControl), Some(SOECode.DeclarationValidation.code))
  val movement =
    MovementInfo("", "", transportDetails = Some(Transport(modeOfTransport = Some("mode"), nationality = Some("FR"), transportId = Some("WagonId"))))
  val mucrInfo =
    MucrInfo(ucr = "mucr", movements = Seq(movement), entryStatus = Some(status), isShut = Some(true))

  private def summaryElement(html: Html, index: Int) = html.getElementById("summary").select(s"dl>div:eq($index)>dd").get(0)

  private def view(mucr: MucrInfo = mucrInfo) = page(mucr)

  "Mucr summary" should {

    "render all routes of entry" in {
      ROECode.codes
        .filterNot(_ == UnknownRoe())
        .foreach(roe => summaryElement(view(mucrInfo.copy(entryStatus = Some(status.copy(roe = Some(roe))))), 0) must containMessage(roe.messageKey))
    }

    "render unknown route of entry" in {
      val summaryView = view(mucrInfo.copy(entryStatus = Some(status.copy(roe = Some(UnknownRoe())))))
      summaryElement(summaryView, 0) must containMessage("ileCode.unknown")
    }

    "render all mucr status of entry" in {
      SOECode.MucrCodes.foreach(soe =>
        summaryElement(view(mucrInfo.copy(entryStatus = Some(status.copy(soe = Some(soe.code))))), 1) must containMessage(soe.messageKey)
      )
    }

    "render unknown status of entry" in {
      val summaryView = view(mucrInfo.copy(entryStatus = Some(status.copy(soe = Some("unknown")))))
      summaryElement(summaryView, 1) must containMessage("ileCode.unknown")
    }

    "render transport" in {
      summaryElement(view(), 2).text() must include("WagonId, France")
    }

    "render all rows" in {
      val summaryText = view().text()
      summaryText must include("Route")
      summaryText must include("Status")
      summaryText must include("Transport")
    }

    "not render rows when codes and transport missing" in {
      val summaryText = view(mucrInfo.copy(entryStatus = None, movements = Seq.empty)).text()
      summaryText must not include "Route"
      summaryText must not include "Status"
      summaryText must not include "Transport"
    }

  }

}
