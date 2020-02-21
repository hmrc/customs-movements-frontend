/*
 * Copyright 2020 HM Revenue & Customs
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

import javax.inject.Inject
import models.cache.JourneyType.JourneyType
import models.requests.{AuthenticatedRequest, JourneyRequest}
import play.api.mvc.{ActionRefiner, Result, Results}
import repositories.CacheRepository

import scala.concurrent.{ExecutionContext, Future}

class JourneyRefiner @Inject()(movementRepository: CacheRepository)(implicit val exc: ExecutionContext)
    extends ActionRefiner[AuthenticatedRequest, JourneyRequest] {

  override protected def executionContext: ExecutionContext = exc

  override protected def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, JourneyRequest[A]]] =
    toJourneyRequest(request, Seq.empty[JourneyType]: _*).map(orRedirect)

  def apply(types: JourneyType*): ActionRefiner[AuthenticatedRequest, JourneyRequest] = new ActionRefiner[AuthenticatedRequest, JourneyRequest] {
    override protected def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, JourneyRequest[A]]] =
      toJourneyRequest(request, types: _*).map(orRedirect)

    override protected def executionContext: ExecutionContext = exc
  }

  private def toJourneyRequest[A](request: AuthenticatedRequest[A], types: JourneyType*): Future[Option[JourneyRequest[A]]] =
    movementRepository.findByEori(request.user.eori).map { cacheOption =>
      for {
        cache   <- cacheOption
        answers <- cache.answers
        if (types.isEmpty || types.contains(answers.`type`))
      } yield JourneyRequest(cache, request)
    }

  private def orRedirect[A](journeyOption: Option[JourneyRequest[A]]): Either[Result, JourneyRequest[A]] =
    journeyOption.toRight {
      Results.Redirect(controllers.routes.ChoiceController.displayChoiceForm())
    }

}
