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

package controllers.consolidations

import controllers.ControllerLayerSpec
import forms.MucrOptions
import forms.MucrOptions.Create
import models.UcrBlock
import models.cache.AssociateUcrAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import repository.MockCache
import testdata.CommonTestData
import testdata.CommonTestData.validMucr
import views.html.associateucr.mucr_options

import scala.concurrent.ExecutionContext.global

class MucrOptionsControllerSpec extends ControllerLayerSpec with MockCache with OptionValues {

  private val page = mock[mucr_options]

  private val queryUcr = UcrBlock(ucr = "ducr", ucrType = "D")

  private def controller(answers: AssociateUcrAnswers, queryUcr: Option[UcrBlock] = None) =
    new MucrOptionsController(SuccessfulAuth(), ValidJourney(answers, queryUcr), stubMessagesControllerComponents(), cache, page)(global)

  override def beforeEach() {
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

  "Mucr Options Controller" should {

    "return 200 (OK)" when {

      "display page method is invoked" in {
        val result = controller(AssociateUcrAnswers()).displayPage()(getRequest())

        status(result) mustBe OK
        theFormRendered.value mustBe empty
      }

      "display page with filled data" in {
        val mucrOptions = MucrOptions(CommonTestData.correctUcr)

        val result = controller(AssociateUcrAnswers(None, Some(mucrOptions), None)).displayPage()(getRequest())

        status(result) mustBe OK
        theFormRendered.value.get.mucr mustBe CommonTestData.correctUcr
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect during saving on first validation" in {
        val incorrectForm = Json.toJson(MucrOptions("8GB12345612345612345", "8GB12345612345612345", ""))

        val result = controller(AssociateUcrAnswers()).save()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verify(page).apply(any(), any(), any())(any(), any())
      }

      "form is incorrect during saving on second validation" in {
        val incorrectForm = Json.toJson(MucrOptions("incorrect", "incorrect", Create))

        val result = controller(AssociateUcrAnswers()).save()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verify(page).apply(any(), any(), any())(any(), any())
      }
    }

    "return 303 (SEE_OTHER)" when {

      "form is correct when queryUcr not present" in {
        val correctForm = Json.toJson(MucrOptions(validMucr, "", Create))

        val result = controller(AssociateUcrAnswers(), queryUcr = None).save()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.AssociateUcrController.displayPage().url
      }

      "form is correct when queryUcr present" in {

        val correctForm = Json.toJson(MucrOptions(validMucr, "", Create))

        val result = controller(AssociateUcrAnswers(), queryUcr = Some(queryUcr)).save()(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.AssociateUcrSummaryController.displayPage().url
      }
    }
  }
}
