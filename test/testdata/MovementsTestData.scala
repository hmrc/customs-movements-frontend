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

package testdata

import java.time.{Instant, LocalDate, LocalTime, ZoneId}

import forms.Choice.Arrival
import forms._
import forms.common.{Date, Time}
import models.submissions.{ActionType, Submission}
import models.{SignedInUser, UcrBlock}
import play.api.libs.json._
import testdata.CommonTestData._
import uk.gov.hmrc.auth.core.{Enrolment, Enrolments}

object MovementsTestData {

  val incorrectTransport: JsValue = JsObject(
    Map(
      "transportId" -> JsString("Transport Id"),
      "transportMode" -> JsString("Transport mode"),
      "transportNationality" -> JsString("Transport nationality")
    )
  )
  val date = Date(LocalDate.of(2018, 8, 10))
  val departureDetails = DepartureDetails(date, Time(LocalTime.now()))
  val location: JsValue = Json.toJson(Location("PLAYcorrect"))
  val correctTransport: JsValue = JsObject(Map("modeOfTransport" -> JsString("2"), "nationality" -> JsString("PL"), "transportId" -> JsString("REF")))

  private val zoneId: ZoneId = ZoneId.of("Europe/London")

  val movementDetails = new MovementDetails(zoneId)

  def newUser(eori: String): SignedInUser =
    SignedInUser(eori, Enrolments(Set(Enrolment("HMRC-CUS-ORG").withIdentifier("EORINumber", eori))))

  def cacheMapData(movementType: Choice, refType: String = "DUCR"): Map[String, JsValue] =
    Map(
      Choice.choiceId -> Json.toJson(movementType),
      ConsignmentReferences.formId -> Json.toJson(consignmentReferences(refType)),
      MovementDetails.formId -> arrivalDepartureTimes(movementType),
      Location.formId -> location,
      Transport.formId -> correctTransport,
      ArrivalReference.formId -> Json.toJson(arrivalReference(movementType))
    )

  def consignmentReferences(refType: String) = ConsignmentReferences(refType, CommonTestData.correctUcr)

  def arrivalDepartureTimes(movementType: Choice): JsValue = movementType match {
    case Arrival => Json.toJson(ArrivalDetails(date, Time(LocalTime.of(13, 34))))
    case _       => Json.toJson(DepartureDetails(date, Time(LocalTime.now())))
  }

  def arrivalReference(movementType: Choice): ArrivalReference =
    ArrivalReference(if (movementType == Arrival) Some("1234") else None)

  def exampleSubmission(
    eori: String = validEori,
    conversationId: String = conversationId,
    ucr: String = correctUcr,
    ucrType: String = "D",
    actionType: ActionType = ActionType.Arrival,
    requestTimestamp: Instant = Instant.now()
  ): Submission =
    Submission(
      eori = eori,
      conversationId = conversationId,
      ucrBlocks = Seq(UcrBlock(ucr = ucr, ucrType = ucrType)),
      actionType = actionType,
      requestTimestamp = requestTimestamp
    )

}
