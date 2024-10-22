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

package services

import connectors.CustomsDeclareExportsMovementsConnector
import connectors.exchanges._
import forms._
import metrics.MovementsMetrics
import models.ReturnToStartException
import models.cache.JourneyType.JourneyType
import models.cache._
import models.confirmation.SubmissionResult
import repositories.CacheRepository
import services.audit.{AuditService, AuditType}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class SubmissionService @Inject() (
  repository: CacheRepository,
  connector: CustomsDeclareExportsMovementsConnector,
  auditService: AuditService,
  metrics: MovementsMetrics,
  movementBuilder: MovementBuilder
)(implicit ec: ExecutionContext) {

  private val success = "Success"
  private val failed = "Failed"

  def submit(eori: String, answers: DisassociateUcrAnswers, cacheUuid: String)(implicit hc: HeaderCarrier): Future[String] = {
    val ucr = answers.ucr.getOrElse(throw ReturnToStartException).ucr
    val exchange = answers.ucr.map(_.kind) match {
      case Some(UcrType.Ducr) => DisassociateDUCRRequest(eori, ucr)
      case Some(UcrType.Mucr) => DisassociateMUCRRequest(eori, ucr)
      case _                  => throw new IllegalArgumentException("Incorrect Ucr type")
    }

    connector
      .submit(exchange)
      .andThen {
        case Success(conversationId) =>
          repository
            .removeByEoriAndAnswerCacheId(eori, cacheUuid)
            .flatMap(_ => auditService.auditDisassociate(eori, ucr, success, Some(conversationId)))
        case Failure(_) => auditService.auditDisassociate(eori, ucr, failed)
      }
  }

  def submit(eori: String, answers: AssociateUcrAnswers, cacheUuid: String)(implicit hc: HeaderCarrier): Future[String] = {
    val mucr = answers.mucrOptions.map(_.mucr).getOrElse(throw ReturnToStartException)
    val ucr = answers.associateUcr.map(_.ucr).getOrElse(throw ReturnToStartException)
    val exchange = answers.associateUcr.map(_.kind) match {
      case Some(UcrType.Ducr) => AssociateDUCRRequest(eori, mucr, ucr)
      case Some(UcrType.Mucr) => AssociateMUCRRequest(eori, mucr, ucr)
      case _                  => throw new IllegalArgumentException("Incorrect Ucr type")
    }

    connector
      .submit(exchange)
      .andThen {
        case Success(conversationId) =>
          repository
            .removeByEoriAndAnswerCacheId(eori, cacheUuid)
            .map(_ => auditService.auditAssociate(eori, mucr, ucr, success, Some(conversationId)))
        case Failure(conversationId) => auditService.auditAssociate(eori, mucr, ucr, failed)
      }
  }

  def submit(eori: String, answers: ShutMucrAnswers, cacheUuid: String)(implicit hc: HeaderCarrier): Future[String] = {
    val mucr = answers.shutMucr.map(_.mucr).getOrElse(throw ReturnToStartException)

    connector
      .submit(ShutMUCRRequest(eori, mucr))
      .andThen {
        case Success(conversationId) =>
          repository
            .removeByEoriAndAnswerCacheId(eori, cacheUuid)
            .flatMap(_ => auditService.auditShutMucr(eori, mucr, success, Some(conversationId)))
        case Failure(_) => auditService.auditShutMucr(eori, mucr, failed)
      }
  }

  def submit(eori: String, answers: MovementAnswers, cacheUuid: String)(implicit hc: HeaderCarrier): Future[SubmissionResult] = {
    val journeyType = answers.`type`
    val data = movementBuilder.createMovementRequest(eori, answers)
    val timer = metrics.startTimer(Choice(journeyType))

    auditService.auditAllPagesUserInput(eori, answers)

    (for {
      conversationId <- connector.submit(data)
      _ <- repository.removeByEoriAndAnswerCacheId(eori, cacheUuid)
    } yield SubmissionResult(conversationId, data.consignmentReference)).andThen {
      case Success(subResult) => auditService.auditMovements(data, success, movementAuditType(journeyType), Some(subResult.conversationId))
      case Failure(_)         => auditService.auditMovements(data, failed, movementAuditType(journeyType))
    }.andThen { case _ =>
      metrics.incrementCounter(Choice(journeyType))
      timer.stop()
    }
  }

  private def movementAuditType(journeyType: JourneyType): AuditType.Value =
    (journeyType: @unchecked) match {
      case JourneyType.ARRIVE => AuditType.AuditArrival
      case JourneyType.DEPART => AuditType.AuditDeparture
    }
}
