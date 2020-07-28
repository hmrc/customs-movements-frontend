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
import config.IleQueryConfig
import forms.DucrPartDetails
import org.jsoup.nodes.Document
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.{Form, FormError}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.twirl.api.Html
import testdata.CommonTestData._
import views.html.ducr_part_details
import views.spec.ViewMatchers
import views.tags.ViewTest

@ViewTest
class DucrPartDetailsViewSpec extends ViewSpec with ViewMatchers with MockitoSugar with BeforeAndAfterEach {

  private val appConfig = mock[IleQueryConfig]
  private val injector = new OverridableInjector(bind[IleQueryConfig].toInstance(appConfig))

  private implicit val request = FakeRequest().withCSRFToken
  private val page = injector.instanceOf[ducr_part_details]

  private def createView(form: Form[DucrPartDetails]): Html = page(form)

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(appConfig.isIleQueryEnabled).thenReturn(true)
  }

  override def afterEach(): Unit = {
    reset(appConfig)
    super.afterEach()
  }

  "DucrPartDetails view" when {

    "provided with empty form" should {

      val view = createView(DucrPartDetails.form())

      "render title" in {

        view.getTitle must containMessage("ducrPartDetails.title")
      }

      "render heading" in {

        view.getElementById("title") must containMessage("ducrPartDetails.title")
      }

      "render page hint" in {

        view.getElementById("page-hint") must containMessage("ducrPartDetails.heading")
      }

      "render 'Back' button leading to 'Find a consignment' page when ileQuery enabled" in {
        when(appConfig.isIleQueryEnabled).thenReturn(true)
        val view = createView(DucrPartDetails.form())

        view.getBackButton mustBe defined
        view.getBackButton.get must haveHref(controllers.ileQuery.routes.FindConsignmentController.displayQueryForm())
      }

      "render 'Back' button leading to 'Ducr Part Chief' page when ileQuery disabled" in {
        when(appConfig.isIleQueryEnabled).thenReturn(false)
        val view = createView(DucrPartDetails.form())

        view.getBackButton mustBe defined
        view.getBackButton.get must haveHref(controllers.routes.DucrPartChiefController.displayPage())
      }

      "render DUCR input field label" in {

        view.getElementsByAttributeValue("for", "ducr").first() must containMessage("ducrPartDetails.ducr")
      }

      "render DUCR input field hint" in {

        view.getElementById("ducr-hint") must containMessage("ducrPartDetails.ducr.hint")
      }

      "render empty DUCR input field" in {

        view.getElementById("ducr").`val`() mustBe empty
      }

      "render DUCR Part ID input field label" in {

        view.getElementsByAttributeValue("for", "ducrPartId").first() must containMessage("ducrPartDetails.ducrPartId")
      }

      "render DUCR Part ID input field hint" in {

        view.getElementById("ducrPartId-hint") must containMessage("ducrPartDetails.ducrPartId.hint")
      }

      "render empty DUCR Part ID input field" in {

        view.getElementById("ducrPartId").`val`() mustBe empty
      }

      "render submit button" in {

        view.getSubmitButton mustBe defined
        view.getSubmitButton.get must containMessage("site.continue")
      }
    }

    "provided with filled form" should {

      val form = DucrPartDetails.form().fill(DucrPartDetails(ducr = validDucr, ducrPartId = validDucrPartId))
      val view = createView(form)

      "fill DUCR input field" in {

        view.getElementById("ducr").`val`() mustBe validDucr
      }

      "fill DUCR Part ID input field" in {

        view.getElementById("ducrPartId").`val`() mustBe validDucrPartId
      }
    }

    "provided with form containing DUCR error" should {

      val form = DucrPartDetails.form().withError(FormError("ducr", "ducrPartDetails.ducr.error"))
      val view: Document = createView(form)

      "render error summary" in {

        view must haveGovUkGlobalErrorSummary
      }

      "render field error" in {

        view must haveGovUkFieldError("ducr", messages("ducrPartDetails.ducr.error"))
      }
    }

    "provided with form containing DUCR Part ID error" should {

      val form = DucrPartDetails.form().withError(FormError("ducrPartId", "ducrPartDetails.ducrPartId.error"))
      val view: Document = createView(form)

      "render error summary" in {

        view must haveGovUkGlobalErrorSummary
      }

      "render field error" in {

        view must haveGovUkFieldError("ducrPartId", messages("ducrPartDetails.ducrPartId.error"))
      }
    }
  }

}
