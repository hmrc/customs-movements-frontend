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

package views

import base.Injector
import models.notifications.queries.{MovementInfo, MucrInfo, UcrInfo}
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import views.html.ile_query_mucr_response
import views.tags.ViewTest

@ViewTest
class IleQueryMucrResponseViewSpec extends ViewSpec with Injector {

  private implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val page = instanceOf[ile_query_mucr_response]

  val movement = MovementInfo("EAL", "goods")
  val mucrInfo = MucrInfo(ucr = "mucr", movements = Seq(movement), isShut = Some(true))
  val parentInfo = MucrInfo("parentUcr")
  val associatedInfo = MucrInfo("childUcr")

  private def view(info: MucrInfo = mucrInfo, parent: Option[MucrInfo] = None, associatedConsignments: Seq[UcrInfo] = Seq.empty) =
    page(info, parent, associatedConsignments)

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

    "render associated consignments" in {
      view(associatedConsignments = Seq(associatedInfo)).getElementById("associatedUcrs") must containMessage("ileQueryResponse.associated")
    }

    "render isShut when mucr shut" in {
      view(mucrInfo.copy(isShut = Some(true))).getElementById("isShutMucr") must containMessage("ileQueryResponse.details.isShutMucr.true")
    }

    "render isShut when mucr not shut" in {
      view(mucrInfo.copy(isShut = Some(false))).getElementById("isShutMucr") must containMessage("ileQueryResponse.details.isShutMucr.false")
    }

    "not render isShut when missing" in {
      view(mucrInfo.copy(isShut = None)).getElementById("isShutMucr") must be(null)
    }

    "not render isShut when mucr has parent" in {
      view(info = mucrInfo.copy(isShut = Some(true)), parent = Some(MucrInfo("mucr"))).getElementById("isShutMucr") must be(null)
    }

    "display a 'Print' button" in {
      view().getElementsByClass("gem-c-print-link__button").size() mustBe 1
    }
  }
}
