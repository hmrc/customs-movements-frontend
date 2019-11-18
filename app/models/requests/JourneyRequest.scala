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

package models.requests

import models.ReturnToStartException
import models.cache.Answers
import play.api.mvc.WrappedRequest

case class JourneyRequest[T](answers: Answers, request: AuthenticatedRequest[T]) extends WrappedRequest(request) {

  val eori: String = request.user.eori

  def answersAre[J <: Answers]: Boolean = answers.isInstanceOf[J]

  def answersAs[J <: Answers]: J = answers match {
    case ans: J => ans
    case _      => throw ReturnToStartException
  }
}
