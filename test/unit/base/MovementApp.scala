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

package base

import java.util.UUID

import akka.stream.Materializer
import com.codahale.metrics.SharedMetricRegistries
import metrics.MovementsMetrics
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsValue
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsJson, Request}
import play.api.test.FakeRequest
import play.filters.csrf.{CSRFConfig, CSRFConfigProvider, CSRFFilter}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.SessionKeys
import utils.FakeRequestCSRFSupport._

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

trait MovementApp extends PlaySpec with GuiceOneAppPerSuite with MockAuthConnector with MockMovementsMetrics with ScalaFutures {

  override def fakeApplication(): Application = {
    SharedMetricRegistries.clear()
    GuiceApplicationBuilder()
      .overrides(bind[AuthConnector].to(authConnectorMock))
      .build()
  }

  val cfg: CSRFConfig = app.injector.instanceOf[CSRFConfigProvider].get

  val token: String =
    app.injector.instanceOf[CSRFFilter].tokenProvider.generateToken

  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  implicit val messages: Messages = messagesApi.preferred(FakeRequest())

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  implicit val mat: Materializer = app.materializer

  val metrics = app.injector.instanceOf[MovementsMetrics]

  implicit val ec: ExecutionContext = global

  protected def getRequest(uri: String, headers: Map[String, String] = Map.empty): Request[AnyContentAsEmpty.type] = {
    val session: Map[String, String] = Map(SessionKeys.sessionId -> s"session-${UUID.randomUUID()}")

    FakeRequest("GET", uri)
      .withHeaders((Map(cfg.headerName -> token) ++ headers).toSeq: _*)
      .withSession(session.toSeq: _*)
      .withCSRFToken
  }

  protected def postRequest(uri: String, body: JsValue, headers: Map[String, String] = Map.empty): Request[AnyContentAsJson] = {
    val session: Map[String, String] = Map(SessionKeys.sessionId -> s"session-${UUID.randomUUID()}")

    FakeRequest("POST", uri)
      .withHeaders((Map(cfg.headerName -> token) ++ headers).toSeq: _*)
      .withSession(session.toSeq: _*)
      .withJsonBody(body)
      .withCSRFToken
  }
}
