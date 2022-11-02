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

import base.Injector
import controllers.routes
import controllers.routes.SubmissionsController
import forms.ConsignmentReferences
import models.cache._
import views.html.confirmation_page
import views.tags.ViewTest

@ViewTest
class ConfirmationPageViewSpec extends ViewSpec with Injector {

  private val page = instanceOf[confirmation_page]
  private val dummyUcr = "dummyUcr"

  "ConfirmationPageView" when {

    List(ArrivalAnswers(), DepartureAnswers(), AssociateUcrAnswers(), DisassociateUcrAnswers(), ShutMucrAnswers()).foreach { answer =>
      s"provided with ${answer.`type`} Journey Type" should {
        implicit val request = journeyRequest(answer)
        val view = page(answer.`type`, Some(dummyUcr))

        "render title" in {
          view.getTitle must containMessage(s"confirmation.title.${answer.`type`}")
        }

        "render header" in {
          view.getElementsByClass("govuk-heading-xl").first() must containMessage(s"confirmation.title.${answer.`type`}")
        }

        "render inset text with link to View Requests page" in {
          val inset = view.getElementsByClass("govuk-inset-text").first()
          inset must containMessage("confirmation.insetText", messages("confirmation.notification.timeline.link"))

          val link = inset.getElementsByClass("govuk-link").first()
          link must haveHref(SubmissionsController.displayPage)
        }

        "render sub-heading" in {
          val subHeading = view.getElementsByClass("govuk-heading-m").first()
          subHeading must containMessage("confirmation.subheading")
        }

        "render link to consignment information" in {
          val link = view.getElementById("summary-link")

          link must containMessage("confirmation.redirect.summary.link")
          link must haveHref(controllers.ileQuery.routes.IleQueryController.getConsignmentData(dummyUcr))
        }

        "render link to choice page" in {
          val link = view.getElementById("choice-link")

          link must containMessage("confirmation.redirect.choice.link")
          link must haveHref(controllers.routes.ChoiceController.displayChoices)
        }

        "render Exit Survey link" in {
          view.getElementById("exit-survey") must containMessage("exitSurvey.header")
        }

        "hide language switch" in {
          view.getElementsByClass("hmrc-language-select").text() must be(empty)
        }
      }
    }
  }
}
