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

package controllers.actions

import models.SignedInUser
import models.cache.{AssociateUcrAnswers, Cache}
import models.requests.{AuthenticatedRequest, JourneyRequest}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.`given`
import org.mockito.MockitoSugar.{mock, reset, verify}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import play.api.mvc.{Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.CacheRepository
import uk.gov.hmrc.auth.core.Enrolments

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class JourneyRefinerSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  private val movementRepository = mock[CacheRepository]
  private val arriveDepartAllowList = mock[ArriveDepartAllowList]
  private val block = mock[JourneyRequest[_] => Future[Result]]
  private val user = SignedInUser("eori", Enrolments(Set.empty))
  private val request = AuthenticatedRequest(FakeRequest(), user)
  private val answers = AssociateUcrAnswers()
  private val cache = Cache("eori", answers)
  private val redirectResult = Results.Redirect(controllers.routes.ChoiceController.displayChoices)

  private val refiner = new JourneyRefiner(movementRepository, arriveDepartAllowList)

  override def afterEach(): Unit = {
    reset(movementRepository, block)
    super.afterEach()
  }

  "refine" should {

    "add cache to the request" when {
      "cache contains non-empty answers" in {
        given(block.apply(any())).willReturn(Future.successful(Results.Ok))
        given(movementRepository.findByEori("eori")).willReturn(Future.successful(Some(cache)))

        await(refiner.invokeBlock(request, block)) mustBe Results.Ok

        theRequestBuilt mustBe JourneyRequest(cache, request)
      }

      def theRequestBuilt: JourneyRequest[_] = {
        val captor = ArgumentCaptor.forClass(classOf[JourneyRequest[_]])
        verify(block).apply(captor.capture())
        captor.getValue
      }
    }

    "block request" when {

      "cache is empty" in {
        given(movementRepository.findByEori("eori")).willReturn(Future.successful(None))

        await(refiner.invokeBlock(request, block)) mustBe redirectResult
      }

      "cached answers are empty" in {
        val emptyCache = Cache("eori", None, None, false, None)
        given(movementRepository.findByEori("eori")).willReturn(Future.successful(Some(emptyCache)))

        await(refiner.invokeBlock(request, block)) mustBe redirectResult
      }
    }
  }
}
