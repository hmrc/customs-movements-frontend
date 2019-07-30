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

package base.testdata
import forms.GoodsDeparted.AllowedPlaces
import forms._
import forms.common.{Date, Time}
import models.SignedInUser
import play.api.libs.json._
import uk.gov.hmrc.auth.core.{Enrolment, Enrolments}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest

object MovementsTestData {

  def newUser(eori: String): SignedInUser =
    SignedInUser(eori, Enrolments(Set(Enrolment("HMRC-CUS-ORG").withIdentifier("EORINumber", eori))))

  val incorrectTransport: JsValue = JsObject(
    Map(
      "transportId" -> JsString("Transport Id"),
      "transportMode" -> JsString("Transport mode"),
      "transportNationality" -> JsString("Transport nationality")
    )
  )

  def consignmentReferences(refType: String) = ConsignmentReferences(refType, CommonTestData.correctUcr)
  val date = Date(Some(10), Some(8), Some(2018))
  val departureDetails = DepartureDetails(date)

  def arrivalDepartureTimes(movementType: String): JsValue = movementType match {
    case "EAL" => Json.toJson(ArrivalDetails(date, Some(Time(Some("13"), Some("34")))))
    case _     => Json.toJson(DepartureDetails(date))
  }

  val goodsDeparted = GoodsDeparted(AllowedPlaces.outOfTheUk)

  val location: JsValue = Json.toJson(Location("A", "Y", "correct", "PL"))

  val correctTransport: JsValue = JsObject(Map("modeOfTransport" -> JsString("M"), "nationality" -> JsString("PL")))

  def cacheMapData(movementType: String, refType: String = "DUCR") =
    Map(
      Choice.choiceId -> Json.toJson(Choice(movementType)),
      ConsignmentReferences.formId -> Json.toJson(consignmentReferences(refType)),
      MovementDetails.formId -> arrivalDepartureTimes(movementType),
      GoodsDeparted.formId -> Json.toJson(goodsDeparted),
      Location.formId -> location,
      Transport.formId -> correctTransport
    )

  def validMovementRequest(movementType: String): InventoryLinkingMovementRequest =
    Movement.createMovementRequest(CacheMap(movementType, cacheMapData(movementType)), "eori1", Choice(movementType))

}
