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

package controllers.consolidations

import base.MockSubmissionService
import controllers.exception.IncompleteApplication
import forms.Choice.ShutMUCR
import forms.{Choice, ShutMucr}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import play.api.libs.json.JsObject
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import testdata.ConsolidationTestData.ValidMucr
import unit.base.LegacyControllerSpec
import views.html.shut_mucr_summary

import scala.concurrent.ExecutionContext.global

class ShutMucrSummaryControllerSpec extends LegacyControllerSpec with MockSubmissionService {

  val shutMucrSummaryPage = mock[shut_mucr_summary]

  val controller = new ShutMucrSummaryController(
    mockAuthAction,
    mockJourneyAction,
    stubMessagesControllerComponents(),
    mockCustomsCacheService,
    mockSubmissionService,
    shutMucrSummaryPage
  )(global)

  override def beforeEach(): Unit = {
    super.beforeEach()

    authorizedUser()
    withCaching(Choice.choiceId, Some(ShutMUCR))
    when(shutMucrSummaryPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  "Shut Mucr Summary Controller" should {

    "return 200 (OK)" when {

      "cache contains information from shut mucr page" in {

        withCaching(ShutMucr.formId, Some(ShutMucr(ValidMucr)))

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
        verify(shutMucrSummaryPage).apply(any())(any(), any())
      }
    }

    "throw an exception" when {

      "cache is empty for displayPage method" in {

        withCaching(ShutMucr.formId, None)

        intercept[IncompleteApplication] {
          await(controller.displayPage()(getRequest()))
        }
      }

      "cache is empty for submit method" in {

        withCaching(ShutMucr.formId, None)

        intercept[IncompleteApplication] {
          await(controller.submit()(postRequest(JsObject(Seq.empty))))
        }
      }
    }

    "return 303 (SEE_OTHER)" when {

      "cache contains shut mucr data and submission is successfully" in {

        withCaching(ShutMucr.formId, Some(ShutMucr(ValidMucr)))
        mockShutMucr()
        mockCustomsCacheServiceClearedSuccessfully

        val result = controller.submit()(postRequest(JsObject(Seq.empty)))

        status(result) mustBe SEE_OTHER
      }
    }
  }
}
