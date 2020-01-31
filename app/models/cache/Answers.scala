/*
 * Copyright 2020 HM Revenue & Customs
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

package models.cache

import forms.{AssociateUcr, MucrOptions, _}
import models.cache.JourneyType.JourneyType
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.play.json.Union

case class ArrivalAnswers(
  override val consignmentReferences: Option[ConsignmentReferences] = None,
  arrivalDetails: Option[ArrivalDetails] = None,
  override val location: Option[Location] = None
) extends MovementAnswers {
  override val `type`: JourneyType.Value = JourneyType.ARRIVE
}

object DepartureAnswers {
  implicit val format: Format[DepartureAnswers] = Json.format[DepartureAnswers]
}

case class DepartureAnswers(
  override val consignmentReferences: Option[ConsignmentReferences] = None,
  departureDetails: Option[DepartureDetails] = None,
  override val location: Option[Location] = None,
  transport: Option[Transport] = None
) extends MovementAnswers {
  override val `type`: JourneyType.Value = JourneyType.DEPART
}

object ArrivalAnswers {
  implicit val format: Format[ArrivalAnswers] = Json.format[ArrivalAnswers]
}

trait MovementAnswers extends Answers {
  val consignmentReferences: Option[ConsignmentReferences]
  val location: Option[Location]
}

case class AssociateUcrAnswers(mucrOptions: Option[MucrOptions] = None, associateUcr: Option[AssociateUcr] = None) extends Answers {
  override val `type`: JourneyType.Value = JourneyType.ASSOCIATE_UCR
}

object AssociateUcrAnswers {
  implicit val format: Format[AssociateUcrAnswers] = Json.format[AssociateUcrAnswers]
}

case class DisassociateUcrAnswers(ucr: Option[DisassociateUcr] = None) extends Answers {
  override val `type`: JourneyType.Value = JourneyType.DISSOCIATE_UCR
}

object DisassociateUcrAnswers {
  implicit val format: Format[DisassociateUcrAnswers] = Json.format[DisassociateUcrAnswers]
}

case class ShutMucrAnswers(shutMucr: Option[ShutMucr] = None) extends Answers {
  override val `type`: JourneyType.Value = JourneyType.SHUT_MUCR
}

object ShutMucrAnswers {
  implicit val format: Format[ShutMucrAnswers] = Json.format[ShutMucrAnswers]
}

trait Answers {
  val `type`: JourneyType
}

object Answers {
  implicit val format: Format[Answers] = Union
    .from[Answers]("type")
    .and[ArrivalAnswers](JourneyType.ARRIVE.toString)
    .and[DepartureAnswers](JourneyType.DEPART.toString)
    .and[AssociateUcrAnswers](JourneyType.ASSOCIATE_UCR.toString)
    .and[DisassociateUcrAnswers](JourneyType.DISSOCIATE_UCR.toString)
    .and[ShutMucrAnswers](JourneyType.SHUT_MUCR.toString)
    .format
}
