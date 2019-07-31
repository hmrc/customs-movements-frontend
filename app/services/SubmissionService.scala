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
import play.api.http.Status.INTERNAL_SERVER_ERROR
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubmissionService @Inject()(
  cacheService: CustomsCacheService,
  connector: CustomsDeclareExportsMovementsConnector,
  metrics: MovementsMetrics
) {

  def submitMovementRequest(cacheId: String, eori: String, choice: Choice)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Int] =
    cacheService.fetch(cacheId).flatMap {
      case Some(cacheMap) => {
        val data = Movement.createMovementRequest(cacheMap, eori, choice)
        val timer = metrics.startTimer(data.messageCode)

        choice.value match {
          case Arrival =>
            connector.sendArrivalDeclaration(data.toXml).map { submitResponse =>
              metrics.incrementCounter(data.messageCode)
              timer.stop()
              submitResponse.status
            }
          case Departure =>
            connector.sendDepartureDeclaration(data.toXml).map { submitResponse =>
              metrics.incrementCounter(data.messageCode)
              timer.stop()
              submitResponse.status
            }
        }
      }
      case _ =>
        Future.successful(INTERNAL_SERVER_ERROR)
    }

  def submitDucrAssociation(
    mucrOptions: MucrOptions,
    associateDucr: AssociateDucr
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Int] =
    connector
      .sendAssociationRequest(buildAssociationRequest(mucr = mucrOptions.mucr, ducr = associateDucr.ducr).toString)
      .map(_.status)

  def submitDucrDisassociation(
    disassociateDucr: DisassociateDucr
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Int] =
    connector.sendDisassociationRequest(buildDisassociationRequest(disassociateDucr.ducr).toString).map(_.status)

  def submitShutMucrRequest(shutMucr: ShutMucr)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Int] =
    connector.sendShutMucrRequest(buildShutMucrRequest(shutMucr.mucr).toString).map(_.status)
}
