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

package base

import controllers.navigation.Navigator
import models.requests.RequestWithAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.`given`
import org.mockito.MockitoSugar.{mock, reset, verify}
import org.scalatest.{BeforeAndAfterEach, Suite}
import play.api.http.Status
import play.api.mvc._

trait MockNavigator extends BeforeAndAfterEach { self: Suite =>

  protected val navigator: Navigator = mock[Navigator]
  protected val aRedirectToTheNextPage: Result = mock[Result]

  override protected def beforeEach(): Unit = {
    given(navigator.continueTo(any[Call])(any[RequestWithAnswers[AnyContent]]))
      .willReturn(aRedirectToTheNextPage)
    given(aRedirectToTheNextPage.header).willReturn(ResponseHeader(Status.SEE_OTHER))
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(navigator)
  }

  protected def thePageNavigatedTo: Call = {
    val callCaptor: ArgumentCaptor[Call] = ArgumentCaptor.forClass(classOf[Call])
    verify(navigator).continueTo(callCaptor.capture())(any())
    callCaptor.getValue
  }

}
