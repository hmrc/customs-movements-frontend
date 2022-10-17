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

package controllers.ileQuery

import controllers.ControllerLayerSpec
import controllers.actions.IleQueryAction
import controllers.exception.InvalidFeatureStateException
import controllers.ileQuery.routes.IleQueryController
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.libs.json.{JsString, Json}
import play.api.test.Helpers.{status, _}
import play.twirl.api.HtmlFormat
import repository.{MockCache, MockIleQueryCache}
import testdata.CommonTestData.correctUcr
import views.html.ile_query

class FindConsignmentControllerSpec extends ControllerLayerSpec with MockIleQueryCache with MockCache {

  private val ileQueryPage = mock[ile_query]

  private def controllerWithIleQuery(ileQueryAction: IleQueryAction): FindConsignmentController =
    new FindConsignmentController(SuccessfulAuth(), ileQueryAction, stubMessagesControllerComponents(), ileQueryPage)

  private val controller = controllerWithIleQuery(IleQueryEnabled)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(ileQueryPage)

    when(ileQueryPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(ileQueryPage)

    super.afterEach()
  }

  "FindConsignmentController on displayPage" should {
    "return Ok status (200)" in {
      val result = controller.displayPage(getRequest())

      status(result) mustBe OK
    }
  }

  "FindConsignmentController on submitPage" when {

    "provide with correct form" should {

      "return SeeOther status (303)" in {
        val correctForm = Json.obj(("ucr", JsString(correctUcr)))

        val result = controller.submitPage(postRequest(correctForm))

        status(result) mustBe SEE_OTHER
      }

      "redirect to Consignment Details page" in {
        val correctForm = Json.obj(("ucr", JsString(correctUcr)))

        val result = controller.submitPage(postRequest(correctForm))

        redirectLocation(result).get mustBe IleQueryController.getConsignmentData(correctUcr).url
      }
    }

    "provided with incorrect form" should {
      "return BadRequest status (400)" in {
        val incorrectForm = JsString("1234")

        val result = controller.submitPage(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }
    }
  }

  "FindConsignmentController when ileQuery disabled" should {

    val controllerIleQueryDisabled = controllerWithIleQuery(IleQueryDisabled)

    "block access to query form" in {
      intercept[RuntimeException] {
        await(controllerIleQueryDisabled.displayPage(getRequest()))
      } mustBe InvalidFeatureStateException

    }

    "block access when posting query form" in {
      val correctForm = Json.obj(("ucr", JsString(correctUcr)))

      intercept[RuntimeException] {
        await(controllerIleQueryDisabled.submitPage(postRequest(correctForm)))
      }
    }
  }
}
