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

package views.spec

import akka.util.Timeout
import base.Injector
import config.AppConfig
import forms.Choice
import models.requests.{AuthenticatedRequest, LegacyJourneyRequest}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{AnyContent, AnyContentAsEmpty, Request, Result}
import play.api.test.Helpers.contentAsString
import play.api.test.{FakeRequest, Helpers}
import play.twirl.api.Html
import testdata.MovementsTestData
import unit.base.UnitSpec
import utils.Stubs
import views.html.main_template

import scala.concurrent.Future

class UnitViewSpec extends UnitSpec with ViewMatchers {

  import utils.FakeRequestCSRFSupport._

  implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  def fakeJourneyRequest(choice: Choice): LegacyJourneyRequest[AnyContentAsEmpty.type] =
    LegacyJourneyRequest(AuthenticatedRequest(FakeRequest("", "").withCSRFToken, MovementsTestData.newUser("")), choice)

  implicit val messages: Messages = Helpers.stubMessages()

  def mainTemplate: main_template = UnitViewSpec.mainTemplate

  def messagesApi: MessagesApi = UnitViewSpec.realMessagesApi

  def appConfig: AppConfig = UnitViewSpec.realAppConfig

  implicit protected def htmlBodyOf(html: Html): Document = Jsoup.parse(html.toString())
  implicit protected def htmlBodyOf(page: String): Document = Jsoup.parse(page)
  implicit protected def htmlBodyOf(result: Future[Result])(implicit timeout: Timeout): Document =
    htmlBodyOf(contentAsString(result))
}

object UnitViewSpec extends Stubs with Injector {
  val realMessagesApi = instanceOf[MessagesApi]
  val realAppConfig = instanceOf[AppConfig]
}
