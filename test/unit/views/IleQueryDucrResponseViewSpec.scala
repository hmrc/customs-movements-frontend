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

package views

import base.Injector
import models.notifications.EntryStatus
import models.notifications.queries.{DucrInfo, MovementInfo, MucrInfo}
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import views.html.ile_query_ducr_response
import views.tags.ViewTest

@ViewTest
class IleQueryDucrResponseViewSpec extends ViewSpec with Injector {

  private implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val page = instanceOf[ile_query_ducr_response]

  val status = EntryStatus(Some("ICS"), None, Some("SOE"))
  val movement = MovementInfo("EAL", "goods")
  val ducrInfo = DucrInfo(ucr = "ducr", declarationId = "id", movements = Seq(movement), entryStatus = Some(status))
  val parentInfo = MucrInfo("parentUcr")

  private def view(info: DucrInfo = ducrInfo, parent: Option[MucrInfo] = None) = page(info, parent)

  "Ile Query page" should {

    "render title" in {
      view().getTitle must containMessage("ileQueryResponse.ducr.title")
    }

    "render queried ucr summary" in {
      view().getElementById("summary") must containMessage("ileQueryResponse.details")
    }

    "render previous movements" in {
      view().getElementById("previousMovements") must containMessage("ileQueryResponse.previousMovements")
    }

    "render parent" in {
      view(parent = Some(parentInfo)).getElementById("parentConsignment") must containMessage("ileQueryResponse.parent")
    }

    "display a 'Print' button" in {
      view().getElementsByClass("gem-c-print-link__button").size() mustBe 2
    }
  }
}
