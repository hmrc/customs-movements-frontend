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

import controllers.consolidations.{routes, AssociateDucrController}
import controllers.exception.IncompleteApplication
import forms.Choice.AllowedChoiceValues
import forms.{AssociateDucr, Choice, MucrOptions}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import unit.base.ControllerSpec
import views.html.associate_ducr

import scala.concurrent.ExecutionContext.global

class AssociateDucrControllerSpec extends ControllerSpec {

  val mockAssociateDucrPage = mock[associate_ducr]

  val controller = new AssociateDucrController(
    mockAuthAction,
    mockJourneyAction,
    stubMessagesControllerComponents(),
    mockCustomsCacheService,
    mockAssociateDucrPage
  )(global)

  override protected def beforeEach() {
    super.beforeEach()

    authorizedUser()
    withCaching(Choice.choiceId, Some(Choice(AllowedChoiceValues.AssociateDUCR)))
    withCaching(AssociateDucr.formId)
    when(mockAssociateDucrPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockAssociateDucrPage)

    super.afterEach()
  }

  "Associate DUCR GET" should {

    "throw incomplete application when cache empty" in {

      withCaching(MucrOptions.formId, None)

      assertThrows[IncompleteApplication] {
        await(controller.displayPage()(getRequest()))
      }
    }

    "return Ok for GET request" in {

      withCaching(MucrOptions.formId, Some(MucrOptions("MUCR")))

      val result = controller.displayPage()(getRequest())

      status(result) must be(OK)
    }
  }

  "Associate DUCR POST" should {

    "throw incomplete application when cache empty" in {

      withCaching(MucrOptions.formId, None)

      assertThrows[IncompleteApplication] {
        await(controller.submit()(postRequest(Json.obj())))
      }
    }

    "display an error for a missing DUCR" in {

      withCaching(MucrOptions.formId, Some(MucrOptions("MUCR")))

      val result = controller.submit()(postRequest(Json.obj()))

      status(result) must be(BAD_REQUEST)
    }

    "display an error for a empty DUCR" in {

      withCaching(MucrOptions.formId, Some(MucrOptions("MUCR")))

      val result = controller.submit()(postRequest(Json.obj("ducr" -> "")))

      status(result) must be(BAD_REQUEST)
    }

    "display an error for an invalid DUCR" in {

      withCaching(MucrOptions.formId, Some(MucrOptions("MUCR")))

      val result = controller.submit()(postRequest(Json.obj("ducr" -> "invalid")))

      status(result) must be(BAD_REQUEST)
    }

    "Redirect to next page for a valid form" in {

      withCaching(MucrOptions.formId, Some(MucrOptions("MUCR")))

      val validMUCR =
        JsObject(Map("ducr" -> JsString("8GB12345612345612345")))

      val result = controller.submit()(postRequest(validMUCR))

      status(result) must be(SEE_OTHER)
      redirectLocation(result) mustBe Some(routes.AssociateDucrSummaryController.displayPage().url)
    }
  }
}
