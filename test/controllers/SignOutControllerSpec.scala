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

import config.AppConfig
import models.SignOutReason
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.{session_timed_out, user_signed_out}

class SignOutControllerSpec extends ControllerLayerSpec with ScalaFutures {

  private val mcc = stubMessagesControllerComponents()
  private val sessionTimedOutPage = mock[session_timed_out]
  private val userSignedOutPage = mock[user_signed_out]
  private val config = instanceOf[AppConfig]

  private val controller = new SignOutController(mcc, sessionTimedOutPage, userSignedOutPage, config)

  private val expectedBasGatewayHost = "http://localhost:9553/bas-gateway/sign-out-without-state"
  private val expectedUserSignOutUrl = s"${expectedBasGatewayHost}?continue=http%3A%2F%2Flocalhost%3A6796%2Fyou-have-signed-out"
  private val expectedTimeoutSignOutUrl = s"${expectedBasGatewayHost}?continue=http%3A%2F%2Flocalhost%3A6796%2Fwe-signed-you-out"

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(sessionTimedOutPage.apply()(any(), any())).thenReturn(HtmlFormat.empty)
    when(userSignedOutPage.apply()(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override def afterEach(): Unit = {
    reset(sessionTimedOutPage)
    reset(userSignedOutPage)

    super.afterEach()
  }

  "SignOutController on signOut" when {

    "provided with SessionTimeout parameter" should {

      "return 303 (SEE_OTHER) status" in {
        val result = controller.signOut(SignOutReason.SessionTimeout)(getRequest())
        status(result) mustBe SEE_OTHER
      }

      "redirect to /we-signed-you-out" in {
        val result = controller.signOut(SignOutReason.SessionTimeout)(getRequest())
        redirectLocation(result) mustBe Some(expectedTimeoutSignOutUrl)
      }
    }

    "provided with UserAction parameter" should {

      "return 303 (SEE_OTHER) status" in {
        val result = controller.signOut(SignOutReason.UserAction)(getRequest())
        status(result) mustBe SEE_OTHER
      }

      "redirect to /you-have-signed-out" in {
        val result = controller.signOut(SignOutReason.UserAction)(getRequest())
        redirectLocation(result) mustBe Some(expectedUserSignOutUrl)
      }
    }
  }

  "SignOutController on sessionTimeoutSignedOut" should {

    val controller = new SignOutController(mcc, sessionTimedOutPage, userSignedOutPage, config)

    "call sessionTimedOutPage" in {
      controller.sessionTimeoutSignedOut()(getRequest()).futureValue
      verify(sessionTimedOutPage).apply()(any(), any())
    }

    "return 200 status" in {
      val result = controller.sessionTimeoutSignedOut()(getRequest())
      status(result) mustBe OK
    }
  }

  "SignOutController on userSignedOut" should {

    val controller = new SignOutController(mcc, sessionTimedOutPage, userSignedOutPage, config)

    "call userSignedOutPage" in {
      controller.userSignedOut()(getRequest()).futureValue
      verify(userSignedOutPage).apply()(any(), any())
    }

    "return 200 status" in {
      val result = controller.userSignedOut()(getRequest())
      status(result) mustBe OK
    }
  }
}
