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

import forms.Choice._
import models.requests.{MovementDetailsRequest, MovementRequest}
import uk.gov.hmrc.http.cache.client.CacheMap

object Movement {

  def createMovementRequest(cacheMap: CacheMap, eori: String, choice: Choice): MovementRequest = {
    val consignmentReference =
      cacheMap
        .getEntry[ConsignmentReferences](ConsignmentReferences.formId)
        .getOrElse(ConsignmentReferences("", ""))

    val movementDateTime = choice match {
      case Departure =>
        cacheMap
          .getEntry[DepartureDetails](MovementDetails.formId)
          .map(_.dateOfDeparture.toString)
          .getOrElse("")
      case Arrival =>
        cacheMap
          .getEntry[ArrivalDetails](MovementDetails.formId)
          .map(res => s"${res.dateOfArrival.toString}T${res.timeOfArrival.toString}:00")
          .getOrElse("")
    }

    MovementRequest(
      choice = extractChoice(choice),
      consignmentReference = consignmentReference,
      movementDetails = MovementDetailsRequest(movementDateTime),
      location = cacheMap.getEntry[Location](Location.formId),
      transport = cacheMap.getEntry[Transport](Transport.formId),
      arrivalReference = cacheMap.getEntry[ArrivalReference](ArrivalReference.formId)
    )
  }

  private def extractChoice(choice: Choice): String = choice match {
    case Arrival   => "EAL"
    case Departure => "EDL"
    case _         => throw new IllegalArgumentException("Allowed is only arrival or departure here")
  }
}
