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

package controllers.consolidations

import base.MockFactory.buildSubmissionServiceMock
import base.{MockAuthConnector, URIHelper}
import controllers.storage.FlashKeys
import controllers.util.RoutingHelper
import forms.ShutMucr
import forms.ShutMucrSpec._
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SubmissionService
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.Future

class ShutMucrControllerSpec
    extends WordSpec with GuiceOneAppPerSuite with MockAuthConnector with ScalaFutures with MustMatchers
    with URIHelper {

  private val shutMucrUri = uriWithContextPath("/shut-mucr")

  private val submissionServiceMock = buildSubmissionServiceMock
  override lazy val app: Application =
    GuiceApplicationBuilder()
      .overrides(bind[AuthConnector].to(authConnectorMock), bind[SubmissionService].to(submissionServiceMock))
      .build()

  private val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  private implicit val messages: Messages = messagesApi.preferred(FakeRequest())

  private val routingHelper = RoutingHelper(app, shutMucrUri)

  private trait Test {
    reset(authConnectorMock, submissionServiceMock)
    when(submissionServiceMock.submitShutMucrRequest(any())(any(), any())).thenReturn(Future.successful(ACCEPTED))
    authorizedUser()
  }

  "ShutMucr Controller on GET" should {

    "return Ok code" in new Test {

      status(routingHelper.routeGet()) must be(OK)
    }
  }

  "ShutMucr Controller on POST" when {

    "provided with correct data" should {

      "return SeeOther code" in new Test {

        status(routingHelper.routePost(body = correctShutMucrJSON)) must be(SEE_OTHER)
      }

      "call SubmissionService passing ShutMucr object" in new Test {

        routingHelper.routePost(body = correctShutMucrJSON).futureValue

        val expectedShutMucr = ShutMucr(correctMucr)
        verify(submissionServiceMock).submitShutMucrRequest(meq(expectedShutMucr))(any(), any())
      }

      "redirect to ShutMucrConfirmation page" in new Test {

        redirectLocation(routingHelper.routePost(body = correctShutMucrJSON)) must be(
          Some(routes.ShutMucrConfirmationController.displayPage().url)
        )
      }

      "add MUCR to Flash" in new Test {

        val flashValue = flash(routingHelper.routePost(body = correctShutMucrJSON))
        flashValue.get(FlashKeys.MUCR) must be(defined)
        flashValue.get(FlashKeys.MUCR).get must equal(correctMucr)
      }
    }

    "provided with incorrect data" should {

      "return BadRequest code" in new Test {

        status(routingHelper.routePost(body = incorrectShutMucrJSON)) must be(BAD_REQUEST)
      }

      "return Shut a MUCR page" in new Test {

        val result = routingHelper.routePost(body = incorrectShutMucrJSON)

        result.futureValue
        contentAsString(result) must include(messages("shutMucr.title"))
      }

      "not call SubmissionService" in new Test {

        routingHelper.routePost(body = incorrectShutMucrJSON).futureValue

        verifyZeroInteractions(submissionServiceMock)
      }
    }

    "SubmissionService returns status other than Accepted" should {
      "return InternalServerError code" in new Test {
        when(submissionServiceMock.submitShutMucrRequest(any())(any(), any()))
          .thenReturn(Future.successful(BAD_REQUEST))

        status(routingHelper.routePost(body = correctShutMucrJSON)) must be(INTERNAL_SERVER_ERROR)
      }
    }

  }

}
