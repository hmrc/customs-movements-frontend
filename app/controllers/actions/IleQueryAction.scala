/*
 * Copyright 2022 HM Revenue & Customs
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

import config.IleQueryConfig
import controllers.exception.InvalidFeatureStateException
import javax.inject.Inject
import models.requests.AuthenticatedRequest
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class IleQueryAction @Inject() (ileQueryConfig: IleQueryConfig)(implicit val exc: ExecutionContext)
    extends ActionFunction[AuthenticatedRequest, AuthenticatedRequest] {

  override def invokeBlock[A](request: AuthenticatedRequest[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] =
    if (ileQueryConfig.isIleQueryEnabled) block(request) else throw InvalidFeatureStateException

  override protected def executionContext: ExecutionContext = exc
}
