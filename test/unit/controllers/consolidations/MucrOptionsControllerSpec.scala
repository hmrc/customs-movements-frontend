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

package controllers.consolidations

import controllers.ControllerLayerSpec
import controllers.consolidations.routes.AssociateUcrController
import controllers.summary.routes.AssociateUcrSummaryController
import forms.DucrPartChiefChoice.IsDucrPart
import forms.MucrOptions.Create
import forms.{DucrPartChiefChoice, MucrOptions}
import models.cache.AssociateUcrAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import repository.MockCache
import testdata.CommonTestData
import testdata.CommonTestData.validMucr
import views.html.consolidations.mucr_options

import scala.concurrent.ExecutionContext.global

class MucrOptionsControllerSpec extends ControllerLayerSpec with MockCache with OptionValues {

  private val page = mock[mucr_options]

  private def controller(journey: ValidJourney = ValidJourney(AssociateUcrAnswers())): MucrOptionsController =
    new MucrOptionsController(SuccessfulAuth(), journey, stubMessagesControllerComponents(), cacheRepository, page, navigator)(global)

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(page.apply(any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override def afterEach(): Unit = {
    reset(page)
    super.afterEach()
  }

  private def theFormRendered: Form[MucrOptions] = {
    val captor: ArgumentCaptor[Form[MucrOptions]] = ArgumentCaptor.forClass(classOf[Form[MucrOptions]])
    verify(page).apply(captor.capture(), any(), any())(any(), any())
    captor.getValue
  }

  private def formWrites(x: MucrOptions): JsValue = {
    val mucrFieldName = if (x.createOrAdd == MucrOptions.Create) "newMucr" else "existingMucr"

    Json.obj("createOrAdd" -> x.createOrAdd, mucrFieldName -> x.mucr)
  }

  "Mucr Options Controller" should {

    "return 200 (OK)" when {

      "display page method is invoked" in {
        val result = controller().displayPage(getRequest())

        status(result) mustBe OK
        theFormRendered.value mustBe empty
      }

      "display page with filled data" in {
        val mucrOptions = MucrOptions(MucrOptions.Create, CommonTestData.correctUcr)

        val validJourney = ValidJourney(AssociateUcrAnswers(None, Some(mucrOptions)))
        val result = controller(validJourney).displayPage(getRequest())

        status(result) mustBe OK
        theFormRendered.value.get.mucr mustBe CommonTestData.correctUcr
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect during saving on first validation" in {
        val incorrectForm = Json.toJson(MucrOptions(MucrOptions.Create, "8GB12345612345612345"))

        val result = controller().save()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verify(page).apply(any(), any(), any())(any(), any())
      }

      "form is incorrect during saving on second validation" in {
        val incorrectForm = Json.toJson(MucrOptions(Create, "incorrect"))

        val result = controller().save()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verify(page).apply(any(), any(), any())(any(), any())
      }

      "a valid MUCR is over 35 characters long" in {
        val incorrectForm = Json.toJson(MucrOptions(Create, "GB/82F9-0N2F6500040010TO120P0A300689"))

        val result = controller().save()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verify(page).apply(any(), any(), any())(any(), any())
      }
    }

    "return 303 (SEE_OTHER)" when {

      "on a NON-'Find a consignment' journey and" when {

        "the Ucr is not of a DucrPart type" in {
          val correctForm = Json.toJson(MucrOptions(Create, validMucr))(formWrites(_))

          val result = controller().save()(postRequest(correctForm))

          status(result) mustBe SEE_OTHER
          thePageNavigatedTo.url mustBe AssociateUcrController.displayPage.url
        }

        "the Ucr is of a DucrPart type" in {
          val correctForm = Json.toJson(MucrOptions(Create, validMucr))(formWrites(_))

          val validJourney = ValidJourney(AssociateUcrAnswers(), None, false, Some(DucrPartChiefChoice(IsDucrPart)))
          val result = controller(validJourney).save()(postRequest(correctForm))

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe AssociateUcrSummaryController.displayPage.url
        }
      }

      "on a 'Find a consignment' journey" in {
        val correctForm = Json.toJson(MucrOptions(Create, validMucr))(formWrites(_))

        val validJourney = ValidJourney(AssociateUcrAnswers(), None, true)
        val result = controller(validJourney).save()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe AssociateUcrSummaryController.displayPage.url
      }
    }
  }
}
