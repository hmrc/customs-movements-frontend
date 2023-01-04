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
import controllers.summary.routes.AssociateUcrSummaryController
import forms.UcrType._
import forms.{AssociateUcr, MucrOptions}
import models.ReturnToStartException
import models.cache.AssociateUcrAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import repository.MockCache
import testdata.CommonTestData.validDucr
import views.html.consolidations.associate_ucr

import scala.concurrent.ExecutionContext.Implicits.global

class AssociateUcrControllerSpec extends ControllerLayerSpec with MockCache {

  private val mockAssociateDucrPage = mock[associate_ucr]

  private def controller(answers: AssociateUcrAnswers) =
    new AssociateUcrController(SuccessfulAuth(), ValidJourney(answers), stubMessagesControllerComponents(), cacheRepository, mockAssociateDucrPage)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(mockAssociateDucrPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockAssociateDucrPage)
    super.afterEach()
  }

  def theFormRendered: Form[AssociateUcr] = {
    val captor: ArgumentCaptor[Form[AssociateUcr]] = ArgumentCaptor.forClass(classOf[Form[AssociateUcr]])
    verify(mockAssociateDucrPage).apply(captor.capture(), any())(any(), any())
    captor.getValue
  }

  "Associate Ducr controller" should {
    val mucrOptions = MucrOptions(MucrOptions.Create, "MUCR")
    val associateUcr = AssociateUcr(Ducr, "DUCR")

    "return OK (200)" when {

      "display page method is invoked and Mucr Options page has data" in {
        val request = getRequest()

        val result = controller(AssociateUcrAnswers(None, Some(mucrOptions))).displayPage(request)

        status(result) must be(OK)
        theFormRendered.value mustBe empty
      }

      "display previously gathered data" in {
        val request = getRequest()

        val result = controller(AssociateUcrAnswers(None, Some(mucrOptions), Some(associateUcr))).displayPage(request)

        status(result) must be(OK)
        theFormRendered.value.get mustBe AssociateUcr(Ducr, "DUCR")
      }
    }

    "throw an IncompleteApplication exception" when {

      "display page method is invoked and Mucr Options page is not in cache" in {
        val request = getRequest()

        intercept[RuntimeException] {
          await(controller(AssociateUcrAnswers(mucrOptions = None)).displayPage(request))
        } mustBe ReturnToStartException
      }

      "Mucr Options page is not in cache during saving with incorrect form" in {
        intercept[RuntimeException] {
          await(controller(AssociateUcrAnswers(mucrOptions = None)).submit(postRequest(Json.obj())))
        } mustBe ReturnToStartException
      }
    }

    "return 400 (BAD_REQUEST)" when {
      "form is incorrect and cache contains data from previous page" in {
        val result = controller(AssociateUcrAnswers(None, Some(mucrOptions))).submit(postRequest(Json.obj()))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER)" when {
      "form is correct" in {
        val validDUCR = AssociateUcr.mapping.unbind(AssociateUcr(Ducr, validDucr))

        val result = controller(AssociateUcrAnswers(None, Some(mucrOptions))).submit(postRequest(validDUCR))
        status(result) must be(SEE_OTHER)
        redirectLocation(result) mustBe Some(AssociateUcrSummaryController.displayPage.url)
      }
    }
  }
}
