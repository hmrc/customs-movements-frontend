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

package unit.controllers.consolidations

import base.MockSubmissionService
import controllers.consolidations.{routes, DisassociateUcrController}
import forms.Choice.DisassociateDUCR
import forms._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import testdata.ConsolidationTestData.ValidDucr
import unit.base.ControllerSpec
import views.html.disassociate_ucr

import scala.concurrent.ExecutionContext.global

class DisassociateUcrControllerSpec extends ControllerSpec with MockSubmissionService with ScalaFutures with OptionValues {

  private val mockDisassociateUcrPage = mock[disassociate_ucr]

  private val controller = new DisassociateUcrController(
    mockAuthAction,
    mockJourneyAction,
    stubMessagesControllerComponents(),
    mockCustomsCacheService,
    mockDisassociateUcrPage
  )(global)

  private val correctForm = Json.toJson(DisassociateUcr(DisassociateKind.Ducr, Some(ValidDucr), Some("")))
  private val incorrectForm = Json.toJson(DisassociateUcr(DisassociateKind.Ducr, Some("abc"), None))

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    withCaching(Choice.choiceId, Some(DisassociateDUCR))
    withCaching(DisassociateUcr.formId, None)
    withCaching(DisassociateUcr.formId)
    when(mockDisassociateUcrPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockDisassociateUcrPage)
    super.afterEach()
  }

  "Disassociate Ucr Controller" should {

    "return 200 (OK)" when {

      "display page is invoked" in {

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "incorrect form is submitted" in {

        val result = controller.submit()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 303 (SEE_OTHER)" when {

      "form is correct" in {

        val result = controller.submit()(postRequest(correctForm))
        await(result)
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.DisassociateUcrSummaryController.displayPage().url
      }
    }
  }
}
