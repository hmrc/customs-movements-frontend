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

import base.{MockAuthConnector, MovementBaseSpec, URIHelper}
import controllers.util.RoutingHelper
import forms.DisassociateDucr
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito._
import org.scalatest.MustMatchers
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.test.Helpers._
import testdata.CommonTestData.correctUcr

import scala.concurrent.Future

class DisassociateDucrControllerSpec
    extends MovementBaseSpec with MockAuthConnector with ScalaFutures with MustMatchers with URIHelper {

  import DisassociateDucrControllerSpec._

  private val uri = uriWithContextPath("/disassociate-ducr")

  private val routingHelper = RoutingHelper(app, uri)

  private trait Test {
    reset(authConnectorMock, mockSubmissionService)
    when(mockSubmissionService.submitDucrDisassociation(any())(any(), any())).thenReturn(Future.successful(ACCEPTED))
    authorizedUser()
  }

  "Disassociate Ducr Controller on GET" should {

    "return 200 for get request" in new Test {

      status(routingHelper.routeGet()) must be(OK)
    }
  }

  "Disassociate Ducr Controller on POST" when {

    "provided with correct data" should {

      "return SeeOther code" in new Test {

        status(routingHelper.routePost(body = correctForm)) must be(SEE_OTHER)
      }

      "call SubmissionService" in new Test {

        routingHelper.routePost(body = correctForm).futureValue

        verify(mockSubmissionService).submitDucrDisassociation(meq(DisassociateDucr(correctUcr)))(any(), any())
      }

      "redirect to confirmation page" in new Test {

        redirectLocation(routingHelper.routePost(body = correctForm)) must be(
          Some(routes.DisassociateDucrConfirmationController.displayPage().url)
        )
        verify(mockSubmissionService).submitDucrDisassociation(meq(DisassociateDucr(correctUcr)))(any(), any())
      }
    }

    "provided with incorrect data" should {

      "return BadRequest code" in new Test {

        status(routingHelper.routePost(body = incorrectForm)) must be(BAD_REQUEST)
      }

      "not call SubmissionService" in new Test {

        routingHelper.routePost(body = incorrectForm).futureValue

        verifyZeroInteractions(mockSubmissionService)
      }
    }

    "SubmissionService returns status other than Accepted" should {
      "return InternalServerError code" in new Test {
        when(mockSubmissionService.submitDucrDisassociation(any())(any(), any()))
          .thenReturn(Future.successful(BAD_REQUEST))

        status(routingHelper.routePost(body = correctForm)) must be(INTERNAL_SERVER_ERROR)
      }
    }
  }

}

object DisassociateDucrControllerSpec {
  val correctForm = JsObject(Map("ducr" -> JsString(correctUcr)))
  val incorrectForm: JsValue = JsObject(Map("ducr" -> JsString("abc")))
}
