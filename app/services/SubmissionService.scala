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

package services

import connectors.CustomsDeclareExportsMovementsConnector
import forms.Choice._
import forms._
import javax.inject.{Inject, Singleton}
import metrics.MovementsMetrics
import models.external.requests.ConsolidationRequest
import models.external.requests.ConsolidationRequestFactory._
import models.requests.MovementRequest
import play.api.http.Status.{ACCEPTED, INTERNAL_SERVER_ERROR}
import services.audit.{AuditService, AuditTypes}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

@Singleton
class SubmissionService @Inject()(
  cacheService: CustomsCacheService,
  connector: CustomsDeclareExportsMovementsConnector,
  auditService: AuditService,
  metrics: MovementsMetrics
)(implicit ec: ExecutionContext) {

  def submitMovementRequest(cacheId: String, eori: String, choice: Choice)(implicit hc: HeaderCarrier): Future[(Option[ConsignmentReferences], Int)] =
    cacheService.fetch(cacheId).flatMap {
      case Some(cacheMap) => {
        val data = Movement.createMovementRequest(cacheMap, eori, choice)
        val timer = metrics.startTimer(choice)

        auditService.auditAllPagesUserInput(choice, cacheMap)

        val movementAuditType =
          if (choice == Arrival) AuditTypes.AuditArrival else AuditTypes.AuditDeparture

        sendMovementRequest(choice, data).map { submitResponse =>
          metrics.incrementCounter(choice)
          auditService
            .auditMovements(eori, data, submitResponse.status.toString, movementAuditType)
          timer.stop()
          (Some(data.consignmentReference), submitResponse.status)
        }
      }
      case _ =>
        Future.successful((None, INTERNAL_SERVER_ERROR))
    }

  private def sendMovementRequest(
    choice: Choice,
    movementRequest: MovementRequest
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    choice match {
      case Arrival   => connector.sendArrivalDeclaration(movementRequest)
      case Departure => connector.sendDepartureDeclaration(movementRequest)
    }

  def submitDucrAssociation(mucrOptions: MucrOptions, associateDucr: AssociateDucr, eori: String)(
    implicit hc: HeaderCarrier
  ): Future[ConsolidationRequest] = {
    val timer = metrics.startTimer(AssociateDUCR)
    connector
      .sendConsolidationRequest(buildAssociationRequest(mucrOptions.mucr, associateDucr.ducr))
      .andThen {
        case Success(_) =>
          auditService.auditAssociate(eori, mucrOptions.mucr, associateDucr.ducr, ACCEPTED.toString)
          timer.stop()
          metrics.incrementCounter(AssociateDUCR)
      }
  }

  def submitDucrDisassociation(disassociateDucr: DisassociateDucr, eori: String)(implicit hc: HeaderCarrier): Future[ConsolidationRequest] = {
    val timer = metrics.startTimer(DisassociateDUCR)
    connector
      .sendConsolidationRequest(buildDisassociationRequest(disassociateDucr.ducr))
      .andThen {
        case Success(_) =>
          auditService.auditDisassociate(eori, disassociateDucr.ducr, ACCEPTED.toString)
          timer.stop()
          metrics.incrementCounter(DisassociateDUCR)
      }
  }

  def submitShutMucrRequest(shutMucr: ShutMucr, eori: String)(implicit hc: HeaderCarrier): Future[ConsolidationRequest] = {
    val timer = metrics.startTimer(ShutMUCR)
    connector.sendConsolidationRequest(buildShutMucrRequest(shutMucr.mucr)).andThen {
      case Success(_) =>
        auditService.auditShutMucr(eori, shutMucr.mucr, ACCEPTED.toString)
        timer.stop()
        metrics.incrementCounter(ShutMUCR)
    }
  }
}
