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
import forms.Choice.AllowedChoiceValues.{Arrival, Departure}
import forms._
import javax.inject.{Inject, Singleton}
import metrics.MovementsMetrics
import models.external.requests.InventoryLinkingConsolidationRequestFactory._
import models.requests.JourneyRequest
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.{JsObject, Json}
import services.audit.AuditService
import services.audit.EventData._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

@Singleton
class SubmissionService @Inject()(
  cacheService: CustomsCacheService,
  connector: CustomsDeclareExportsMovementsConnector,
  auditService: AuditService,
  metrics: MovementsMetrics
) {

  def submitMovementRequest(
    cacheId: String,
    eori: String,
    choice: Choice
  )(implicit hc: HeaderCarrier, request: JourneyRequest[_], ec: ExecutionContext): Future[Int] =
    cacheService.fetch(cacheId).flatMap {
      case Some(cacheMap) => {
        val data = Movement.createMovementRequest(cacheMap, eori, choice)
        val timer = metrics.startTimer(choice.value)

        auditService.auditAllPagesUserInput(choice, Json.toJson(cacheMap).as[JsObject])

        sendMovementRequest(choice, data).map { submitResponse =>
          metrics.incrementCounter(data.messageCode)
          auditService.audit(choice, auditData(data, Success.toString))
          timer.stop()
          submitResponse.status
        }
      }
      case _ =>
        Future.successful(INTERNAL_SERVER_ERROR)
    }

  private def sendMovementRequest(
    choice: Choice,
    data: InventoryLinkingMovementRequest
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    choice.value match {
      case Arrival   => connector.sendArrivalDeclaration(data.toXml)
      case Departure => connector.sendDepartureDeclaration(data.toXml)
    }

  private def auditData(data: InventoryLinkingMovementRequest, result: String)(
    implicit request: JourneyRequest[_]
  ): Map[String, String] =
    Map(
      EORI.toString -> request.authenticatedRequest.user.eori,
      MessageCode.toString -> data.messageCode,
      UCRType.toString -> data.ucrBlock.ucrType,
      UCR.toString -> data.ucrBlock.ucr,
      MovementReference.toString -> data.movementReference.getOrElse(""),
      SubmissionResult.toString -> result
    )

  def submitDucrAssociation(
    mucrOptions: MucrOptions,
    associateDucr: AssociateDucr
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Int] = {
    val timer = metrics.startTimer(Choice.AllowedChoiceValues.AssociateDUCR)
    connector
      .sendAssociationRequest(buildAssociationRequest(mucr = mucrOptions.mucr, ducr = associateDucr.ducr).toString)
      .map(_.status)
      .andThen {
        case Success(_) =>
          timer.stop()
          metrics.incrementCounter(Choice.AllowedChoiceValues.AssociateDUCR)
      }
  }

  def submitDucrDisassociation(
    disassociateDucr: DisassociateDucr
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Int] = {
    val timer = metrics.startTimer(Choice.AllowedChoiceValues.DisassociateDUCR)
    connector
      .sendDisassociationRequest(buildDisassociationRequest(disassociateDucr.ducr).toString)
      .map(_.status)
      .andThen {
        case Success(_) =>
          timer.stop()
          metrics.incrementCounter(Choice.AllowedChoiceValues.DisassociateDUCR)
      }
  }

  def submitShutMucrRequest(shutMucr: ShutMucr)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Int] = {
    val timer = metrics.startTimer(Choice.AllowedChoiceValues.ShutMucr)
    connector.sendShutMucrRequest(buildShutMucrRequest(shutMucr.mucr).toString).map(_.status).andThen {
      case Success(_) =>
        timer.stop()
        metrics.incrementCounter(Choice.AllowedChoiceValues.ShutMucr)
    }
  }
}
