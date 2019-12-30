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
import play.twirl.api.Html
import views.html.start_page
import views.spec.UnitViewSpec
import views.tags.ViewTest

@ViewTest
class StartViewSpec extends UnitViewSpec with Injector {

  private val page = instanceOf[start_page]
  private def createView(): Html = page()(request, messages)

  "Start Page view" should {

    "have proper messages for labels" in {
      val messages = messagesApi.preferred(request)
      messages must haveTranslationFor("startPage.title.sectionHeader")
      messages must haveTranslationFor("startPage.title")
      messages must haveTranslationFor("startPage.description")
      messages must haveTranslationFor("startPage.contents.header")
      messages must haveTranslationFor("startPage.beforeYouStart.header")
      messages must haveTranslationFor("startPage.beforeYouStart.line.1")
      messages must haveTranslationFor("startPage.beforeYouStart.line.1.link")
      messages must haveTranslationFor("startPage.beforeYouStart.line.2")
      messages must haveTranslationFor("startPage.beforeYouStart.line.3")
      messages must haveTranslationFor("startPage.informationYouNeed.header")
      messages must haveTranslationFor("startPage.informationYouNeed.line.1")
      messages must haveTranslationFor("startPage.informationYouNeed.listItem.1")
      messages must haveTranslationFor("startPage.informationYouNeed.listItem.2")
      messages must haveTranslationFor("startPage.informationYouNeed.listItem.3")
      messages must haveTranslationFor("startPage.informationYouNeed.listItem.4")
      messages must haveTranslationFor("startPage.reportYourArrivalAndDeparture.header")
      messages must haveTranslationFor("startPage.problemsWithServiceNotice")
      messages must haveTranslationFor("startPage.problemsWithServiceNotice.link")
      messages must haveTranslationFor("startPage.buttonName")
    }

    val view = createView()

    "display same page title as header" in {

      val view = page()(request, messagesApi.preferred(request))
      view.title() must include(view.getElementsByTag("h1").text())
    }

    "display section header" in {
      view.getElementById("section-header").text() mustBe "startPage.title.sectionHeader"
    }

    "display header" in {
      view.getElementById("title").text() mustBe "startPage.title"
    }

    "display general description" in {
      view.getElementById("description").text() mustBe "startPage.description"
    }

    "display Contents section" in {
      view.getElementById("contents").text() mustBe "startPage.contents.header"

      view.getElementById("contents-list") must haveChildCount(3)
      view.getElementById("contents-list").child(0).text() mustBe "startPage.beforeYouStart.header"
      view.getElementById("contents-list").child(1).text() mustBe "startPage.informationYouNeed.header"
      view.getElementById("contents-list").child(2).text() mustBe "startPage.reportYourArrivalAndDeparture.header"
    }

    "contain links in Contents section" in {
      view.getElementById("contents-list").child(0).child(0) must haveHref("#before-you-start")
      view.getElementById("contents-list").child(1).child(0) must haveHref("#information-you-need")
      view.getElementById("contents-list").child(2).child(0) must haveHref("#report-your-arrival-and-departure")
    }

    "display 'Before you start' section" in {
      view.getElementById("before-you-start").text() mustBe "startPage.beforeYouStart.header"

      view.getElementById("before-you-start-element-1").text() must include("startPage.beforeYouStart.line.1")
      view.getElementById("before-you-start-element-2").text() mustBe "startPage.beforeYouStart.line.2"
      view.getElementById("before-you-start-element-3").text() mustBe "startPage.beforeYouStart.line.3"
    }

    "contain link to Customs Declarations Guidance in 'Before you start' section" in {
      val view = page()(request, messagesApi.preferred(request))
      view.getElementById("before-you-start-element-1").child(0) must haveHref(
        "https://www.gov.uk/guidance/customs-declarations-for-goods-taken-out-of-the-eu"
      )
    }

    "display 'Information you need' section" in {
      view.getElementById("information-you-need").text() mustBe "startPage.informationYouNeed.header"

      view.getElementById("information-you-need-element-1").text() mustBe "startPage.informationYouNeed.line.1"

      view.getElementById("information-you-need-list") must haveChildCount(4)
      view.getElementById("information-you-need-list").child(0).text() mustBe "startPage.informationYouNeed.listItem.1"
      view.getElementById("information-you-need-list").child(1).text() mustBe "startPage.informationYouNeed.listItem.2"
      view.getElementById("information-you-need-list").child(2).text() mustBe "startPage.informationYouNeed.listItem.3"
      view.getElementById("information-you-need-list").child(3).text() mustBe "startPage.informationYouNeed.listItem.4"
    }

    "display 'Report your arrival and departure' section" in {
      view
        .getElementById("report-your-arrival-and-departure")
        .text() mustBe "startPage.reportYourArrivalAndDeparture.header"
    }

    "display problems with service notice" in {
      view.getElementById("problems-with-service-notice").text() must include("startPage.problemsWithServiceNotice")
    }

    "contain link to service availability in 'Report your arrival and departure' section" in {
      val view = page()(request, messagesApi.preferred(request))
      view.getElementById("problems-with-service-notice").child(0) must haveHref(
        "https://www.gov.uk/guidance/customs-declaration-service-service-availability-and-issues"
      )
    }

    "display 'Start now' button" in {
      view.getElementsByClass("govuk-button govuk-button--start").get(0).text() mustBe "startPage.buttonName"
      view.getElementsByClass("govuk-button govuk-button--start").get(0) must haveHref(controllers.routes.ChoiceController.displayChoiceForm())
    }

    "display link to go back to Contents section" in {
      view.getElementById("back-to-top").text() mustBe "startPage.contents.header"
      view.getElementById("back-to-top").child(0) must haveHref("#contents")
    }
  }

}
