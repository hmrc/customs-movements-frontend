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

import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.wco.dec.inventorylinking.common.{AgentDetails, TransportDetails, UcrBlock}
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest
import forms.Choice.AllowedChoiceValues._

object Movement {

  def createMovementRequest(cacheMap: CacheMap, eori: String, choice: Choice): InventoryLinkingMovementRequest = {
    val referencesForm =
      cacheMap
        .getEntry[ConsignmentReferences](ConsignmentReferences.formId)
        .getOrElse(ConsignmentReferences(None, "", ""))

    val departureDetails = choice.value match {
      case Departure => cacheMap.getEntry[DepartureDetails](MovementDetails.formId)
      case _         => None
    }
    //TODO:Seconds to be mapped with user input
    val arrivalDetails = choice.value match {
      case Arrival =>
        cacheMap
          .getEntry[ArrivalDetails](MovementDetails.formId)
          .map(res => s"${res.dateOfArrival.toString}T${res.timeOfArrival.fold("")(_.toString)}:00")
      case _ => None
    }
    val location =
      cacheMap.getEntry[Location](Location.formId).flatMap(_.goodsLocation)
    val transport =
      cacheMap
        .getEntry[Transport](Transport.formId)

    InventoryLinkingMovementRequest(
      messageCode = choice.value,
      agentDetails = Some(AgentDetails(eori = Some(eori), agentLocation = location)),
      ucrBlock = UcrBlock(ucr = referencesForm.referenceValue, ucrType = referencesForm.reference),
      goodsLocation = location.getOrElse(""),
      goodsArrivalDateTime = arrivalDetails,
      goodsDepartureDateTime = departureDetails.map(_.toString),
      transportDetails = mapTransportDetails(transport)
    )
  }

  private def mapTransportDetails(transport: Option[Transport]) =
    transport.map(
      data =>
        TransportDetails(transportMode = Some(data.modeOfTransport), transportNationality = Some(data.nationality))
    )

}
