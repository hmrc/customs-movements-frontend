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

package controllers.actions

import base.UnitSpec
import play.api.mvc._
import config.DucrPartConfig
import controllers.exception.InvalidFeatureStateException
import models.requests.AuthenticatedRequest
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import play.api.mvc.Result
import play.twirl.api.HtmlFormat

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class DucrPartActionSpec extends UnitSpec with BeforeAndAfterEach with ScalaFutures {

  private val ducrPartsConfig = mock[DucrPartConfig]
  private val request = mock[AuthenticatedRequest[_]]
  private val functionBlock = mock[AuthenticatedRequest[_] => Future[Result]]
  private val controllerResult = mock[Result]

  private val ducrPartsAction = new DucrPartsAction(ducrPartsConfig)

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(ducrPartsConfig, request, functionBlock)
    when(functionBlock.apply(any())).thenReturn(Future.successful(controllerResult))
  }

  override def afterEach(): Unit = {
    reset(ducrPartsConfig, request, functionBlock)

    super.afterEach()
  }

  "DucrPartsAction on invokeBlock" when {

    "ducrParts feature is enabled" should {

      "call provided function block" in {

        when(ducrPartsConfig.isDucrPartsEnabled).thenReturn(true)

        ducrPartsAction.invokeBlock(request, functionBlock).futureValue

        verify(functionBlock).apply(any())
      }

      "pass provided request to function block" in {

        when(ducrPartsConfig.isDucrPartsEnabled).thenReturn(true)

        ducrPartsAction.invokeBlock(request, functionBlock).futureValue

        verify(functionBlock).apply(meq(request))
      }
    }

    "ducrParts feature is disabled" should {

      "throw InvalidFeatureStateException" in {

        when(ducrPartsConfig.isDucrPartsEnabled).thenReturn(false)

        intercept[InvalidFeatureStateException](ducrPartsAction.invokeBlock(request, functionBlock).futureValue)
      }
    }
  }
}
