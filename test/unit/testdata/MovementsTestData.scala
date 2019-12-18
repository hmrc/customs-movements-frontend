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

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate, LocalTime, ZoneId}

import forms.Transport.ModesOfTransport
import forms._
import forms.common.{Date, Time}
import models.cache.{ArrivalAnswers, DepartureAnswers}
import models.requests.{MovementDetailsRequest, MovementRequest, MovementType}
import models.submissions.{ActionType, Submission}
import models.{SignedInUser, UcrBlock}
import testdata.CommonTestData._
import uk.gov.hmrc.auth.core.{Enrolment, Enrolments}

object MovementsTestData {

  private val zoneId: ZoneId = ZoneId.of("Europe/London")
  val movementDetails = new MovementDetails(zoneId)

  def newUser(eori: String): SignedInUser =
    SignedInUser(eori, Enrolments(Set(Enrolment("HMRC-CUS-ORG").withIdentifier("EORINumber", eori))))

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

  def validArrivalAnswers =
    ArrivalAnswers(
      consignmentReferences = Some(ConsignmentReferences(reference = "D", referenceValue = correctUcr)),
      arrivalReference = Some(ArrivalReference(Some("arrivalReference"))),
      arrivalDetails = Some(ArrivalDetails(Date(LocalDate.now().minusDays(1)), Time(LocalTime.of(1, 1)))),
      location = Some(Location("GBAUEMAEMAEMA"))
    )

  def validDepartureAnswers =
    DepartureAnswers(
      consignmentReferences = Some(ConsignmentReferences(reference = "D", referenceValue = correctUcr)),
      departureDetails = Some(DepartureDetails(Date(LocalDate.of(2019, 1, 1)), Time(LocalTime.of(0, 0)))),
      location = Some(Location("GBAUEMAEMAEMA")),
      transport = Some(Transport(modeOfTransport = ModesOfTransport.Sea, nationality = "GB", transportId = "transportID"))
    )

  private val dateTimeFormatter = DateTimeFormatter.ISO_INSTANT

  def validArrivalMovementRequest = MovementRequest(
    eori = validEori,
    choice = MovementType.Arrival,
    consignmentReference = ConsignmentReferences(reference = "D", referenceValue = correctUcr),
    location = Some(Location("GBAUEMAEMAEMA")),
    arrivalReference = Some(ArrivalReference(Some("arrivalReference"))),
    movementDetails = MovementDetailsRequest(
      dateTimeFormatter.format(ArrivalDetails(Date(LocalDate.now().minusDays(1)), Time(LocalTime.of(1, 1))).goodsArrivalMoment(zoneId))
    )
  )

  def validDepartureMovementRequest = MovementRequest(
    eori = validEori,
    choice = MovementType.Departure,
    consignmentReference = ConsignmentReferences(reference = "D", referenceValue = correctUcr),
    location = Some(Location("GBAUEMAEMAEMA")),
    movementDetails = MovementDetailsRequest(
      dateTimeFormatter.format(DepartureDetails(Date(LocalDate.of(2019, 1, 1)), Time(LocalTime.of(0, 0))).goodsDepartureMoment(zoneId))
    ),
    transport = Some(Transport(modeOfTransport = ModesOfTransport.Sea, nationality = "GB", transportId = "transportID"))
  )

}
