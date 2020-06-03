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

package views

import base.OverridableInjector
import config.AppConfig
import models.cache.JourneyType
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import views.html.confirmation_page
import views.tags.ViewTest

@ViewTest
class ConfirmationPageViewSpec extends ViewSpec with MockitoSugar with BeforeAndAfterEach {

  private implicit val request = FakeRequest().withCSRFToken

  private val appConfig = mock[AppConfig]
  private val injector = new OverridableInjector(bind[AppConfig].toInstance(appConfig))

  private val page = injector.instanceOf[confirmation_page]

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(appConfig.ileQueryEnabled).thenReturn(false)
  }

  override def afterEach(): Unit = {
    reset(appConfig)

    super.afterEach()
  }

  "ConfirmationPageView" should {

    "render title" when {

      "provided with ARRIVE Journey Type" in {

        page(JourneyType.ARRIVE).getTitle must containMessage("confirmation.title.ARRIVE")
      }

      "provided with DEPART Journey Type" in {

        page(JourneyType.DEPART).getTitle must containMessage("confirmation.title.DEPART")
      }

      "provided with ASSOCIATE_UCR Journey Type" in {

        page(JourneyType.ASSOCIATE_UCR).getTitle must containMessage("confirmation.title.ASSOCIATE_UCR")
      }

      "provided with DISSOCIATE_UCR Journey Type" in {

        page(JourneyType.DISSOCIATE_UCR).getTitle must containMessage("confirmation.title.DISSOCIATE_UCR")
      }

      "provided with SHUT_MUCR Journey Type" in {

        page(JourneyType.SHUT_MUCR).getTitle must containMessage("confirmation.title.SHUT_MUCR")
      }
    }

    "render header" when {

      "provided with ARRIVE Journey Type" in {

        page(JourneyType.ARRIVE)
          .getElementsByClass("govuk-heading-xl")
          .first() must containMessage("confirmation.title.ARRIVE")
      }

      "provided with DEPART Journey Type" in {

        page(JourneyType.DEPART)
          .getElementsByClass("govuk-heading-xl")
          .first() must containMessage("confirmation.title.DEPART")
      }

      "provided with ASSOCIATE_UCR Journey Type" in {

        page(JourneyType.ASSOCIATE_UCR)
          .getElementsByClass("govuk-heading-xl")
          .first() must containMessage("confirmation.title.ASSOCIATE_UCR")
      }

      "provided with DISSOCIATE_UCR Journey Type" in {

        page(JourneyType.DISSOCIATE_UCR)
          .getElementsByClass("govuk-heading-xl")
          .first() must containMessage("confirmation.title.DISSOCIATE_UCR")
      }

      "provided with SHUT_MUCR Journey Type" in {

        page(JourneyType.SHUT_MUCR)
          .getElementsByClass("govuk-heading-xl")
          .first() must containMessage("confirmation.title.SHUT_MUCR")
      }
    }

    "render inset text with link to View Requests page" in {

      val inset = page(JourneyType.ARRIVE).getElementsByClass("govuk-inset-text").first()
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

        val link = page(JourneyType.ARRIVE).getElementsByClass("govuk-link").get(2)

        link must containMessage("confirmation.redirect.choice.link")
        link must haveHref(controllers.routes.ChoiceController.displayChoiceForm())
      }
    }

    "ileQuery feature is enabled" should {
      "render 'Find another consignment' link to Find Consignment page" in {

        when(appConfig.ileQueryEnabled).thenReturn(true)

        val link = page(JourneyType.ARRIVE).getElementsByClass("govuk-link").get(2)

        link must containMessage("confirmation.redirect.query.link")
        link must haveHref(controllers.ileQuery.routes.FindConsignmentController.displayQueryForm())
      }
    }
  }

}
