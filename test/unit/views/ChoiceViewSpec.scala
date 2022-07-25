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
import forms.Choice._
import forms.{Choice, UcrType}
import models.UcrBlock
import models.requests.AuthenticatedRequest
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.test.FakeRequest
import testdata.CommonTestData.validEori
import testdata.MovementsTestData.newUser
import uk.gov.hmrc.govukfrontend.views.html.components.{FormWithCSRF, GovukButton, GovukRadios}
import views.components.config.ChoicePageConfig
import views.html.choice_page
import views.html.components.gds.{errorSummary, gds_main_template, sectionHeader}
import views.tags.ViewTest
import scala.collection.JavaConverters._

@ViewTest
class ChoiceViewSpec extends ViewSpec with Injector with MockitoSugar with BeforeAndAfterEach {

  private implicit val request = AuthenticatedRequest(FakeRequest().withCSRFToken, newUser(validEori))

  private val form: Form[Choice] = Choice.form()

  private val govukLayout = instanceOf[gds_main_template]
  private val govukButton = instanceOf[GovukButton]
  private val govukRadios = instanceOf[GovukRadios]
  private val errorSummary = instanceOf[errorSummary]
  private val sectionHeader = instanceOf[sectionHeader]
  private val formHelper = instanceOf[FormWithCSRF]
  private val pageConfig = mock[ChoicePageConfig]

  private def isIleQueryEnabled(enabled: Boolean): Unit = {
    when(pageConfig.backLink(any())).thenReturn(
      if (enabled) Some(controllers.ileQuery.routes.FindConsignmentController.displayQueryForm())
      else None
    )
    when(pageConfig.ileQueryEnabled).thenReturn(enabled)
  }

  private def isUserOnArriveDepartAllowList(present: Boolean) =
    when(pageConfig.isUserPermittedArriveDepartAccess(any())).thenReturn(present)

  private val choicePage = new choice_page(govukLayout, govukButton, govukRadios, errorSummary, sectionHeader, formHelper, pageConfig)
  private def createView(form: Form[Choice] = form): Document =
    choicePage(form)(request, messages)

  override def afterEach(): Unit =
    reset(pageConfig)

  "Choice View" should {

    "have proper labels for messages" in {
      messages must haveTranslationFor("movement.choice.title")
      messages must haveTranslationFor("movement.choice.arrival.label")
      messages must haveTranslationFor("movement.choice.departure.label")
      messages must haveTranslationFor("movement.choice.associateucr.label")
      messages must haveTranslationFor("movement.choice.disassociateucr.label")
      messages must haveTranslationFor("movement.choice.shutmucr.label")
      messages must haveTranslationFor("movement.choice.submissions.label")
    }

    "have proper labels for error messages" in {
      messages must haveTranslationFor("choicePage.input.error.empty")
      messages must haveTranslationFor("choicePage.input.error.incorrectValue")
    }

    "not render 'Shut Mucr' option" when {

      "ILE query was for a Ducr" in {
        isUserOnArriveDepartAllowList(true)
        isIleQueryEnabled(true)
        val view = choicePage(Choice.form(), Some(UcrBlock("DUCR", UcrType.Ducr)))

        view.getElementsByAttributeValue("for", "choice").text() must be(messages("movement.choice.arrival.label"))
        view.getElementsByAttributeValue("for", "choice-2").text() must be(messages("movement.choice.departure.label"))
        view.getElementsByAttributeValue("for", "choice-3").text() must be(messages("movement.choice.associateucr.label"))
        view.getElementsByAttributeValue("for", "choice-4").text() must be(messages("movement.choice.disassociateucr.label"))
      }

      "user entered Choice page through Ducr Part Details page" in {
        isUserOnArriveDepartAllowList(true)
        isIleQueryEnabled(true)
        val view = choicePage(Choice.form(), Some(UcrBlock("DUCR-123X", UcrType.DucrPart)))

        view.getElementsByAttributeValue("for", "choice").text() must be(messages("movement.choice.arrival.label"))
        view.getElementsByAttributeValue("for", "choice-2").text() must be(messages("movement.choice.departure.label"))
        view.getElementsByAttributeValue("for", "choice-3").text() must be(messages("movement.choice.associateucr.label"))
        view.getElementsByAttributeValue("for", "choice-4").text() must be(messages("movement.choice.disassociateucr.label"))
      }
    }

    "render 'Shut Mucr' option" when {

      "ILE query was for a Mucr" in {
        isUserOnArriveDepartAllowList(true)
        isIleQueryEnabled(true)
        val view = choicePage(Choice.form(), Some(UcrBlock("MUCR", UcrType.Mucr)))

        view.getElementsByAttributeValue("for", "choice").text() must be(messages("movement.choice.arrival.label"))
        view.getElementsByAttributeValue("for", "choice-2").text() must be(messages("movement.choice.departure.label"))
        view.getElementsByAttributeValue("for", "choice-3").text() must be(messages("movement.choice.associateucr.label"))
        view.getElementsByAttributeValue("for", "choice-4").text() must be(messages("movement.choice.disassociateucr.label"))
        view.getElementsByAttributeValue("for", "choice-5").text() must be(messages("movement.choice.shutmucr.label"))
      }
    }
  }

