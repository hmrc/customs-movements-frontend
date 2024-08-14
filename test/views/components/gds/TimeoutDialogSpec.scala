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

package views.components.gds

import base.{Injector, OverridableInjector}
import com.typesafe.config.ConfigFactory
import config.TimeoutDialogConfig
import controllers.routes.SignOutController
import forms.Choice
import models.SignOutReason.SessionTimeout
import models.SignedInUser
import models.requests.AuthenticatedRequest
import play.api.Configuration
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import testdata.CommonTestData.validEori
import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import views.ViewSpec
import views.html.choice

import scala.jdk.CollectionConverters.IterableHasAsScala

class TimeoutDialogSpec extends ViewSpec with Injector {

  "Timeout Dialog" should {
    "display the timeout dialog when user is signed in" in {
      implicit val request = FakeRequest().withCSRFToken
      val form: Form[Choice] = Choice.form
      val authenticatedRequest = AuthenticatedRequest(FakeRequest().withCSRFToken, SignedInUser(validEori, Enrolments(Set.empty)))

      val serviceConfig = new ServicesConfig(Configuration(ConfigFactory.parseString(s"""
          timeoutDialog.timeout="100 millis"
          timeoutDialog.countdown="200 millis"
      """)))

      val timeoutDialogConfig = new TimeoutDialogConfig(serviceConfig)
      val injector = new OverridableInjector(bind[TimeoutDialogConfig].toInstance(timeoutDialogConfig))
      val choicePage = injector.instanceOf[choice]
      val view = choicePage(form)(authenticatedRequest, messages)
      val elements = view.getElementsByTag("meta").asScala.toList
      val metas = elements.filter(_.attr("name") == "hmrc-timeout-dialog")
      assert(metas.nonEmpty)
      metas.head.dataset.get("sign-out-url") mustBe SignOutController.signOut(SessionTimeout).url
    }
  }
}
