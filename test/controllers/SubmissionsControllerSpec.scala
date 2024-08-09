/*
 * Copyright 2024 HM Revenue & Customs
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

import connectors.CustomsDeclareExportsMovementsConnector
import models.now
import models.submissions.Submission
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, anyString, eq => meq}
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import testdata.MovementsTestData.exampleSubmission
import views.html.movements

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class SubmissionsControllerSpec extends ControllerLayerSpec with ScalaFutures {

  private val mockMovementsPage = mock[movements]
  private val connector = mock[CustomsDeclareExportsMovementsConnector]

  private val controller =
    new SubmissionsController(SuccessfulAuth(), connector, stubMessagesControllerComponents(), mockMovementsPage)(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(mockMovementsPage.apply(any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockMovementsPage, connector)

    super.afterEach()
  }

  "Submissions Controller on displayPage" should {

    "return 200 (OK)" in {
      when(connector.fetchAllSubmissions(any[String])(any())).thenReturn(Future.successful(Seq.empty))

      val result = controller.displayPage(getRequest())

      status(result) mustBe OK
    }

    "call connector for all Submissions" in {
      when(connector.fetchAllSubmissions(any[String])(any())).thenReturn(Future.successful(Seq.empty))

      controller.displayPage(getRequest()).futureValue

      verify(connector).fetchAllSubmissions(meq(user.eori))(any())
    }

    "return 200 (OK)" when {
      "display page is invoked with submissions in descending order" in {
        val submission1 = exampleSubmission(requestTimestamp = now.minusSeconds(60))
        val submission2 = exampleSubmission(requestTimestamp = now.minusSeconds(30))
        val submission3 = exampleSubmission(requestTimestamp = now)

        when(connector.fetchAllSubmissions(anyString())(any()))
          .thenReturn(Future.successful(Seq(submission1, submission2, submission3)))

        val result = controller.displayPage(getRequest())

        status(result) mustBe OK

        val captor: ArgumentCaptor[Seq[Submission]] =
          ArgumentCaptor.forClass(classOf[Seq[Submission]])
        verify(mockMovementsPage).apply(captor.capture())(any(), any())

        val submissions: Seq[Submission] = captor.getValue

        submissions must be(Seq(submission3, submission2, submission1))
      }
    }
  }
}
