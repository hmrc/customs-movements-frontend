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

import base.Injector
import forms.Choice._
import forms.{Choice, UcrType}
import models.UcrBlock
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import play.api.data.Form
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.html.components.{GovukButton, GovukRadios}
import uk.gov.hmrc.play.views.html.helpers.FormWithCSRF
import views.components.config.ChoicePageConfig
import views.html.choice_page
import views.html.components.gds.{errorSummary, gds_main_template, sectionHeader}
import views.spec.UnitViewSpec
import views.spec.UnitViewSpec.{realAppConfig, realMessagesApi}
import views.tags.ViewTest

@ViewTest
class ChoiceViewSpec extends UnitViewSpec with Injector with BeforeAndAfterEach {

  private val form: Form[Choice] = Choice.form()

  private val govukLayout = instanceOf[gds_main_template]
  private val govukButton = instanceOf[GovukButton]
  private val govukRadios = instanceOf[GovukRadios]
  private val errorSummary = instanceOf[errorSummary]
  private val sectionHeader = instanceOf[sectionHeader]
  private val formHelper = instanceOf[FormWithCSRF]
  private val pageConfig = mock[ChoicePageConfig]

  def isIleQueryEnabled(enabled: Boolean): Unit = {
    when(pageConfig.backLink(any())).thenReturn(
      if (enabled) Some(controllers.ileQuery.routes.FindConsignmentController.displayQueryForm())
      else None
    )
    when(pageConfig.ileQueryEnabled).thenReturn(enabled)
  }

  private val choicePage = new choice_page(govukLayout, govukButton, govukRadios, errorSummary, sectionHeader, formHelper, pageConfig)
  private def createView(form: Form[Choice] = form, messages: Messages = stubMessages()): Document =
    choicePage(form)(request, messages)

  override def afterEach(): Unit =
    reset(pageConfig)

  "Choice View" should {

    "have proper labels for messages" in {
      isIleQueryEnabled(true)

      val messages = messagesApi.preferred(request)

      messages must haveTranslationFor("movement.choice.title")
      messages must haveTranslationFor("movement.choice.arrival.label")
      messages must haveTranslationFor("movement.choice.departure.label")
      messages must haveTranslationFor("movement.choice.associateucr.label")
      messages must haveTranslationFor("movement.choice.disassociateucr.label")
      messages must haveTranslationFor("movement.choice.shutmucr.label")
      messages must haveTranslationFor("movement.choice.submissions.label")
    }

    "have proper labels for error messages" in {
      isIleQueryEnabled(true)

      val messages = messagesApi.preferred(request)

      messages must haveTranslationFor("choicePage.input.error.empty")
      messages must haveTranslationFor("choicePage.input.error.incorrectValue")
    }

    "not render 'Shut Mucr' option" when {

      "ILE query was for a Ducr" in {
        isIleQueryEnabled(true)

        val view = choicePage(Choice.form(), Some(UcrBlock("DUCR", UcrType.Ducr)))

        view.getElementsByAttributeValue("for", "choice").text() must be(messages("movement.choice.arrival.label"))
        view.getElementsByAttributeValue("for", "choice-2").text() must be(messages("movement.choice.associateucr.label"))
        view.getElementsByAttributeValue("for", "choice-3").text() must be(messages("movement.choice.disassociateucr.label"))
        view.getElementsByAttributeValue("for", "choice-4").text() must be(messages("movement.choice.departure.label"))
      }
    }

    "render 'Shut Mucr' option" when {

      "ILE query was for a Mucr" in {
        isIleQueryEnabled(true)

        val view = choicePage(Choice.form(), Some(UcrBlock("MUCR", UcrType.Mucr)))

        view.getElementsByAttributeValue("for", "choice").text() must be(messages("movement.choice.arrival.label"))
        view.getElementsByAttributeValue("for", "choice-2").text() must be(messages("movement.choice.associateucr.label"))
        view.getElementsByAttributeValue("for", "choice-3").text() must be(messages("movement.choice.disassociateucr.label"))
        view.getElementsByAttributeValue("for", "choice-4").text() must be(messages("movement.choice.shutmucr.label"))
        view.getElementsByAttributeValue("for", "choice-5").text() must be(messages("movement.choice.departure.label"))
      }
    }
  }

