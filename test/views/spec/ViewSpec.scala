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

package views.spec

import testdata.MovementsTestData
import com.codahale.metrics.SharedMetricRegistries
import config.AppConfig
import forms.Choice
import models.requests.{AuthenticatedRequest, LegacyJourneyRequest}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.mvc.{AnyContentAsEmpty, Flash, Request}
import play.api.test.FakeRequest
import services.{Countries, Country}
import utils.FakeRequestCSRFSupport._

@deprecated("Please use UnitViewSpec instead", since = "2019-09-06")
trait ViewSpec extends PlaySpec with GuiceOneAppPerSuite with ViewValidator {

  lazy val basePrefix = "supplementary."
  lazy val addressPrefix = "supplementary.address."

  override def fakeApplication(): Application = {
    SharedMetricRegistries.clear()
    super.fakeApplication()
  }

  lazy val injector: Injector = app.injector
  implicit lazy val appConfig: AppConfig = injector.instanceOf[AppConfig]

  implicit lazy val messagesApi: MessagesApi = injector.instanceOf[MessagesApi]
  implicit lazy val fakeRequest: Request[AnyContentAsEmpty.type] =
    FakeRequest("", "").withCSRFToken
  implicit lazy val messages: Messages = messagesApi.preferred(FakeRequest())
  implicit lazy val flash: Flash = new Flash()
  implicit lazy val countries: List[Country] = Countries.allCountries

  def assertMessage(key: String, expected: String): Unit =
    messages(key) must be(expected)

  def fakeJourneyRequest(choice: Choice): LegacyJourneyRequest[AnyContentAsEmpty.type] =
    LegacyJourneyRequest(AuthenticatedRequest(fakeRequest, MovementsTestData.newUser("")), choice)

}
