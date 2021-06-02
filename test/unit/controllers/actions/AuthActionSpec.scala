/*
 * Copyright 2021 HM Revenue & Customs
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

import controllers.routes
import models.requests.AuthenticatedRequest
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{AnyContent, Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Retrieval}
import utils.Stubs

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthActionSpec extends AnyWordSpec with Matchers with MockitoSugar with Stubs {

  private val connector = mock[AuthConnector]
  private val allowList = mock[EoriAllowList]
  private val parsers = stubControllerComponents().parsers
  private val block = mock[AuthenticatedRequest[AnyContent] => Future[Result]]
  private val action = new AuthActionImpl(connector, allowList, parsers)

  "Auth Action" should {
    val controllerResponse = mock[Result]

    "delegate to controller" when {

      "auth success for allow listed eori" in {
        val enrolment = Enrolment("HMRC-CUS-ORG", Seq(EnrolmentIdentifier("EORINumber", "eori")), "state")
        given(block.apply(any())).willReturn(Future.successful(controllerResponse))
        given(connector.authorise(any(), any[Retrieval[Enrolments]]())(any(), any())).willReturn(Future.successful(Enrolments(Set(enrolment))))
        given(allowList.contains(any())).willReturn(true)

        val result: Result = await(action.invokeBlock(FakeRequest(), block))

        result mustBe controllerResponse
        theAuthCondition mustBe Enrolment("HMRC-CUS-ORG")
      }
    }

    "throw Insufficient Enrolments" when {
      "role is missing" in {
        given(connector.authorise(any(), any[Retrieval[Enrolments]]())(any(), any())).willReturn(Future.successful(Enrolments(Set.empty)))

        intercept[InsufficientEnrolments] {
          await(action.invokeBlock(FakeRequest(), block))
        }
      }

      "eori is missing" in {
        val enrolment = Enrolment("HMRC-CUS-ORG", Seq.empty, "state")
        given(connector.authorise(any(), any[Retrieval[Enrolments]]())(any(), any())).willReturn(Future.successful(Enrolments(Set(enrolment))))

        intercept[InsufficientEnrolments] {
          await(action.invokeBlock(FakeRequest(), block))
        }
      }
    }

    "redirect to unauthorized" when {
      val enrolment = Enrolment("HMRC-CUS-ORG", Seq(EnrolmentIdentifier("EORINumber", "eori")), "state")

      "eori missing from allowList" in {
        given(connector.authorise(any(), any[Retrieval[Enrolments]]())(any(), any())).willReturn(Future.successful(Enrolments(Set(enrolment))))
        given(allowList.contains(any())).willReturn(false)

        val result: Result = await(action.invokeBlock(FakeRequest(), block))

        result mustBe Results.Redirect(routes.UnauthorisedController.onPageLoad())
      }
    }

    def theAuthCondition: Predicate = {
      val captor = ArgumentCaptor.forClass(classOf[Predicate])
      verify(connector).authorise(captor.capture(), any[Retrieval[Option[Credentials]]])(any(), any())
      captor.getValue
    }
  }
}