  "Choice View on empty page" when {

    "ILE Query is enabled and user is on ArriveDepart allow list" should {
      isUserOnArriveDepartAllowList(true)
      isIleQueryEnabled(true)
      val view = createView(Choice.form())

      "display same page title as header" in {
        view.title() must include(view.getElementsByTag("h1").text())
      }

      "display header" in {
        view.getElementsByClass("govuk-fieldset__heading").get(0).text() must be(messages("movement.choice.title.consignment"))
      }

      "display 'Back' button" in {
        val backButton = view.getElementById("back-link")

        backButton.text() must be(messages("site.back"))
        backButton.attr("href") must be(controllers.ileQuery.routes.FindConsignmentController.displayQueryForm().url)
      }

      "display 5 radio buttons with labels" in {
        view.getElementsByAttributeValue("for", "choice").text() must be(messages("movement.choice.arrival.label"))
        view.getElementsByAttributeValue("for", "choice-2").text() must be(messages("movement.choice.departure.label"))
        view.getElementsByAttributeValue("for", "choice-3").text() must be(messages("movement.choice.associateucr.label"))
        view.getElementsByAttributeValue("for", "choice-4").text() must be(messages("movement.choice.disassociateucr.label"))
        view.getElementsByAttributeValue("for", "choice-5").text() must be(messages("movement.choice.shutmucr.label"))
        view.getElementsByTag("label").size mustBe 5
      }

      "display 5 unchecked radio buttons" in {
        view.getElementsByClass("govuk-radios__input").size mustBe 5
        view.getElementsByAttribute("checked").size mustBe 0
      }

      "display 'Save and continue' button on page" in {
        val saveButton = view.getElementsByClass("govuk-button").get(0)
        saveButton.text() must be(messages("site.continue"))
      }
    }

    "ILE Query is enabled and user is absent from ArriveDepart allow list" should {
      isUserOnArriveDepartAllowList(false)
      isIleQueryEnabled(true)
      val view = createView(Choice.form())

      "display same page title as header" in {
        view.title() must include(view.getElementsByTag("h1").text())
      }

      "display header" in {
        view.getElementsByClass("govuk-fieldset__heading").get(0).text() must be(messages("movement.choice.title.consignment"))
      }

      "display 'Back' button" in {
        val backButton = view.getElementById("back-link")

        backButton.text() must be(messages("site.back"))
        backButton.attr("href") must be(controllers.ileQuery.routes.FindConsignmentController.displayQueryForm().url)
      }

      "display 3 radio buttons with labels when user is not on allow list" in {
        view.getElementsByAttributeValue("for", "choice").text() must be(messages("movement.choice.associateucr.label"))
        view.getElementsByAttributeValue("for", "choice-2").text() must be(messages("movement.choice.disassociateucr.label"))
        view.getElementsByAttributeValue("for", "choice-3").text() must be(messages("movement.choice.shutmucr.label"))
        view.getElementsByTag("label").size mustBe 3
      }

      "display 3 unchecked radio buttons" in {
        view.getElementsByClass("govuk-radios__input").size mustBe 3
        view.getElementsByAttribute("checked").size mustBe 0
      }

      "display 'Save and continue' button on page" in {
        val saveButton = view.getElementsByClass("govuk-button").get(0)
        saveButton.text() must be(messages("site.continue"))
      }
    }

    "ILE Query is disabled and user is on ArriveDepart allow list" should {
      isUserOnArriveDepartAllowList(true)
      isIleQueryEnabled(false)
      val view = createView(Choice.form())

      "display same page title as header" in {
        view.title() must include(view.getElementsByTag("h1").text())
      }

      "display header" in {
        view.getElementsByClass("govuk-fieldset__heading").get(0).text() must be(messages("movement.choice.title"))
      }

      "not display 'Back' button" in {
        val backButton = view.getElementById("back-link")

        Option(backButton) mustBe empty
      }

      "display 6 radio buttons with labels" in {
        view.getElementsByAttributeValue("for", "choice").text() must be(messages("movement.choice.arrival.label"))
        view.getElementsByAttributeValue("for", "choice-2").text() must be(messages("movement.choice.departure.label"))
        view.getElementsByAttributeValue("for", "choice-3").text() must be(messages("movement.choice.associateucr.label"))
        view.getElementsByAttributeValue("for", "choice-4").text() must be(messages("movement.choice.disassociateucr.label"))
        view.getElementsByAttributeValue("for", "choice-5").text() must be(messages("movement.choice.shutmucr.label"))
        view.getElementsByAttributeValue("for", "choice-6").text() must be(messages("movement.choice.submissions.label"))
        view.getElementsByTag("label").size mustBe 6
      }

      "display 6 unchecked radio buttons" in {
        view.getElementsByClass("govuk-radios__input").size mustBe 6
        view.getElementsByAttribute("checked").size mustBe 0
      }

      "display 'Save and continue' button on page" in {
        val saveButton = view.getElementsByClass("govuk-button").get(0)
        saveButton.text() must be(messages("site.continue"))
      }
    }

    "ILE Query is disabled and user is absent from ArriveDepart allow list" should {
      isUserOnArriveDepartAllowList(false)
      isIleQueryEnabled(false)
      val view = createView(Choice.form())

      "display same page title as header" in {
        view.title() must include(view.getElementsByTag("h1").text())
      }

      "display header" in {
        view.getElementsByClass("govuk-fieldset__heading").get(0).text() must be(messages("movement.choice.title"))
      }

      "not display 'Back' button" in {
        val backButton = view.getElementById("back-link")

        Option(backButton) mustBe empty
      }

      "display 4 radio buttons with labels" in {
        view.getElementsByAttributeValue("for", "choice").text() must be(messages("movement.choice.associateucr.label"))
        view.getElementsByAttributeValue("for", "choice-2").text() must be(messages("movement.choice.disassociateucr.label"))
        view.getElementsByAttributeValue("for", "choice-3").text() must be(messages("movement.choice.shutmucr.label"))
        view.getElementsByAttributeValue("for", "choice-4").text() must be(messages("movement.choice.submissions.label"))
        view.getElementsByTag("label").size mustBe 4
      }

      "display 4 unchecked radio buttons" in {
        view.getElementsByClass("govuk-radios__input").size mustBe 4
        view.getElementsByAttribute("checked").size mustBe 0
      }

      "display 'Save and continue' button on page" in {
        val saveButton = view.getElementsByClass("govuk-button").get(0)
        saveButton.text() must be(messages("site.continue"))
      }
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
      isUserOnArriveDepartAllowList(true)
      isIleQueryEnabled(true)

      val view = createView(Choice.form().fill(Arrival))

      ensureRadioIsChecked(view, "choice")
      ensureRadioIsUnChecked(view, "choice-2")
      ensureRadioIsUnChecked(view, "choice-3")
      ensureRadioIsUnChecked(view, "choice-4")
      ensureRadioIsUnChecked(view, "choice-5")
    }

    "display selected 2nd radio button - Depart (EDL)" in {
      isUserOnArriveDepartAllowList(true)
      isIleQueryEnabled(true)

      val view = createView(Choice.form().fill(Departure))

      ensureRadioIsUnChecked(view, "choice")
      ensureRadioIsChecked(view, "choice-2")
      ensureRadioIsUnChecked(view, "choice-3")
      ensureRadioIsUnChecked(view, "choice-4")
      ensureRadioIsUnChecked(view, "choice-5")
    }

    "display selected 3rd radio button - Associate (EDL)" in {
      isUserOnArriveDepartAllowList(true)
      isIleQueryEnabled(true)

      val view = createView(Choice.form().fill(AssociateUCR))

      ensureRadioIsUnChecked(view, "choice")
      ensureRadioIsUnChecked(view, "choice-2")
      ensureRadioIsChecked(view, "choice-3")
      ensureRadioIsUnChecked(view, "choice-4")
      ensureRadioIsUnChecked(view, "choice-5")
    }

    "display selected 4th radio button - Disassociate (EAC)" in {
      isUserOnArriveDepartAllowList(true)
      isIleQueryEnabled(true)

      val view = createView(Choice.form().fill(DisassociateUCR))

      ensureRadioIsUnChecked(view, "choice")
      ensureRadioIsUnChecked(view, "choice-2")
      ensureRadioIsUnChecked(view, "choice-3")
      ensureRadioIsChecked(view, "choice-4")
      ensureRadioIsUnChecked(view, "choice-5")
    }

    "display selected 5th radio button - Shut a MUCR (CST)" in {
      isUserOnArriveDepartAllowList(true)
      isIleQueryEnabled(true)

      val view = createView(Choice.form().fill(ShutMUCR))

      ensureRadioIsUnChecked(view, "choice")
      ensureRadioIsUnChecked(view, "choice-2")
      ensureRadioIsUnChecked(view, "choice-3")
      ensureRadioIsUnChecked(view, "choice-4")
      ensureRadioIsChecked(view, "choice-5")
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
