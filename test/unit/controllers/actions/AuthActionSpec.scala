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

import base.MockAuthConnector
import controllers.routes
import models.requests.AuthenticatedRequest
import models.AuthKey.{enrolment, eoriIdentifierKey, hashIdentifierKey}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mockito.{reset, when}
import org.mockito.MockitoSugar.{mock, verify}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.{AnyContent, Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Retrieval}
import utils.Stubs

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthActionSpec extends AnyWordSpec with Matchers with Stubs with MockAuthConnector with BeforeAndAfterEach {

  private val allowList = mock[EoriAllowList]
  private val parsers = stubControllerComponents().parsers
  private val block = mock[AuthenticatedRequest[AnyContent] => Future[Result]]
  private val action = new AuthActionImpl(authConnectorMock, allowList, parsers, appConfig)

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(appConfig)
    reset(allowList)
    reset(authConnectorMock)

    when(appConfig.maybeTdrHashSalt).thenReturn(None)
    when(allowList.allows(any())).thenReturn(true)
  }

  "Auth Action" should {
    val controllerResponse = mock[Result]

    "delegate to controller" when {

      "auth success for eori on allow list" when {
        "TDRSecret is not required" in {
          authorizedUser()

          given(block.apply(any())).willReturn(Future.successful(controllerResponse))

          val result: Result = await(action.invokeBlock(FakeRequest(), block))

          result mustBe controllerResponse
          theAuthCondition mustBe Enrolment(enrolment)
        }

        "TDRSecret is required" in {
          when(appConfig.maybeTdrHashSalt).thenReturn(Some("SomeSuperSecret"))
          authorizedUser()

          given(block.apply(any())).willReturn(Future.successful(controllerResponse))

          val result: Result = await(action.invokeBlock(FakeRequest(), block))

          result mustBe controllerResponse
          theAuthCondition mustBe Enrolment(enrolment)
        }
      }
    }

    "throw Insufficient Enrolments" when {
      "role is missing" in {
        given(authConnectorMock.authorise(any(), any[Retrieval[Enrolments]]())(any(), any())).willReturn(Future.successful(Enrolments(Set.empty)))

        intercept[InsufficientEnrolments] {
          await(action.invokeBlock(FakeRequest(), block))
        }
      }

      "eori is missing" in {
        val enrolments = Set(Enrolment(enrolment, Seq.empty, "state"))
        given(authConnectorMock.authorise(any(), any[Retrieval[Enrolments]]())(any(), any())).willReturn(Future.successful(Enrolments(enrolments)))

        intercept[InsufficientEnrolments] {
          await(action.invokeBlock(FakeRequest(), block))
        }
      }
    }

    "redirect to unauthorized" when {

      "eori value provided is missing from allowList" in {
        val enrolments = Set(Enrolment(enrolment, Seq(EnrolmentIdentifier(eoriIdentifierKey, "eori")), "state"))
        given(authConnectorMock.authorise(any(), any[Retrieval[Enrolments]]())(any(), any())).willReturn(Future.successful(Enrolments(enrolments)))
        given(allowList.allows(any())).willReturn(false)

        val result: Result = await(action.invokeBlock(FakeRequest(), block))

        result mustBe Results.Redirect(routes.UnauthorisedController.onPageLoad)
      }

      "maybeTdrHashSalt config key is set and" when {

        "user does not provide a TDRSecret value" in {
          when(appConfig.maybeTdrHashSalt).thenReturn(Some("SomeSuperSecret"))
          val enrolments = Set(Enrolment(enrolment, Seq(EnrolmentIdentifier(eoriIdentifierKey, "eori")), "state"))
          given(authConnectorMock.authorise(any(), any[Retrieval[Enrolments]]())(any(), any())).willReturn(Future.successful(Enrolments(enrolments)))

          val result: Result = await(action.invokeBlock(FakeRequest(), block))

          result mustBe Results.Redirect(routes.UnauthorisedController.onPageLoad)
        }

        "user provides an incorrect TDRSecret value" in {
          when(appConfig.maybeTdrHashSalt).thenReturn(Some("SomeSuperSecret"))
          val enrolments = Set(
            Enrolment(enrolment, Seq(EnrolmentIdentifier(eoriIdentifierKey, "eori"), EnrolmentIdentifier(hashIdentifierKey, "tdrSecret")), "state")
          )
          given(authConnectorMock.authorise(any(), any[Retrieval[Enrolments]]())(any(), any())).willReturn(Future.successful(Enrolments(enrolments)))

          val result: Result = await(action.invokeBlock(FakeRequest(), block))

          result mustBe Results.Redirect(routes.UnauthorisedController.onPageLoad)
        }
      }
    }

    def theAuthCondition: Predicate = {
      val captor = ArgumentCaptor.forClass(classOf[Predicate])
      verify(authConnectorMock).authorise(captor.capture(), any[Retrieval[Option[Credentials]]])(any(), any())
      captor.getValue
    }
  }
}
