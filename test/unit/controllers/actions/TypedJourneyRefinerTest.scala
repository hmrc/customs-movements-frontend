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
import models.cache._
import models.requests.{AuthenticatedRequest, JourneyRequest}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.`given`
import org.mockito.MockitoSugar.{mock, reset, verify}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.mvc.{Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.CacheRepository
import uk.gov.hmrc.auth.core.Enrolments

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TypedJourneyRefinerTest extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  private val movementRepository = mock[CacheRepository]
  private val arriveDepartAllowList = mock[ArriveDepartAllowList]
  private val block = mock[JourneyRequest[_] => Future[Result]]
  private val user = SignedInUser("eori", Enrolments(Set.empty))
  private val request = AuthenticatedRequest(FakeRequest(), user)
  private val arrivalAnswers = ArrivalAnswers()
  private val departureAnswers = DepartureAnswers()
  private val cacheWithArrivalAnswers = Cache("eori", arrivalAnswers)
  private val cacheWithDepartureAnswers = Cache("eori", departureAnswers)

  private val refiner = new JourneyRefiner(movementRepository, arriveDepartAllowList)

  override def afterEach(): Unit = {
    reset(movementRepository, arriveDepartAllowList, block)
    super.afterEach()
  }

  "refine" should {
    "permit request" when {
      "answers found" when {

        "on unshared journey" in {
          given(arriveDepartAllowList.contains("eori")).willReturn(true)
          given(block.apply(any())).willReturn(Future.successful(Results.Ok))
          given(movementRepository.findByEori("eori")).willReturn(Future.successful(Some(cacheWithArrivalAnswers)))

          await(refiner(JourneyType.ARRIVE).invokeBlock(request, block)) mustBe Results.Ok

          theRequestBuilt mustBe JourneyRequest(cacheWithArrivalAnswers, request)
        }

        "on shared journey" in {
          given(arriveDepartAllowList.contains("eori")).willReturn(true)
          given(block.apply(any())).willReturn(Future.successful(Results.Ok))
          given(movementRepository.findByEori("eori")).willReturn(Future.successful(Some(cacheWithArrivalAnswers)))

          await(refiner(JourneyType.DEPART, JourneyType.ARRIVE).invokeBlock(request, block)) mustBe Results.Ok

          theRequestBuilt mustBe JourneyRequest(cacheWithArrivalAnswers, request)
        }
      }

      def theRequestBuilt: JourneyRequest[_] = {
        val captor = ArgumentCaptor.forClass(classOf[JourneyRequest[_]])
        verify(block).apply(captor.capture())
        captor.getValue
      }
    }

    "block request" when {

      "answers not found" in {
        given(movementRepository.findByEori("eori")).willReturn(Future.successful(None))
        given(arriveDepartAllowList.contains("eori")).willReturn(true)

        await(refiner(JourneyType.ARRIVE).invokeBlock(request, block)) mustBe Results.Redirect(controllers.routes.ChoiceController.displayChoices)
      }

      "answers found of a different type" in {
        given(movementRepository.findByEori("eori")).willReturn(Future.successful(None))
        given(arriveDepartAllowList.contains("eori")).willReturn(true)

        await(refiner(JourneyType.DEPART).invokeBlock(request, block)) mustBe Results.Redirect(controllers.routes.ChoiceController.displayChoices)
      }

      for (journey <- Seq(JourneyType.ARRIVE, JourneyType.DEPART))
        s"answers type is $journey and user is not on allow list" in {
          val cache = if (journey == JourneyType.ARRIVE) cacheWithArrivalAnswers else cacheWithDepartureAnswers
          given(movementRepository.findByEori("eori")).willReturn(Future.successful(Some(cache)))
          given(arriveDepartAllowList.contains("eori")).willReturn(false)

          await(refiner(journey).invokeBlock(request, block)) mustBe Results.Redirect(controllers.routes.ChoiceController.displayChoices)
        }
    }
  }
}
