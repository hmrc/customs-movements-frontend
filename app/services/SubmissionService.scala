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
import forms.{Choice, Movement}
import javax.inject.{Inject, Singleton}
import metrics.MovementsMetrics
import uk.gov.hmrc.http.HeaderCarrier
import play.api.http.Status.INTERNAL_SERVER_ERROR

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
        connector.submitMovementDeclaration(data.ucrBlock.ucr, data.messageCode, data.toXml).map { submitResponse =>
          metrics.incrementCounter(data.messageCode)
          timer.stop()
          submitResponse.status
        }
      }
      case _ =>
        Future.successful(INTERNAL_SERVER_ERROR)
    }

  def submitDucrDisassociation(
    cacheId: String,
    ducr: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    // TODO Implement once the backend approach is defined

    //    val request = InventoryLinkingConsolidationRequest(
    //      messageCode = "EAC",
    //      transactionType = "",
    //      masterUCR = None,
    //      ucrBlock = Some(UcrBlock(ucr = ducr, ucrType = "D"))
    //    )
    Future.successful((): Unit)
  }
}