  "Choice View on empty page" should {

    "display same page title as header with ile query disabled" in {
      isIleQueryEnabled(false)

      val viewWithMessage = createView(messages = realMessagesApi.preferred(request))
      viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
    }

    "display same page title as header with ile query enabled" in {
      isIleQueryEnabled(true)

      val viewWithMessage = createView(messages = realMessagesApi.preferred(request))
      viewWithMessage.title() must include(viewWithMessage.getElementsByTag("h1").text())
    }

    "display header with ile query disabled" in {
      isIleQueryEnabled(false)

      createView().getElementsByClass("govuk-fieldset__heading").get(0).text() must be(messages("movement.choice.title"))
    }

    "display header with ile query enabled" in {
      isIleQueryEnabled(true)

      createView().getElementsByClass("govuk-fieldset__heading").get(0).text() must be(messages("movement.choice.title.consignment"))
    }

    "display 'Back' button when ile query enabled" in {
      isIleQueryEnabled(true)

      val backButton = createView().getElementById("back-link")

      backButton.text() must be(messages("site.back"))
      backButton.attr("href") must be(controllers.ileQuery.routes.FindConsignmentController.displayQueryForm().url)
    }

    "not display 'Back' button when ile query disabled" in {
      isIleQueryEnabled(false)

      val backButton = createView().getElementById("back-link")

      backButton must be(null)
    }

    "display 5 radio buttons with labels when ileQuery is enabled" in {
      isIleQueryEnabled(true)

      val view = createView(Choice.form())

      view.getElementsByAttributeValue("for", "choice").text() must be(messages("movement.choice.arrival.label"))
      view.getElementsByAttributeValue("for", "choice-2").text() must be(messages("movement.choice.associateucr.label"))
      view.getElementsByAttributeValue("for", "choice-3").text() must be(messages("movement.choice.disassociateucr.label"))
      view.getElementsByAttributeValue("for", "choice-4").text() must be(messages("movement.choice.shutmucr.label"))
      view.getElementsByAttributeValue("for", "choice-5").text() must be(messages("movement.choice.departure.label"))
      view.getElementsByAttributeValue("for", "choice-6") must be(empty)
    }

    "display 6 radio buttons with labels when ileQuery is disabled" in {
      isIleQueryEnabled(false)

      val view = createView(Choice.form())

      view.getElementsByAttributeValue("for", "choice").text() must be(messages("movement.choice.arrival.label"))
      view.getElementsByAttributeValue("for", "choice-2").text() must be(messages("movement.choice.associateucr.label"))
      view.getElementsByAttributeValue("for", "choice-3").text() must be(messages("movement.choice.disassociateucr.label"))
      view.getElementsByAttributeValue("for", "choice-4").text() must be(messages("movement.choice.shutmucr.label"))
      view.getElementsByAttributeValue("for", "choice-5").text() must be(messages("movement.choice.departure.label"))
      view.getElementsByAttributeValue("for", "choice-6").text() must be(messages("movement.choice.submissions.label"))
    }

    "display 4 unchecked radio buttons" in {
      isIleQueryEnabled(true)

      val view = createView(Choice.form())

      ensureRadioIsUnChecked(view, "choice")
      ensureRadioIsUnChecked(view, "choice-2")
      ensureRadioIsUnChecked(view, "choice-3")
      ensureRadioIsUnChecked(view, "choice-4")
    }

    "display 'Save and continue' button on page" in {
      isIleQueryEnabled(true)

      val view = createView()

      val saveButton = view.getElementsByClass("govuk-button").get(0)
      saveButton.text() must be(messages("site.continue"))
    }
  }

  "Choice View for invalid input" should {

    "display error when no choice is made" in {
      isIleQueryEnabled(true)

      val view = createView(Choice.form().bind(Map[String, String]()))

      view must haveGovUkGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#choice")

      view.getElementsByClass("govuk-list govuk-error-summary__list").get(0).text() must be(messages("choicePage.input.error.empty"))
    }

    "display error when choice is incorrect" in {
      isIleQueryEnabled(true)

      val view = createView(Choice.form().bind(Map("choice" -> "incorrect")))

      view must haveGovUkGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#choice")

      view.getElementsByClass("govuk-list govuk-error-summary__list").get(0).text() must be(messages("choicePage.input.error.incorrectValue"))
    }
  }

  "Choice View when filled" should {

    "display selected 1st radio button - Arrival (EAL)" in {
      isIleQueryEnabled(true)

      val view = createView(Choice.form().fill(Arrival))

      ensureRadioIsChecked(view, "choice")
      ensureRadioIsUnChecked(view, "choice-2")
      ensureRadioIsUnChecked(view, "choice-3")
      ensureRadioIsUnChecked(view, "choice-4")
    }

    "display selected 2nd radio button - Associate (EDL)" in {
      isIleQueryEnabled(true)

      val view = createView(Choice.form().fill(AssociateUCR))

      ensureRadioIsUnChecked(view, "choice")
      ensureRadioIsChecked(view, "choice-2")
      ensureRadioIsUnChecked(view, "choice-3")
      ensureRadioIsUnChecked(view, "choice-4")
    }

    "display selected 3rd radio button - Disassociate (EAC)" in {
      isIleQueryEnabled(true)

      val view = createView(Choice.form().fill(DisassociateUCR))

      ensureRadioIsUnChecked(view, "choice")
      ensureRadioIsUnChecked(view, "choice-2")
      ensureRadioIsChecked(view, "choice-3")
      ensureRadioIsUnChecked(view, "choice-4")
    }

    "display selected 4th radio button - Shut a MUCR (CST)" in {
      isIleQueryEnabled(true)

      val view = createView(Choice.form().fill(ShutMUCR))

      ensureRadioIsUnChecked(view, "choice")
      ensureRadioIsUnChecked(view, "choice-2")
      ensureRadioIsUnChecked(view, "choice-3")
      ensureRadioIsChecked(view, "choice-4")
    }
  }

  private def ensureRadioIsChecked(view: Document, elementId: String): Unit = {
    val option = view.getElementById(elementId).getElementsByAttribute("checked")
    option.size() mustBe 1
  }

  private def ensureRadioIsUnChecked(view: Document, elementId: String): Unit = {
    val option = view.getElementById(elementId)
    option.attr("checked") mustBe empty
  }
}
