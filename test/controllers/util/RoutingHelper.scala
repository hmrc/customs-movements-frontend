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

package controllers.util

import java.util.UUID

import forms.ShutMucrSpec.correctShutMucrJSON
import play.api.libs.json.JsValue
import play.api.mvc.Result
import play.api.Application
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.SessionKeys
import utils.FakeRequestCSRFSupport._

import scala.concurrent.Future

case class RoutingHelper(app: Application, uri: String) {

  def routeGet(headers: Map[String, String] = Map.empty): Future[Result] =
    route(app, FakeRequest(GET, uri).withHeaders(headers.toSeq: _*).withCSRFToken).get

  def routePost(headers: Map[String, String] = Map.empty, body: JsValue): Future[Result] =
    route(
      app,
      FakeRequest(POST, uri)
        .withHeaders(headers.toSeq: _*)
        .withSession(Map(SessionKeys.sessionId -> s"session-${UUID.randomUUID()}").toSeq: _*)
        .withJsonBody(body)
        .withCSRFToken
    ).get

}
