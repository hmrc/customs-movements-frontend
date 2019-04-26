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

import forms.Choice.AllowedChoiceValues
import play.api.data.Form
import play.api.data.Forms.{mapping, optional, text}
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.wco.dec.inventorylinking.common.{AgentDetails, TransportDetails, UcrBlock}
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest

object MovementFormsAndIds {

  val locationForm = Location.form()
  val locationId = "Location"

  val transportForm = Form(Transport.mapping)
  val transportId = "Transport"
}

object Movement {

  def createMovementRequest(cacheMap: CacheMap, eori: String, choice: Choice): InventoryLinkingMovementRequest = {
    val referencesForm =
      cacheMap
        .getEntry[ConsignmentReferences](ConsignmentReferences.formId)
        .getOrElse(ConsignmentReferences("", "", ""))
    val departureDetails =
      cacheMap.getEntry[DepartureDetails](MovementDetails.formId)
    val location =
      cacheMap
        .getEntry[Location](MovementFormsAndIds.locationId)
        .getOrElse(Location(None))
    val transport =
      cacheMap
        .getEntry[Transport](MovementFormsAndIds.transportId)
        .getOrElse(Transport("", ""))

    // TODO: ucrType is hardcoded need to UPDATE after we allow user input for mucr
    InventoryLinkingMovementRequest(
      messageCode =
        if (choice.value.equals(AllowedChoiceValues.Arrival) || choice.value
              .equals(AllowedChoiceValues.Departure))
          choice.value
        else "",
      agentDetails = Some(AgentDetails(eori = Some(eori), agentLocation = location.goodsLocation)),
      ucrBlock = UcrBlock(ucr = referencesForm.reference, ucrType = "D"),
      goodsLocation = location.goodsLocation.getOrElse(""),
      goodsArrivalDateTime =
        if (choice.value.equals("EAL") && departureDetails.isDefined)
          Some(departureDetails.toString)
        else None,
      goodsDepartureDateTime =
        if (choice.value.equals("EDL") && departureDetails.isDefined)
          Some(departureDetails.toString)
        else None,
      masterUCR = None,
      masterOpt = None,
      movementReference = None,
      transportDetails = None
    )
  }
}
