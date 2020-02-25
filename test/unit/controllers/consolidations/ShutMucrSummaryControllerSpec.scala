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

import forms.ShutMucr
import models.ReturnToStartException
import models.cache.ShutMucrAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito._
import org.mockito.Mockito.{verify, when}
import play.api.libs.json.JsObject
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import services.SubmissionService
import testdata.ConsolidationTestData.validMucr
import unit.controllers.ControllerLayerSpec
import unit.repository.MockCache
import views.html.shut_mucr_summary

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class ShutMucrSummaryControllerSpec extends ControllerLayerSpec with MockCache {

  private val submissionService = mock[SubmissionService]
  private val page = mock[shut_mucr_summary]

  private def controller(answers: ShutMucrAnswers) =
    new ShutMucrSummaryController(SuccessfulAuth(), ValidJourney(answers), stubMessagesControllerComponents(), submissionService, page)(global)

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(page.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  "Shut Mucr Summary Controller" should {
    val mucr = ShutMucr(validMucr)

    "return 200 (OK)" when {

      "cache contains information from shut mucr page" in {
        val result = controller(ShutMucrAnswers(Some(mucr))).displayPage()(getRequest())

        status(result) mustBe OK
        verify(page).apply(any())(any(), any())
      }
    }

    "throw an exception" when {

      "cache is empty for displayPage method" in {
        intercept[RuntimeException] {
          await(controller(ShutMucrAnswers()).displayPage()(getRequest()))
        } mustBe ReturnToStartException
      }
    }

    "return 303 (SEE_OTHER)" when {

      "cache contains shut mucr data and submission is successfully" in {
        given(submissionService.submit(any(), any[ShutMucrAnswers])(any())).willReturn(Future.successful((): Unit))

        val result = controller(ShutMucrAnswers(Some(mucr))).submit()(postRequest(JsObject(Seq.empty)))

        status(result) mustBe SEE_OTHER
      }
    }
  }
}
