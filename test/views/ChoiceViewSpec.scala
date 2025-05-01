/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.actions.ArriveDepartAllowList
import forms.Choice
import forms.Choice._
import models.requests.AuthenticatedRequest
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, reset, when}
import org.scalatest.BeforeAndAfterEach
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import testdata.CommonTestData.validEori
import testdata.MovementsTestData.newUser
import views.html.choice
import views.tags.ViewTest

import scala.jdk.CollectionConverters.IteratorHasAsScala

@ViewTest
class ChoiceViewSpec extends ViewSpec with BeforeAndAfterEach with Injector {

  private val arriveDepartAllowList = mock[ArriveDepartAllowList]

  private val injector = new OverridableInjector(bind[ArriveDepartAllowList].toInstance(arriveDepartAllowList))

  private val choicePage = injector.instanceOf[choice]

  def setArriveDepartAllowList(present: Boolean): Unit = when(arriveDepartAllowList.contains(any())).thenReturn(present)

  override def beforeEach(): Unit = {
    super.beforeEach()
    setArriveDepartAllowList(true)
  }

  override def afterEach(): Unit = {
    reset(arriveDepartAllowList)
    super.afterEach()
  }

  private implicit val request: AuthenticatedRequest[AnyContentAsEmpty.type] = AuthenticatedRequest(FakeRequest().withCSRFToken, newUser(validEori))

  def createView(f: Form[Choice] = form): Document = choicePage(f)

  "Choice View" should {

    val view = createView()

    "display same page title as header" in {
      view.title() must include(view.getElementsByTag("h1").text())
    }

    "display the expected header" in {
      view.getElementsByTag("h1").text() mustBe messages("movement.choice.title")
    }
  }

  "Choice View" should {

    "render the choices in the expected order" in {

      val choices = createView()
        .getElementsByClass("govuk-grid-column-two-thirds")
        .get(0)
        .children()
        .iterator()
        .asScala
        .toList
        .filter(_.tagName() == "a")

      choices(0).attribute("href").getValue must include("findConsignment")
      choices(1).attribute("href").getValue must include("arrival")
      choices(2).attribute("href").getValue must include("departure")
      choices(3).attribute("href").getValue must include("associateUCR")
      choices(4).attribute("href").getValue must include("disassociateUCR")
      choices(5).attribute("href").getValue must include("shutMUCR")
      choices(6).attribute("href").getValue must include("submissions")

    }
  }
}
