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

package controllers

import base.{MovementBaseSpec, ViewValidator}
import controllers.exception.IncompleteApplication
import controllers.storage.FlashKeys
import forms.Choice.AllowedChoiceValues
import forms.{AssociateDucr, Choice, MucrOptions}
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class AssociateDucrSummaryControllerSpec extends MovementBaseSpec with ViewValidator with BeforeAndAfterEach {

  private val uri = uriWithContextPath("/associate-ducr-summary")

  override def beforeEach() {
    authorizedUser()
    withCaching(Choice.choiceId, Some(Choice(AllowedChoiceValues.AssociateDUCR)))
    withCaching(MucrOptions.formId, Some(MucrOptions("MUCR")))
    withCaching(AssociateDucr.formId, Some(AssociateDucr("DUCR")))
  }

  override def afterEach(): Unit = {
    super.afterEach()
    reset(mockSubmissionService, mockCustomsCacheService)
  }

  "Associate DUCR Summary GET" should {

    "throw incomplete application when MUCROptions cache empty" in {
      withCaching(MucrOptions.formId, None)
      assertThrows[IncompleteApplication] {
        await(route(app, getRequest(uri)).get)
      }
    }

    "throw incomplete application when AssociateDUCR cache empty" in {
      withCaching(AssociateDucr.formId, None)
      assertThrows[IncompleteApplication] {
        await(route(app, getRequest(uri)).get)
      }
    }

    "return Ok for GET request" in {
      val result = route(app, getRequest(uri)).get
      status(result) must be(OK)
    }
  }

  "Associate DUCR Summary POST" should {

    "throw incomplete application when MUCROptions cache empty" in {
      withCaching(MucrOptions.formId, None)
      assertThrows[IncompleteApplication] {
        await(route(app, postRequest(uri, Json.obj())).get)
      }
    }

    "throw incomplete application when AssociateDUCR cache empty" in {
      withCaching(AssociateDucr.formId, None)
      assertThrows[IncompleteApplication] {
        await(route(app, postRequest(uri, Json.obj())).get)
      }
    }

    "Redirect to next page" in {
      given(mockSubmissionService.submitDucrAssociation(any(), any())) willReturn Future.successful((): Unit)
      given(mockCustomsCacheService.remove(anyString())(any(), any())) willReturn Future.successful(mock[HttpResponse])

      val Some(result) = route(app, postRequest(uri, Json.obj()))
      status(result) must be(SEE_OTHER)
      redirectLocation(result) mustBe Some(routes.AssociateDucrConfirmationController.displayPage().url)
      flash(result).get(FlashKeys.MUCR) mustBe Some("MUCR")

      verify(mockSubmissionService).submitDucrAssociation(MucrOptions("MUCR"), AssociateDucr("DUCR"))
      verify(mockCustomsCacheService).remove(anyString)(any(), any())
    }
  }
}
