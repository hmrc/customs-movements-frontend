/*
 * Copyright 2023 HM Revenue & Customs
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

package views.summary

import base.Injector
import controllers.ileQuery.routes.IleQueryController
import controllers.routes.{ChoiceController, NotificationsController}
import forms.UcrType.{Ducr, DucrPart, Mucr}
import forms.{ConsignmentReferences, UcrType}
import models.cache._
import models.confirmation.Confirmation
import views.ViewSpec
import views.html.summary.confirmation_page
import views.tags.ViewTest

@ViewTest
class ConfirmationPageViewSpec extends ViewSpec with Injector {

  private val page = instanceOf[confirmation_page]

  private val conversationId = "conversationId"
  private val dummyUcr = "dummyUcr"

  private val call = IleQueryController.getConsignmentData(dummyUcr)

  private def consignmentRefs(ucrType: UcrType = Ducr) = ConsignmentReferences(ucrType, dummyUcr)

  "Confirmation page" when {

    List(ArrivalAnswers(), DepartureAnswers(), AssociateUcrAnswers(), DisassociateUcrAnswers(), ShutMucrAnswers()).foreach { answer =>
      s"provided with ${answer.`type`} Journey Type" should {
        implicit val request = journeyRequest(answer)
        val view = page(Confirmation(answer.`type`, conversationId, Some(consignmentRefs()), None))

        "render title" in {
          view.getTitle must containMessage(s"confirmation.title.${answer.`type`}")
        }

        "render panel with heading" in {
          Option(view.getElementsByClass("govuk-panel").first()) mustBe defined
          view.getElementsByTag("h1").first() must containMessage(s"confirmation.title.${answer.`type`}")
        }

        "render body text with link to View Requests page" in {
          val bodyText = view.getElementsByClass("govuk-body").first()
          bodyText must containMessage("confirmation.bodyText", messages("confirmation.notification.timeline.link"))

          val link = bodyText.getElementsByClass("govuk-link").first()
          link must haveHref(NotificationsController.listOfNotifications(conversationId))
        }

        "render sub-heading" in {
          val subHeading = view.getElementsByClass("govuk-heading-m").first()
          subHeading must containMessage("confirmation.subheading")
        }

        "render link to choice page" in {
          val link = view.getElementById("choice-link")

          link must containMessage("confirmation.redirect.choice.link")
          link must haveHref(ChoiceController.displayChoices)
        }

        "render Exit Survey link" in {
          view.getElementById("exit-survey") must containMessage("exitSurvey.header")
        }

        "hide language switch" in {
          view.getElementsByClass("hmrc-language-select").text() must be("English Newid yr iaith ir Gymraeg Cymraeg")
        }

        "display a 'Print' button" in {
          view.getElementsByClass("gem-c-print-link__button").size() mustBe 1
        }
      }
    }

    for {
      ucrType <- List(Mucr, Ducr, DucrPart)
      answer <- List(ArrivalAnswers(), DepartureAnswers(), DisassociateUcrAnswers())
    }
      s"provided with ${answer.`type`} Journey Type and $ucrType" should {
        implicit val request = journeyRequest(answer)
        val view = page(Confirmation(answer.`type`, conversationId, Some(consignmentRefs(ucrType)), None))

        "render table with one row" in {
          view.getElementsByClass("govuk-table__row").size mustBe 1
        }

        "render a row with DUCR or MUCR based on input" in {
          ucrType match {
            case Ducr | DucrPart => view.getElementsByClass("govuk-table__cell").first must containMessage("confirmation.D")
            case Mucr            => view.getElementsByClass("govuk-table__cell").first() must containMessage("confirmation.MUCR")
          }

          view.getElementsByClass("govuk-table__cell").get(1) must containText(dummyUcr)
          view.getElementsByClass("govuk-table__cell").get(1).child(0) must haveHref(call)
        }
      }

    for {
      ucrType <- List(Mucr, Ducr, DucrPart)
      answer <- List(AssociateUcrAnswers())
    } s"provided with ${answer.`type`} Journey Type and $ucrType" should {
      implicit val request = journeyRequest(answer)
      val view = page(Confirmation(answer.`type`, conversationId, Some(consignmentRefs(ucrType)), Some(dummyUcr)))

      "render table with two rows" in {
        view.getElementsByClass("govuk-table__row").size mustBe 2
      }

      "render a row with DUCR or MUCR based on input" in {
        ucrType match {
          case Ducr | DucrPart => view.getElementsByClass("govuk-table__cell").first must containMessage("confirmation.D")
          case Mucr            => view.getElementsByClass("govuk-table__cell").first() must containMessage("confirmation.MUCR")
        }

        view.getElementsByClass("govuk-table__cell").get(1) must containText(dummyUcr)
        view.getElementsByClass("govuk-table__cell").get(1).child(0) must haveHref(call)
      }

      "render a second row with MUCR" in {
        view.getElementsByClass("govuk-table__cell").get(2) must containMessage("confirmation.MUCR")
        view.getElementsByClass("govuk-table__cell").get(3) must containText(dummyUcr)
        view.getElementsByClass("govuk-table__cell").get(3).child(0) must haveHref(call)
      }
    }
  }

  for {
    ucrType <- List(Mucr)
    answer <- List(ShutMucrAnswers())
  } s"provided with ${answer.`type`} Journey Type" should {
    implicit val request = journeyRequest(answer)
    val view = page(Confirmation(answer.`type`, conversationId, Some(consignmentRefs(ucrType)), None))

    "render table with one row" in {
      view.getElementsByClass("govuk-table__row").size mustBe 1
    }

    "render a row with MUCR based on input" in {
      view.getElementsByClass("govuk-table__cell").first() must containMessage("confirmation.MUCR")
      view.getElementsByClass("govuk-table__cell").get(1) must containText(dummyUcr)
      view.getElementsByClass("govuk-table__cell").get(1).child(0) must haveHref(call)
    }
  }
}
