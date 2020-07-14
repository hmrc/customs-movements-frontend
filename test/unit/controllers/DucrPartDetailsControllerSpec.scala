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

package controllers

import controllers.actions.DucrPartsAction
import controllers.exception.InvalidFeatureStateException
import forms.{DucrPartDetails, UcrType}
import models.UcrBlock
import play.api.test.Helpers._
import models.cache.Cache
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.libs.json.Json
import play.twirl.api.HtmlFormat
import repository.MockCache
import testdata.CommonTestData.{validDucr, validDucrPartId, validWholeDucrParts}
import views.html.ducr_part_details

import scala.concurrent.ExecutionContext.global

class DucrPartDetailsControllerSpec extends ControllerLayerSpec with MockCache with ScalaFutures with IntegrationPatience {

  private val ducrPartDetailsPage = mock[ducr_part_details]

  private def controller(ducrPartsAction: DucrPartsAction = DucrPartsEnabled) =
    new DucrPartDetailsController(stubMessagesControllerComponents(), SuccessfulAuth(), ducrPartsAction, cache, ducrPartDetailsPage)(global)

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(ducrPartDetailsPage)
    when(ducrPartDetailsPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override def afterEach(): Unit = {
    reset(ducrPartDetailsPage)

    super.afterEach()
  }

  "DucrPartDetailsController on displayPage" when {

    "ducrParts feature is disabled" should {

      "throw InvalidFeatureStateException" in {

        intercept[InvalidFeatureStateException](controller(DucrPartsDisabled).displayPage()(getRequest()).futureValue)
      }
    }

    "ducrParts feature is enabled" should {

      "call CacheRepository" in {

        givenTheCacheIsEmpty()

        controller().displayPage()(getRequest()).futureValue

        verify(cache).findByEori(any())
      }

      "return Ok (200) response" in {

        givenTheCacheIsEmpty()

        val result = controller().displayPage()(getRequest())

        status(result) mustBe OK
      }
    }

    "ducrParts feature is enabled" when {

      "cache is empty" should {

        "call DucrPartDetails view" in {

          givenTheCacheIsEmpty()

          controller().displayPage()(getRequest()).futureValue

          verify(ducrPartDetailsPage).apply(any())(any(), any())
        }

        "pass empty form to DucrPartDetails view" in {

          givenTheCacheIsEmpty()

          controller().displayPage()(getRequest()).futureValue

          val expectedForm = DucrPartDetails.form()
          verify(ducrPartDetailsPage).apply(meq(expectedForm))(any(), any())
        }
      }

      "cache contains queryUcr of DucrParts type" should {

        "call DucrPartDetails view" in {

          val cacheContents =
            Cache(eori = "eori", answers = None, queryUcr = Some(UcrBlock(ucrType = UcrType.DucrParts.codeValue, ucr = validWholeDucrParts)))
          givenTheCacheContains(cacheContents)

          controller().displayPage()(getRequest()).futureValue

          verify(ducrPartDetailsPage).apply(any())(any(), any())
        }

        "pass data from CacheRepository to DucrPartDetails view" in {

          val cacheContents =
            Cache(eori = "eori", answers = None, queryUcr = Some(UcrBlock(ucrType = UcrType.DucrParts.codeValue, ucr = validWholeDucrParts)))
          givenTheCacheContains(cacheContents)

          controller().displayPage()(getRequest()).futureValue

          val expectedForm = DucrPartDetails.form().fill(DucrPartDetails(ducr = validDucr, ducrPartId = validDucrPartId))
          verify(ducrPartDetailsPage).apply(meq(expectedForm))(any(), any())
        }
      }

      "cache contains queryUcr of different type" should {

        "call DucrPartDetails view" in {

          val cacheContents =
            Cache(eori = "eori", answers = None, queryUcr = Some(UcrBlock(ucrType = UcrType.Ducr.codeValue, ucr = validDucr)))
          givenTheCacheContains(cacheContents)

          controller().displayPage()(getRequest()).futureValue

          verify(ducrPartDetailsPage).apply(any())(any(), any())
        }

        "pass empty form to DucrPartDetails view" in {

          val cacheContents =
            Cache(eori = "eori", answers = None, queryUcr = Some(UcrBlock(ucrType = UcrType.Ducr.codeValue, ucr = validDucr)))
          givenTheCacheContains(cacheContents)

          controller().displayPage()(getRequest()).futureValue

          val expectedForm = DucrPartDetails.form()
          verify(ducrPartDetailsPage).apply(meq(expectedForm))(any(), any())
        }
      }
    }
  }

  "DucrPartDetailsController on submitDucrPartDetails" when {

    "ducrParts feature is disabled" should {

      "throw InvalidFeatureStateException" in {

        intercept[InvalidFeatureStateException](controller(DucrPartsDisabled).submitDucrPartDetails()(postRequest()).futureValue)
      }
    }

    "ducrParts feature is enabled" when {

      "provided with incorrect data" should {

        val inputData = Json.obj("ducr" -> "InvalidDucr!@#", "ducrPartId" -> "InvalidDucrPartId!@#")

        "return BadRequest (400) response" in {

          val result = controller().submitDucrPartDetails()(postRequest(inputData))

          status(result) mustBe BAD_REQUEST
        }

        "not call CacheRepository" in {

          controller().submitDucrPartDetails()(postRequest(inputData)).futureValue

          verifyZeroInteractions(cache)
        }
      }

      "provided with correct data" should {

        val inputData = Json.obj("ducr" -> validDucr, "ducrPartId" -> validDucrPartId)

        "call CacheRepository upsert" in {

          controller().submitDucrPartDetails()(postRequest(inputData)).futureValue

          verify(cache).upsert(any[Cache])
        }

        "provide CacheRepository with correct UcrBlock object" in {

          controller().submitDucrPartDetails()(postRequest(inputData)).futureValue

          val expectedUcrBlock = UcrBlock(ucrType = UcrType.DucrParts, ucr = validWholeDucrParts)
          theCacheUpserted.queryUcr mustBe defined
          theCacheUpserted.queryUcr.get mustBe expectedUcrBlock
        }

        "return SeeOther (303) response" in {

          val result = controller().submitDucrPartDetails()(postRequest(inputData))

          status(result) mustBe SEE_OTHER
        }

        "redirect to Choice page" in {

          val result = controller().submitDucrPartDetails()(postRequest(inputData))

          redirectLocation(result) mustBe Some(controllers.routes.ChoiceController.displayChoiceForm().url)
        }
      }
    }
  }
}
