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
import models.cache.Answers
import models.cache.JourneyType.JourneyType
import models.requests.{AuthenticatedRequest, JourneyRequest}
import play.api.mvc.{ActionRefiner, Result, Results}
import repositories.CacheRepository

import scala.concurrent.{ExecutionContext, Future}

class JourneyRefiner @Inject()(movementRepository: CacheRepository)(implicit val exc: ExecutionContext)
    extends ActionRefiner[AuthenticatedRequest, JourneyRequest] {

  override protected def executionContext: ExecutionContext = exc

  private def refiner[A](request: AuthenticatedRequest[A], types: JourneyType*): Future[Either[Result, JourneyRequest[A]]] =
    movementRepository.findByEori(request.user.eori).map(_.map(_.answers)).map {
      case Some(answers: Answers) if types.isEmpty || types.contains(answers.`type`) =>
        Right(JourneyRequest(answers, request))
      case _ =>
        Left(Results.Redirect(controllers.routes.ChoiceController.displayChoiceForm()))
    }

  override protected def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, JourneyRequest[A]]] =
    refiner(request, Seq.empty[JourneyType]: _*)

  def apply(types: JourneyType*): ActionRefiner[AuthenticatedRequest, JourneyRequest] = new ActionRefiner[AuthenticatedRequest, JourneyRequest] {
    override protected def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, JourneyRequest[A]]] =
      refiner(request, types: _*)

    override protected def executionContext: ExecutionContext = exc
  }
}
