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

package base

import controllers.actions.{AuthActionImpl, EoriWhitelist}
import models.SignedInUser
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.PlayBodyParsers
import play.api.test.NoMaterializer
import testdata.CommonTestData.validEori
import testdata.MovementsTestData._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import utils.Stubs

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

trait MockAuthConnector extends MockitoSugar with Stubs {

  val authConnectorMock: AuthConnector = mock[AuthConnector]

  val eoriWhitelistMock: EoriWhitelist = mock[EoriWhitelist]

  val mockAuthAction =
    new AuthActionImpl(authConnectorMock, eoriWhitelistMock, PlayBodyParsers()(NoMaterializer))(global)

  def authorizedUser(user: SignedInUser = newUser(validEori)): Unit = {
    when(authConnectorMock.authorise(any(), ArgumentMatchers.eq(allEnrolments))(any(), any()))
      .thenReturn(Future.successful(user.enrolments))
    when(eoriWhitelistMock.contains(any())).thenReturn(true)
  }

  def userWithoutEori(user: SignedInUser = newUser("")): Unit = {
    when(authConnectorMock.authorise(any(), ArgumentMatchers.eq(allEnrolments))(any(), any()))
      .thenThrow(InsufficientEnrolments())
    when(eoriWhitelistMock.contains(any())).thenReturn(true)
  }
}
