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

package views

import base.OverridableInjector
import models.cache._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.twirl.api.{Html, HtmlFormat}
import views.html.confirmation_page
import views.tags.ViewTest

@ViewTest
class ConfirmationPageViewSpec extends ViewSpec with MockitoSugar with BeforeAndAfterEach {

  private val confirmationPageConfig = mock[ConfirmationPageConfig]
  private val injector = new OverridableInjector(bind[ConfirmationPageConfig].toInstance(confirmationPageConfig))

  private val page = injector.instanceOf[confirmation_page]

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(confirmationPageConfig.nextStepLink()(any())).thenReturn(HtmlFormat.empty)
  }

  override def afterEach(): Unit = {
    reset(confirmationPageConfig)

    super.afterEach()
  }

  "ConfirmationPageView" should {

    "render title" when {

      "provided with ARRIVE Journey Type" in {

        implicit val request = journeyRequest(ArrivalAnswers())

        page(JourneyType.ARRIVE).getTitle must containMessage("confirmation.title.ARRIVE")
      }

      "provided with DEPART Journey Type" in {

        implicit val request = journeyRequest(DepartureAnswers())

        page(JourneyType.DEPART).getTitle must containMessage("confirmation.title.DEPART")
      }

      "provided with ASSOCIATE_UCR Journey Type" in {

        implicit val request = journeyRequest(AssociateUcrAnswers())

        page(JourneyType.ASSOCIATE_UCR).getTitle must containMessage("confirmation.title.ASSOCIATE_UCR")
      }

      "provided with DISSOCIATE_UCR Journey Type" in {

        implicit val request = journeyRequest(DisassociateUcrAnswers())

        page(JourneyType.DISSOCIATE_UCR).getTitle must containMessage("confirmation.title.DISSOCIATE_UCR")
      }

      "provided with SHUT_MUCR Journey Type" in {

        implicit val request = journeyRequest(ShutMucrAnswers())

        page(JourneyType.SHUT_MUCR).getTitle must containMessage("confirmation.title.SHUT_MUCR")
      }
    }

    "render header" when {

      "provided with ARRIVE Journey Type" in {

        implicit val request = journeyRequest(ArrivalAnswers())

        page(JourneyType.ARRIVE)
          .getElementsByClass("govuk-heading-xl")
          .first() must containMessage("confirmation.title.ARRIVE")
      }

      "provided with DEPART Journey Type" in {

        implicit val request = journeyRequest(DepartureAnswers())

        page(JourneyType.DEPART)
          .getElementsByClass("govuk-heading-xl")
          .first() must containMessage("confirmation.title.DEPART")
      }

      "provided with ASSOCIATE_UCR Journey Type" in {

        implicit val request = journeyRequest(AssociateUcrAnswers())

        page(JourneyType.ASSOCIATE_UCR)
          .getElementsByClass("govuk-heading-xl")
          .first() must containMessage("confirmation.title.ASSOCIATE_UCR")
      }

      "provided with DISSOCIATE_UCR Journey Type" in {

        implicit val request = journeyRequest(DisassociateUcrAnswers())

        page(JourneyType.DISSOCIATE_UCR)
          .getElementsByClass("govuk-heading-xl")
          .first() must containMessage("confirmation.title.DISSOCIATE_UCR")
      }

      "provided with SHUT_MUCR Journey Type" in {

        implicit val request = journeyRequest(ShutMucrAnswers())

        page(JourneyType.SHUT_MUCR)
          .getElementsByClass("govuk-heading-xl")
          .first() must containMessage("confirmation.title.SHUT_MUCR")
      }
    }

    "render inset text with link to View Requests page" in {

      implicit val request = journeyRequest(ArrivalAnswers())

      val inset = page(JourneyType.ARRIVE).getElementsByClass("govuk-inset-text").first()
      inset must containMessage("confirmation.insetText")

      val link = inset.getElementsByClass("govuk-link").first()
      link must containMessage("confirmation.notification.timeline.link")
      link must haveHref(controllers.routes.SubmissionsController.displayPage())
    }

    "render Exit Survey link" in {
      implicit val request = journeyRequest(ArrivalAnswers())
      val exitSurvey = page(JourneyType.ARRIVE).getElementById("exit-survey")

      exitSurvey must containMessage("exitSurvey.header")
    }

    "render link returned by ConfirmationPageConfig" in {

      implicit val request = journeyRequest(ArrivalAnswers())

      val testConfirmationLink = Html("""<div class="govuk-link">Test Confirmation Link</div>""")
      when(confirmationPageConfig.nextStepLink()(any())).thenReturn(testConfirmationLink)

      val link = page(JourneyType.ARRIVE).getElementsByClass("govuk-link").get(2)

      link.text must include("Test Confirmation Link")
    }
  }

}
