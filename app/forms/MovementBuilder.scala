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

package forms

import java.time.ZoneId
import java.time.format.DateTimeFormatter

import javax.inject.Inject
import models.ReturnToStartException
import models.cache.{Answers, ArrivalAnswers, DepartureAnswers}
import models.requests.{MovementDetailsRequest, MovementRequest, MovementType}

class MovementBuilder @Inject()(zoneId: ZoneId) {

  private val movementDateTimeFormatter = DateTimeFormatter.ISO_INSTANT

  def createMovementRequest(providerId: String, answers: Answers): MovementRequest = answers match {
    case arrivalAnswers: ArrivalAnswers     => createMovementArrivalRequest(providerId, arrivalAnswers)
    case departureAnswers: DepartureAnswers => createMovementDepartureRequest(providerId, departureAnswers)
  }

  private def createMovementArrivalRequest(eori: String, answers: ArrivalAnswers) =
    MovementRequest(
      eori = eori,
      choice = MovementType.Arrival,
      consignmentReference = answers.consignmentReferences.getOrElse(throw ReturnToStartException),
      movementDetails = movementDetails(answers),
      location = answers.location,
      arrivalReference = answers.arrivalReference
    )

  private def createMovementDepartureRequest(eori: String, answers: DepartureAnswers) =
    MovementRequest(
      eori = eori,
      choice = MovementType.Departure,
      consignmentReference = answers.consignmentReferences.getOrElse(throw ReturnToStartException),
      movementDetails = movementDetails(answers),
      location = answers.location,
      arrivalReference = answers.arrivalReference,
      transport = answers.transport
    )

  private def movementDetails(answers: ArrivalAnswers) =
    MovementDetailsRequest(
      answers.arrivalDetails
        .map(arrival => movementDateTimeFormatter.format(arrival.goodsArrivalMoment(zoneId)))
        .getOrElse("")
    )

  private def movementDetails(answers: DepartureAnswers) =
    MovementDetailsRequest(
      answers.departureDetails
        .map(departure => movementDateTimeFormatter.format(departure.goodsDepartureMoment(zoneId)))
        .getOrElse("")
    )
}
