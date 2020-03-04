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

import config.AppConfig
import controllers.ControllerLayerSpec
import controllers.actions.NonIleQueryAction
import controllers.exception.FeatureDisabledException
import forms._
import models.cache.DisassociateUcrAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import repository.MockCache
import testdata.ConsolidationTestData.validDucr
import views.html.disassociateucr.disassociate_ucr

import scala.concurrent.ExecutionContext.Implicits.global

class DisassociateUcrControllerSpec extends ControllerLayerSpec with MockCache with ScalaFutures with OptionValues {

  private val page = mock[disassociate_ucr]
  private val config = mock[AppConfig]

  private def controller(answers: DisassociateUcrAnswers) =
    new DisassociateUcrController(
      SuccessfulAuth(),
      ValidJourney(answers),
      new NonIleQueryAction(config),
      stubMessagesControllerComponents(),
      cache,
      page
    )

  private val correctForm = Json.toJson(DisassociateUcr(DisassociateKind.Ducr, Some(validDucr), Some("")))
  private val incorrectForm = Json.toJson(DisassociateUcr(DisassociateKind.Ducr, Some("abc"), None))

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(page.apply(any())(any(), any())) thenReturn HtmlFormat.empty
    when(config.ileQueryEnabled) thenReturn false
  }

  override protected def afterEach(): Unit = {
    reset(page)
    reset(config)
    super.afterEach()
  }

  private def theFormDisplayed: Form[DisassociateUcr] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[DisassociateUcr]])
    verify(page).apply(captor.capture())(any(), any())
    captor.getValue
  }

  "Disassociate Ucr Controller" should {
    "return 200 (OK)" when {
      "display page is invoked" in {
        val result = controller(DisassociateUcrAnswers()).displayPage()(getRequest())

        status(result) mustBe OK
        theFormDisplayed.value mustBe empty
      }

      "display page is invoked with data" in {
        val ucr = DisassociateUcr(DisassociateKind.Ducr, Some("ducr"), None)
        val result = controller(DisassociateUcrAnswers(Some(ucr))).displayPage()(getRequest())

        status(result) mustBe OK
        theFormDisplayed.value mustBe Some(ucr)
      }
    }

    "return 400 (BAD_REQUEST)" when {
      "incorrect form is submitted" in {
        val result = controller(DisassociateUcrAnswers()).submit()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 303 (SEE_OTHER)" when {
      "form is correct" in {
        val result = controller(DisassociateUcrAnswers()).submit()(postRequest(correctForm))
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.DisassociateUcrSummaryController.displayPage().url
      }
    }

    "block access" when {
      "ileQuery enabled" in {
        when(config.ileQueryEnabled) thenReturn true

        intercept[RuntimeException] {
          await(controller(DisassociateUcrAnswers()).displayPage()(getRequest()))
        } mustBe FeatureDisabledException
      }
    }
  }

}
