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

import base.{Injector, OverridableInjector}
import config.IleQueryConfig
import controllers.actions.ArriveDepartAllowList
import controllers.routes.ChoiceController
import forms.Choice
import forms.Choice._
import models.requests.AuthenticatedRequest
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import testdata.CommonTestData.validEori
import testdata.MovementsTestData.newUser
import views.html.choice
import views.tags.ViewTest

@ViewTest
class ChoiceViewSpec extends ViewSpec with BeforeAndAfterEach with Injector with MockitoSugar {

  private val arriveDepartAllowList = mock[ArriveDepartAllowList]
  private val ileQueryConfig = mock[IleQueryConfig]

  private val injector =
    new OverridableInjector(bind[ArriveDepartAllowList].toInstance(arriveDepartAllowList), bind[IleQueryConfig].toInstance(ileQueryConfig))

  private val choicePage = injector.instanceOf[choice]

  def setIleQuery(enabled: Boolean): Unit = when(ileQueryConfig.isIleQueryEnabled).thenReturn(enabled)
  def setArriveDepartAllowList(present: Boolean): Unit = when(arriveDepartAllowList.contains(any())).thenReturn(present)

  override def beforeEach(): Unit = {
    super.beforeEach()

    setIleQuery(true)
    setArriveDepartAllowList(true)
  }

  override def afterEach(): Unit = {
    reset(arriveDepartAllowList, ileQueryConfig)
    super.afterEach()
  }

  private implicit val request = AuthenticatedRequest(FakeRequest().withCSRFToken, newUser(validEori))

  def createView(f: Form[Choice] = form): Document = choicePage(f)

  "Choice View" should {

    "have proper labels for error messages" in {
      messages must haveTranslationFor("choicePage.input.error.empty")
      messages must haveTranslationFor("choicePage.input.error.incorrectValue")
    }

    val view = createView()

    "have a form for submission to ChoiceController" in {
      val element = view.getElementsByTag("form").get(0)
      element.attr("method").toUpperCase mustBe "POST"
      element.attr("action") mustBe ChoiceController.displayChoices.url
    }

    "display same page title as header" in {
      view.title() must include(view.getElementsByTag("h1").text())
    }

    "display the expected header" in {
      view.getElementsByTag("h1").text() mustBe messages("movement.choice.title")
    }

    "display 'Save and continue' button on page" in {
      view.getElementsByClass("govuk-button").get(0).text() must be(messages("site.continue"))
    }
  }

  "Choice View" should {

    "render the choices in the expected order depending on the configuration flags" in {
      List((true, true), (true, false), (false, true), (false, false)).foreach { case (arriveDepartAllowListFlag, ileQueryFlag) =>
        setArriveDepartAllowList(arriveDepartAllowListFlag)
        setIleQuery(ileQueryFlag)

        allChoices
          .filterNot(choice => !arriveDepartAllowListFlag && (choice.isArrival || choice.isDeparture))
          .filterNot(!ileQueryFlag && _.isFindConsignment)
          .zipWithIndex
          .foreach { case (choice, index) =>
            val element = createView().getElementsByAttributeValue("value", choice.value).get(0)
            element.tagName() mustBe "input"
            val ix = if (index == 0) "" else s"-${index + 1}"
            element.id() mustBe s"choice$ix"
            element.lastElementSibling().text() mustBe messages(s"movement.choice.${choice.value.toLowerCase}.label")
          }
      }
    }

    "display error when no choice is made" in {
      val view = createView(form.bind(Map[String, String]()))

      view must haveGovUkGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#choice")

      val element = view.getElementsByClass("govuk-list govuk-error-summary__list").get(0)
      element.text() must be(messages("choicePage.input.error.empty"))
    }

    "display error when choice is incorrect" in {
      val view = createView(form.bind(Map("choice" -> "incorrect")))

      view must haveGovUkGlobalErrorSummary
      view must containErrorElementWithTagAndHref("a", "#choice")

      val element = view.getElementsByClass("govuk-list govuk-error-summary__list").get(0)
      element.text() must be(messages("choicePage.input.error.incorrectValue"))
    }
  }
}
