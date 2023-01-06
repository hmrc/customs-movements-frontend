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

package controllers

import controllers.routes.SpecificDateTimeController
import forms.{DucrPartDetails, UcrType}
import models.UcrBlock
import models.cache.{Answers, ArrivalAnswers, Cache}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.MockitoSugar.{mock, reset, verify, verifyNoMoreInteractions, when}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import repository.MockCache
import testdata.CommonTestData.{validDucr, validDucrPartId, validWholeDucrParts}
import views.html.ducr_part_details

import scala.concurrent.ExecutionContext.global

class DucrPartDetailsControllerSpec extends ControllerLayerSpec with MockCache with ScalaFutures with IntegrationPatience {

  private val ducrPartDetailsPage = mock[ducr_part_details]

  private def controller(answers: Answers = ArrivalAnswers()) =
    new DucrPartDetailsController(
      stubMessagesControllerComponents(),
      SuccessfulAuth(),
      ValidJourney(answers),
      cacheRepository,
      ducrPartDetailsPage,
      navigator
    )(global)

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(ducrPartDetailsPage)
    when(ducrPartDetailsPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override def afterEach(): Unit = {
    reset(ducrPartDetailsPage)

    super.afterEach()
  }

  "DucrPartDetailsController on displayPage" should {

    "return Ok (200) response" in {
      givenTheCacheIsEmpty()

      val result = controller().displayPage(getRequest())
      status(result) mustBe OK

      verify(cacheRepository).findByEori(any())
    }

    "pass empty form to DucrPartDetails view" when {
      "cache is empty" in {
        givenTheCacheIsEmpty()

        val result = controller().displayPage(getRequest())
        status(result) mustBe OK

        val expectedForm = DucrPartDetails.form()
        verify(ducrPartDetailsPage).apply(meq(expectedForm))(any(), any())
      }
    }

    "pass data from CacheRepository to DucrPartDetails view" when {
      "cache contains ucrBlock of DucrParts type" in {
        val cacheContents =
          Cache(eori = "eori", UcrBlock(ucrType = UcrType.DucrPart.codeValue, ucr = validWholeDucrParts), false)
        givenTheCacheContains(cacheContents)

        val result = controller().displayPage(getRequest())
        status(result) mustBe OK

        val expectedForm = DucrPartDetails.form().fill(DucrPartDetails(ducr = validDucr, ducrPartId = validDucrPartId))
        verify(ducrPartDetailsPage).apply(meq(expectedForm))(any(), any())
      }
    }

    "pass empty form to DucrPartDetails view" when {
      "cache contains ucrBlock of different type" in {
        val cacheContents = Cache(eori = "eori", UcrBlock(ucrType = UcrType.Ducr.codeValue, ucr = validDucr), false)
        givenTheCacheContains(cacheContents)

        val result = controller().displayPage(getRequest())
        status(result) mustBe OK

        val expectedForm = DucrPartDetails.form()
        verify(ducrPartDetailsPage).apply(meq(expectedForm))(any(), any())
      }
    }
  }

  "DucrPartDetailsController on submitDucrPartDetails" should {

    "return BadRequest (400) response" when {
      "provided with incorrect data" in {
        val inputData = Json.obj("ducr" -> "InvalidDucr!@#", "ducrPartId" -> "InvalidDucrPartId!@#")

        val result = controller().submitDucrPartDetails()(postRequest(inputData))
        status(result) mustBe BAD_REQUEST

        verifyNoMoreInteractions(cacheRepository)
      }
    }

    "provide CacheRepository with correct UcrBlock object" when {
      "provided with correct data" in {
        val inputData = Json.obj("ducr" -> validDucr, "ducrPartId" -> validDucrPartId)

        val result = controller().submitDucrPartDetails()(postRequest(inputData))
        status(result) mustBe SEE_OTHER
        thePageNavigatedTo.url mustBe SpecificDateTimeController.displayPage.url

        val expectedUcrBlock = UcrBlock(ucrType = UcrType.DucrPart, ucr = validWholeDucrParts.toUpperCase)
        theCacheUpserted.ucrBlock mustBe defined
        theCacheUpserted.ucrBlock.get mustBe expectedUcrBlock
      }
    }
  }
}
