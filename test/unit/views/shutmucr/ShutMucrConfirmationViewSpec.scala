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

package views.shutmucr

import base.OverridableInjector
import config.AppConfig
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject._
import play.api.test.FakeRequest
import views.ViewSpec
import views.html.shutmucr.shut_mucr_confirmation
import views.tags.ViewTest

@ViewTest
class ShutMucrConfirmationViewSpec extends ViewSpec with MockitoSugar with BeforeAndAfterEach {

  private implicit val request = FakeRequest().withCSRFToken

  private val appConfig = mock[AppConfig]
  private val injector = new OverridableInjector(bind[AppConfig].toInstance(appConfig))

  private val page = injector.instanceOf[shut_mucr_confirmation]

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(appConfig.ileQueryEnabled).thenReturn(false)
  }

  override def afterEach(): Unit = {
    reset(appConfig)

    super.afterEach()
  }

  "ShutMucrConfirmationView" should {

    "render title" in {

      page().getTitle must containMessage("confirmation.title.SHUT_MUCR")
    }

    "render header" in {

      page()
        .getElementsByClass("govuk-heading-xl")
        .first() must containMessage("confirmation.title.SHUT_MUCR")
    }

    "render inset text with link to View Requests page" in {

      val inset = page().getElementsByClass("govuk-inset-text").first()
      inset must containMessage("confirmation.insetText")

      val link = inset.getElementsByClass("govuk-link").first()
      link must containMessage("confirmation.notification.timeline.link")
      link must haveHref(controllers.routes.SubmissionsController.displayPage())
    }
  }

  "ShutMucrConfirmationView" when {

    "ileQuery feature is disabled" should {
      "render 'Back to start' link to Choice page" in {

        when(appConfig.ileQueryEnabled).thenReturn(false)

        val link = page().getElementsByClass("govuk-link").get(1)

        link must containMessage("confirmation.redirect.choice.link")
        link must haveHref(controllers.routes.ChoiceController.displayChoiceForm())
      }
    }

    "ileQuery feature is enabled" should {
      "render 'Find another consignment' link to Find Consignment page" in {

        when(appConfig.ileQueryEnabled).thenReturn(true)

        val link = page().getElementsByClass("govuk-link").get(1)

        link must containMessage("confirmation.redirect.query.link")
        link must haveHref(controllers.ileQuery.routes.FindConsignmentController.displayQueryForm())
      }
    }
  }

}
