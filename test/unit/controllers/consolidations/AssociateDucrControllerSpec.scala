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
import forms.Choice.AssociateDUCR
import forms.{AssociateDucr, Choice, MucrOptions}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.cache.client.CacheMap
import unit.base.ControllerSpec
import views.html.associate_ducr
import controllers.storage.CacheIdGenerator._

import scala.concurrent.ExecutionContext.global

class AssociateDucrControllerSpec extends ControllerSpec {

  private val mockAssociateDucrPage = mock[associate_ducr]

  private val controller = new AssociateDucrController(
    mockAuthAction,
    mockJourneyAction,
    stubMessagesControllerComponents(),
    mockCustomsCacheService,
    mockAssociateDucrPage
  )(global)

  override protected def beforeEach() {
    super.beforeEach()

    authorizedUser()
    withCaching(Choice.choiceId, Some(AssociateDUCR))
    withCaching(AssociateDucr.formId)
    when(mockAssociateDucrPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockAssociateDucrPage)
    super.afterEach()
  }

  val formCaptor: ArgumentCaptor[Form[AssociateDucr]] = ArgumentCaptor.forClass(classOf[Form[AssociateDucr]])

  "Associate Ducr controller" should {

    "return OK (200)" when {

      "display page method is invoked and Mucr Options page has data" in {
        val request = getRequest()
        withCacheMap(Some(CacheMap(movementCacheId()(request), Map(MucrOptions.formId -> Json.toJson(MucrOptions("MUCR"))))))

        val result = controller.displayPage()(request)

        status(result) must be(OK)

        verify(mockAssociateDucrPage).apply(formCaptor.capture(), any())(any(), any())
        formCaptor.getValue.value mustBe empty
      }

      "display previously gathered data" in {
        val request = getRequest()
        withCacheMap(
          Some(
            CacheMap(
              movementCacheId()(request),
              Map(MucrOptions.formId -> Json.toJson(MucrOptions("MUCR")), AssociateDucr.formId -> Json.toJson(AssociateDucr("DUCR")))
            )
          )
        )

        val result = controller.displayPage()(request)

        status(result) must be(OK)
        verify(mockAssociateDucrPage).apply(formCaptor.capture(), any())(any(), any())
        formCaptor.getValue.value.get mustBe AssociateDucr("DUCR")
      }
    }

    "throw an IncompleteApplication exception" when {

      "display page method is invoked and Mucr Options page is in cache" in {
        val request = getRequest()
        withCacheMap(Some(CacheMap(movementCacheId()(request), Map.empty)))

        assertThrows[IncompleteApplication] {
          await(controller.displayPage()(request))
        }
      }

      "Mucr Options page is not in cache during saving with incorrect form" in {
        withCaching(MucrOptions.formId, None)

        assertThrows[IncompleteApplication] {
          await(controller.submit()(postRequest(Json.obj())))
        }
      }
    }

    "return 400 (BAD_REQUEST)" when {

      "form is incorrect and cache contains data from previous page" in {

        withCaching(MucrOptions.formId, Some(MucrOptions("MUCR")))

        val result = controller.submit()(postRequest(Json.obj()))

        status(result) must be(BAD_REQUEST)
      }
    }

    "return 303 (SEE_OTHER)" when {

      "form is correct" in {

        withCaching(MucrOptions.formId, Some(MucrOptions("MUCR")))

        val validMUCR = Json.toJson(AssociateDucr("8GB12345612345612345"))

        val result = controller.submit()(postRequest(validMUCR))

        status(result) must be(SEE_OTHER)
        redirectLocation(result) mustBe Some(routes.AssociateDucrSummaryController.displayPage().url)
      }
    }
  }
}
