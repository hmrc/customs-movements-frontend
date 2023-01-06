/*
 * Copyright 2023 HM Revenue & Customs
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

package forms

import java.time.ZoneId
import java.time.format.DateTimeFormatter

import connectors.exchanges.ActionType.MovementType
import connectors.exchanges.{MovementDetailsRequest, MovementRequest}
import javax.inject.Inject
import models.ReturnToStartException
import models.cache.{Answers, ArrivalAnswers, DepartureAnswers}

class MovementBuilder @Inject() (zoneId: ZoneId) {

  private val movementDateTimeFormatter = DateTimeFormatter.ISO_INSTANT

  def createMovementRequest(providerId: String, answers: Answers): MovementRequest =
    (answers: @unchecked) match {
      case arrivalAnswers: ArrivalAnswers     => createMovementArrivalRequest(providerId, arrivalAnswers)
      case departureAnswers: DepartureAnswers => createMovementDepartureRequest(providerId, departureAnswers)
    }

  private def createMovementArrivalRequest(eori: String, answers: ArrivalAnswers): MovementRequest =
    MovementRequest(
      eori = eori,
      choice = MovementType.Arrival,
      consignmentReference = answers.consignmentReferences.getOrElse(throw ReturnToStartException),
      movementDetails = movementDetails(answers),
      location = answers.location
    )

  private def createMovementDepartureRequest(eori: String, answers: DepartureAnswers): MovementRequest =
    MovementRequest(
      eori = eori,
      choice = MovementType.Departure,
      consignmentReference = answers.consignmentReferences.getOrElse(throw ReturnToStartException),
      movementDetails = movementDetails(answers),
      location = answers.location,
      transport = answers.transport
    )

  private def movementDetails(answers: ArrivalAnswers): MovementDetailsRequest =
    MovementDetailsRequest(
      answers.arrivalDetails
        .map(arrival => movementDateTimeFormatter.format(arrival.goodsArrivalMoment(zoneId)))
        .getOrElse("")
    )

  private def movementDetails(answers: DepartureAnswers): MovementDetailsRequest =
    MovementDetailsRequest(
      answers.departureDetails
        .map(departure => movementDateTimeFormatter.format(departure.goodsDepartureMoment(zoneId)))
        .getOrElse("")
    )
}
