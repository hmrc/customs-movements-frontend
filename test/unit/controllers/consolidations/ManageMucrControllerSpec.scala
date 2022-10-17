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

package controllers.consolidations

import controllers.ControllerLayerSpec
import controllers.actions.IleQueryAction
import controllers.exception.InvalidFeatureStateException
import forms.{ManageMucrChoice, UcrType}
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
import views.html.associateucr.manage_mucr

import scala.concurrent.ExecutionContext.Implicits.global

class ManageMucrControllerSpec extends ControllerLayerSpec with MockCache with OptionValues {

  private val page = mock[manage_mucr]

  private def controller(
    answers: AssociateUcrAnswers,
    ileQueryAction: IleQueryAction = IleQueryEnabled,
    ucrBlock: Option[UcrBlock] = Some(UcrBlock("mucr", UcrType.Mucr))
  ) =
    new ManageMucrController(
      SuccessfulAuth(),
      ileQueryAction,
      ValidJourney(answers, ucrBlock, true),
      stubMessagesControllerComponents(),
      cacheRepository,
      page,
      navigator
    )

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(page.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override def afterEach(): Unit = {
    reset(page)
    super.afterEach()
  }

  private def theFormRendered: Form[ManageMucrChoice] = {
    val captor: ArgumentCaptor[Form[ManageMucrChoice]] = ArgumentCaptor.forClass(classOf[Form[ManageMucrChoice]])
    verify(page).apply(captor.capture(), any())(any(), any())
    captor.getValue
  }

  "Manage Mucr Controller" should {

    "return 200 (OK)" when {

      "display page method is invoked" in {
        val result = controller(AssociateUcrAnswers()).displayPage(getRequest())

        status(result) mustBe OK
        theFormRendered.value mustBe empty
      }

      "display page with filled data" in {
        val manageMucrChoice = ManageMucrChoice(ManageMucrChoice.AssociateAnotherMucr)

        val result = controller(AssociateUcrAnswers(Some(manageMucrChoice), None, None)).displayPage(getRequest())

        status(result) mustBe OK
        theFormRendered.value.get.choice mustBe ManageMucrChoice.AssociateAnotherMucr
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect during saving" in {
        val incorrectForm = Json.toJson(ManageMucrChoice("invalid"))

        val result = controller(AssociateUcrAnswers()).submit(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
        verify(page).apply(any(), any())(any(), any())
      }
    }

    "return 303 (SEE_OTHER)" when {

      "queried ucr was Ducr" in {
        val result = controller(AssociateUcrAnswers(), ucrBlock = Some(UcrBlock("ducr", UcrType.Ducr))).displayPage(getRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.MucrOptionsController.displayPage.url
      }

      "queried ucr was Ducr Part" in {
        val result = controller(AssociateUcrAnswers(), ucrBlock = Some(UcrBlock("ducrPart", UcrType.DucrPart))).displayPage(getRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.MucrOptionsController.displayPage.url
      }

      "form is correct with AssociateAnotherMucr option" in {
        val correctForm = Json.toJson(ManageMucrChoice(ManageMucrChoice.AssociateAnotherMucr))

        val result = controller(AssociateUcrAnswers()).submit(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        thePageNavigatedTo.url mustBe routes.AssociateUcrController.displayPage.url
      }

      "form is correct with AssociateThisMucr option" in {
        val correctForm = Json.toJson(ManageMucrChoice(ManageMucrChoice.AssociateThisMucr))

        val result = controller(AssociateUcrAnswers()).submit(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
        thePageNavigatedTo.url mustBe routes.MucrOptionsController.displayPage.url
      }
    }
  }

  "Manage Mucr Controller when accessed in illegal state" should {

    "block access" when {

      "ileQuery feature disabled" in {
        intercept[RuntimeException] {
          await(controller(AssociateUcrAnswers(), IleQueryDisabled).displayPage(getRequest()))
        } mustBe InvalidFeatureStateException
      }

      "queried ucr not available in cache" in {
        intercept[RuntimeException] {
          await(controller(AssociateUcrAnswers(), IleQueryEnabled, ucrBlock = None).displayPage(getRequest()))
        } mustBe InvalidFeatureStateException
      }
    }
  }
}
