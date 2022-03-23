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

package views.components.config

import base.Injector
import config.IleQueryConfig
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import views.ViewSpec
import views.html.components.confirmation_link

class ConfirmationPageConfigSpec extends ViewSpec with MockitoSugar with Injector {

  private implicit val request: Request[AnyContent] = FakeRequest().withCSRFToken

  private val ileQueryConfig = mock[IleQueryConfig]
  private val confirmationLink = instanceOf[confirmation_link]
  private val confirmationPageConfig = new ConfirmationPageConfig(ileQueryConfig, confirmationLink)

  "ConfirmationPageConfig on nextStepLink" when {

    "IleQuery feature flag is disabled" should {

      "return confirmation link to Choice page" in {

        when(ileQueryConfig.isIleQueryEnabled).thenReturn(false)

        val nextStepLink = confirmationPageConfig.nextStepLink

        val expectedNextStepLink = confirmationLink(
          message = messages("confirmation.redirect.choice.link"),
          linkTarget = controllers.routes.ChoiceController.displayChoiceForm()
        )
        nextStepLink mustBe expectedNextStepLink
      }
    }

    "IleQuery feature flag is enabled" should {

      "return confirmation link to Find Consignment page" in {

        when(ileQueryConfig.isIleQueryEnabled).thenReturn(true)

        val nextStepLink = confirmationPageConfig.nextStepLink

        val expectedNextStepLink = confirmationLink(
          message = messages("confirmation.redirect.query.link"),
          linkTarget = controllers.ileQuery.routes.FindConsignmentController.displayQueryForm()
        )
        nextStepLink mustBe expectedNextStepLink
      }
    }
  }

}
