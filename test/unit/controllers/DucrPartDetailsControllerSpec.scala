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

package controllers

import config.IleQueryConfig
import controllers.actions.NonIleQueryAction
import controllers.exception.InvalidFeatureStateException
import forms.{DucrPartDetails, UcrType}
import models.UcrBlock
import models.cache.{Answers, ArrivalAnswers, Cache}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito._
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
  private val ileQueryConfig = mock[IleQueryConfig]

  private def controller(answers: Answers = ArrivalAnswers(), nonIleQueryAction: NonIleQueryAction = ValidForIleQuery) =
    new DucrPartDetailsController(
      stubMessagesControllerComponents(),
      SuccessfulAuth(),
      ValidJourney(answers),
      nonIleQueryAction,
      ileQueryConfig,
      cache,
      ducrPartDetailsPage
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

      val result = controller().displayPage()(getRequest())
      status(result) mustBe OK

      verify(cache).findByEori(any())
    }

    "pass empty form to DucrPartDetails view" when {
      "cache is empty" in {
        givenTheCacheIsEmpty()

        val result = controller().displayPage()(getRequest())
        status(result) mustBe OK

        val expectedForm = DucrPartDetails.form()
        verify(ducrPartDetailsPage).apply(meq(expectedForm))(any(), any())
      }
    }

    "pass data from CacheRepository to DucrPartDetails view" when {
      "cache contains queryUcr of DucrParts type" in {
        val cacheContents =
          Cache(eori = "eori", answers = None, queryUcr = Some(UcrBlock(ucrType = UcrType.DucrPart.codeValue, ucr = validWholeDucrParts)), None)
        givenTheCacheContains(cacheContents)

        val result = controller().displayPage()(getRequest())
        status(result) mustBe OK

        val expectedForm = DucrPartDetails.form().fill(DucrPartDetails(ducr = validDucr, ducrPartId = validDucrPartId))
        verify(ducrPartDetailsPage).apply(meq(expectedForm))(any(), any())
      }
    }

    "pass empty form to DucrPartDetails view" when {
      "cache contains queryUcr of different type" in {
        val cacheContents =
          Cache(eori = "eori", answers = None, queryUcr = Some(UcrBlock(ucrType = UcrType.Ducr.codeValue, ucr = validDucr)), None)
        givenTheCacheContains(cacheContents)

        val result = controller().displayPage()(getRequest())
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

        verifyNoMoreInteractions(cache)
      }
    }

    "provide CacheRepository with correct UcrBlock object" when {
      "provided with correct data" in {
        val inputData = Json.obj("ducr" -> validDucr, "ducrPartId" -> validDucrPartId)

        val result = controller().submitDucrPartDetails()(postRequest(inputData))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.ChoiceController.displayChoiceForm().url)

        val expectedUcrBlock = UcrBlock(ucrType = UcrType.DucrPart, ucr = validWholeDucrParts.toUpperCase)
        theCacheUpserted.queryUcr mustBe defined
        theCacheUpserted.queryUcr.get mustBe expectedUcrBlock
      }
    }
  }

  "DucrPartDetailsController on submitDucrPartDetailsJourney" when {

    "ileQuery feature is enabled" should {
      "throw InvalidFeatureStateException" in {
        intercept[InvalidFeatureStateException](
          await(controller(nonIleQueryAction = NotValidForIleQuery).submitDucrPartDetailsJourney()(postRequest()))
        )
      }
    }

    "ileQuery feature is disabled" should {

      "return BadRequest (400) response" when {
        "provided with incorrect data" in {
          val inputData = Json.obj("ducr" -> "InvalidDucr!@#", "ducrPartId" -> "InvalidDucrPartId!@#")

          val result = controller().submitDucrPartDetailsJourney()(postRequest(inputData))

          status(result) mustBe BAD_REQUEST
          verifyNoMoreInteractions(cache)
        }
      }

      "provide CacheRepository with correct UcrBlock object" when {
        "provided with correct data" in {
          val inputData = Json.obj("ducr" -> validDucr, "ducrPartId" -> validDucrPartId)

          val result = controller().submitDucrPartDetails()(postRequest(inputData))
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.ChoiceController.displayChoiceForm().url)

          val expectedUcrBlock = UcrBlock(ucrType = UcrType.DucrPart, ucr = validWholeDucrParts.toUpperCase)
          theCacheUpserted.queryUcr mustBe defined
          theCacheUpserted.queryUcr.get mustBe expectedUcrBlock
        }
      }
    }
  }
}
