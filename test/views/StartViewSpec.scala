/*
 * Copyright 2019 HM Revenue & Customs
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

import play.twirl.api.Html
import views.html.start_page
import views.spec.UnitViewSpec
import views.tags.ViewTest

@ViewTest
class StartViewSpec extends UnitViewSpec {

  private val page = new start_page(mainTemplate)
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

    "display title" in {
      view.select("title").text() mustBe "startPage.title"
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

      view.getElementById("contents-element-1").text() mustBe "startPage.beforeYouStart.header"
      view.getElementById("contents-element-2").text() mustBe "startPage.informationYouNeed.header"
      view.getElementById("contents-element-3").text() mustBe "startPage.reportYourArrivalAndDeparture.header"
    }

    "contain links in Contents section" in {
      view.getElementById("contents-element-1").child(0) must haveHref("#before-you-start")
      view.getElementById("contents-element-2").child(0) must haveHref("#information-you-need")
      view.getElementById("contents-element-3").child(0) must haveHref("#report-your-arrival-and-departure")
    }

    "display 'Before you start' section" in {
      view.getElementById("before-you-start").text() mustBe "startPage.beforeYouStart.header"

      view.getElementById("before-you-start-line-1").text() must include("startPage.beforeYouStart.line.1")
      view.getElementById("before-you-start-line-1").text() must include("startPage.beforeYouStart.line.1.link")
      view.getElementById("before-you-start-line-2").text() mustBe "startPage.beforeYouStart.line.2"
      view.getElementById("before-you-start-line-3").text() mustBe "startPage.beforeYouStart.line.3"
    }

    "contain link to Customs Declarations Guidance in 'Before you start' section" in {
      view.getElementById("before-you-start-line-1").child(0) must haveHref(
        "https://www.gov.uk/guidance/customs-declarations-for-goods-taken-out-of-the-eu"
      )
    }

    "display 'Information you need' section" in {
      view.getElementById("information-you-need").text() mustBe "startPage.informationYouNeed.header"

      view.getElementById("information-you-need-line-1").text() mustBe "startPage.informationYouNeed.line.1"
      view.getElementById("information-you-need-listItem-1").text() mustBe "startPage.informationYouNeed.listItem.1"
      view.getElementById("information-you-need-listItem-2").text() mustBe "startPage.informationYouNeed.listItem.2"
      view.getElementById("information-you-need-listItem-3").text() mustBe "startPage.informationYouNeed.listItem.3"
      view.getElementById("information-you-need-listItem-4").text() mustBe "startPage.informationYouNeed.listItem.4"
    }

    "display 'Report your arrival and departure' section" in {
      view
        .getElementById("report-your-arrival-and-departure")
        .text() mustBe "startPage.reportYourArrivalAndDeparture.header"
    }

    "display problems with service notice" in {
      view.getElementById("problems-with-service-notice").text() must include("startPage.problemsWithServiceNotice")
      view.getElementById("problems-with-service-notice").text() must include(
        "startPage.problemsWithServiceNotice.link"
      )
    }

    "contain link to service availability in 'Report your arrival and departure' section" in {
      view.getElementById("problems-with-service-notice").child(0) must haveHref(
        "https://www.gov.uk/guidance/customs-declaration-service-service-availability-and-issues"
      )
    }

    "display 'Start now' button" in {
      view.getElementById("button-start").text() mustBe "startPage.buttonName"
      view.getElementById("button-start") must haveHref(controllers.routes.ChoiceController.displayChoiceForm())
    }

    "display link to go back to Contents section" in {
      view.getElementById("back-to-top").text() mustBe "startPage.contents.header"
      view.getElementById("back-to-top").child(0) must haveHref("#contents")
    }
  }

}
