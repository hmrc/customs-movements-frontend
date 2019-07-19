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

package base

import base.testdata.CommonTestData
import forms.Choice._
import forms.GoodsDeparted.AllowedPlaces
import forms.common.{Date, Time}
import forms._
import models.SignedInUser
import org.joda.time.DateTimeZone.UTC
import org.joda.time.{DateTime, LocalDate}
import play.api.libs.json._
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.auth.core.{Enrolment, Enrolments, User}
import uk.gov.hmrc.wco.dec.inventorylinking.common.{AgentDetails, TransportDetails, UcrBlock}
import uk.gov.hmrc.wco.dec.inventorylinking.movement.request.InventoryLinkingMovementRequest

object ExportsTestData {

  val nrsCredentials =
    Credentials(providerId = "providerId", providerType = "providerType")
  val nrsGroupIdentifierValue = Some("groupIdentifierValue")
  val nrsCredentialRole = Some(User)
  val nrsMdtpInformation = MdtpInformation("deviceId", "sessionId")
  val nrsItmpName =
    ItmpName(Some("givenName"), Some("middleName"), Some("familyName"))
  val nrsItmpAddress = ItmpAddress(
    Some("line1"),
    Some("line2"),
    Some("line3"),
    Some("line4"),
    Some("line5"),
    Some("postCode"),
    Some("countryName"),
    Some("countryCode")
  )
  val nrsAffinityGroup = Some(Individual)
  val nrsCredentialStrength = Some("STRONG")
  val nrsDateOfBirth = Some(LocalDate.now().minusYears(25))

  val currentLoginTime: DateTime = new DateTime(1530442800000L, UTC)
  val previousLoginTime: DateTime = new DateTime(1530464400000L, UTC)
  val nrsTimeStamp: DateTime = new DateTime(1530475200000L, UTC)

  val nrsLoginTimes = LoginTimes(currentLoginTime, Some(previousLoginTime))

  def newUser(eori: String): SignedInUser =
    SignedInUser(eori, Enrolments(Set(Enrolment("HMRC-CUS-ORG").withIdentifier("EORINumber", eori))))

  val wrongJson: JsValue = JsObject(Map("ducr" -> JsString("")))

  val correctDucrJson: JsValue = JsObject(Map("ducr" -> JsString("5GB123456789000-123ABC456DEFIIIII")))

  val wrongMinimumGoodsDate: JsValue = JsObject(
    Map(
      "day" -> JsNumber(0),
      "month" -> JsNumber(0),
      "year" -> JsNumber(LocalDate.now().getYear - 1),
      "hour" -> JsNumber(-1),
      "minute" -> JsNumber(-1)
    )
  )

  val wrongMaximumGoodsDate: JsValue = JsObject(
    Map(
      "day" -> JsNumber(40),
      "month" -> JsNumber(113),
      "year" -> JsNumber(LocalDate.now().getYear),
      "hour" -> JsNumber(25),
      "minute" -> JsNumber(60)
    )
  )

  val goodsDate: JsValue = JsObject(
    Map(
      "day" -> JsNumber(15),
      "month" -> JsNumber(4),
      "year" -> JsNumber(LocalDate.now().getYear),
      "hour" -> JsNumber(16),
      "minute" -> JsNumber(30)
    )
  )

  val emptyLocation: JsValue = JsObject(Map("" -> JsString("")))

  val location: JsValue = JsObject(
    Map(
      "agentLocation" -> JsString("Agent location"),
      "agentRole" -> JsString("Agent role"),
      "goodsLocation" -> JsString("Goods location"),
      "shed" -> JsString("Shed")
    )
  )

  val incorrectTransport: JsValue = JsObject(
    Map(
      "transportId" -> JsString("Transport Id"),
      "transportMode" -> JsString("Transport mode"),
      "transportNationality" -> JsString("Transport nationality")
    )
  )

  val correctTransport: JsValue = JsObject(Map("modeOfTransport" -> JsString("M"), "nationality" -> JsString("PL")))

  def consignmentReferences(refType: String) = ConsignmentReferences(refType, CommonTestData.ucr)
  val date = Date(Some(10), Some(8), Some(2018))
  val departureDetails = DepartureDetails(date)

  def arrivalDepartureTimes(movementType: String) =
    if (movementType == "EAL") Json.toJson(ArrivalDetails(date, Some(Time(Some("13"), Some("34")))))
    else
      Json.toJson(DepartureDetails(date))

  val goodsDeparted = GoodsDeparted(AllowedPlaces.outOfTheUk)

  def cacheMapData(movementType: String, refType: String = "DUCR") =
    Map(
      Choice.choiceId -> Json.toJson(Choice(movementType)),
      ConsignmentReferences.formId -> Json.toJson(consignmentReferences(refType)),
      MovementDetails.formId -> arrivalDepartureTimes(movementType),
      GoodsDeparted.formId -> Json.toJson(goodsDeparted),
      Location.formId -> location,
      Transport.formId -> correctTransport
    )

  def validMovementRequest(movementType: String) =
    InventoryLinkingMovementRequest(
      messageCode = movementType,
      agentDetails =
        Some(AgentDetails(eori = Some("QWERTY123"), agentLocation = Some("Location"), agentRole = Some("ABC"))),
      ucrBlock = UcrBlock(ucr = "GB/NLA-0YH06GF0V3CUPJC9393", ucrType = "D"),
      goodsLocation = "Location",
      goodsArrivalDateTime = Some("2018-11-21T17:47:02"),
      goodsDepartureDateTime = Some("2018-11-21T17:47:02"),
      shedOPID = Some("ABC"),
      masterUCR = Some("GB/NLA-0YH06GF0V3CUPJC9393"),
      masterOpt = Some("A"),
      movementReference = Some("Movement Reference"),
      transportDetails = Some(
        TransportDetails(
          transportID = Some("Transport ID"),
          transportMode = Some("M"),
          transportNationality = Some("UK")
        )
      )
    )

  val choiceForm = Json.toJson(Choice("EAL"))

}
