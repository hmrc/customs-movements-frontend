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

package models.cache

import forms.UcrType.Mucr
import forms.{AssociateUcr, MucrOptions, _}
import models.UcrBlock
import models.cache.JourneyType._
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.play.json.Union

case class ArrivalAnswers(
  override val consignmentReferences: Option[ConsignmentReferences] = None,
  arrivalDetails: Option[ArrivalDetails] = None,
  override val location: Option[Location] = None,
  override val specificDateTimeChoice: Option[SpecificDateTimeChoice] = None,
  override val readyToSubmit: Option[Boolean] = Some(false)
) extends MovementAnswers {
  override val `type`: JourneyType.Value = ARRIVE
}

object ArrivalAnswers {
  implicit val format: Format[ArrivalAnswers] = Json.format[ArrivalAnswers]

  def fromUcr(ucrBlock: Option[UcrBlock]): ArrivalAnswers =
    new ArrivalAnswers(ucrBlock.map(ConsignmentReferences.apply), None, None)
}
case class DepartureAnswers(
  override val consignmentReferences: Option[ConsignmentReferences] = None,
  departureDetails: Option[DepartureDetails] = None,
  override val location: Option[Location] = None,
  override val specificDateTimeChoice: Option[SpecificDateTimeChoice] = None,
  transport: Option[Transport] = None,
  override val readyToSubmit: Option[Boolean] = Some(false)
) extends MovementAnswers {
  override val `type`: JourneyType.Value = DEPART
}

object DepartureAnswers {
  implicit val format: Format[DepartureAnswers] = Json.format[DepartureAnswers]

  def fromUcr(ucrBlock: Option[UcrBlock]): DepartureAnswers =
    new DepartureAnswers(ucrBlock.map(ConsignmentReferences.apply), None, None, None)
}

trait MovementAnswers extends Answers {
  val location: Option[Location]
  val specificDateTimeChoice: Option[SpecificDateTimeChoice]
}

case class AssociateUcrAnswers(
  manageMucrChoice: Option[ManageMucrChoice] = None,
  mucrOptions: Option[MucrOptions] = None,
  associateUcr: Option[AssociateUcr] = None,
  override val readyToSubmit: Option[Boolean] = Some(false)
) extends Answers {
  override val `type`: JourneyType.Value = ASSOCIATE_UCR
  override val consignmentReferences: Option[ConsignmentReferences] = associateUcr.map(ucr => ConsignmentReferences(ucr.kind, ucr.ucr))

  def isAssociateAnotherMucr: Boolean = manageMucrChoice.exists(_.choice == ManageMucrChoice.AssociateAnotherMucr)
  def isAssociateThisMucr: Boolean = manageMucrChoice.exists(_.choice == ManageMucrChoice.AssociateThisMucr)
}

object AssociateUcrAnswers {
  implicit val format: Format[AssociateUcrAnswers] = Json.format[AssociateUcrAnswers]

  def fromUcr(ucrBlock: Option[UcrBlock]): AssociateUcrAnswers =
    new AssociateUcrAnswers(None, None, ucrBlock.map(AssociateUcr.apply))
}

case class DisassociateUcrAnswers(ucr: Option[DisassociateUcr] = None) extends Answers {
  override val `type`: JourneyType.Value = DISSOCIATE_UCR
  override def consignmentReferences: Option[ConsignmentReferences] = ucr.map(ucr => ConsignmentReferences(ucr.kind, ucr.ucr))
}

object DisassociateUcrAnswers {
  implicit val format: Format[DisassociateUcrAnswers] = Json.format[DisassociateUcrAnswers]

  def fromUcr(ucrBlock: Option[UcrBlock]): DisassociateUcrAnswers =
    new DisassociateUcrAnswers(ucrBlock.map(DisassociateUcr.apply))
}

case class ShutMucrAnswers(shutMucr: Option[ShutMucr] = None) extends Answers {
  override val `type`: JourneyType.Value = SHUT_MUCR
  override val consignmentReferences: Option[ConsignmentReferences] = shutMucr.map(mucr => ConsignmentReferences(Mucr, mucr.mucr))
}

object ShutMucrAnswers {
  implicit val format: Format[ShutMucrAnswers] = Json.format[ShutMucrAnswers]

  def fromUcr(ucrBlock: Option[UcrBlock]): ShutMucrAnswers = {
    val shutMucr = ucrBlock.filter(_.ucrType.equals("M")).map(ucrBlock => ShutMucr(ucrBlock.ucr))
    ShutMucrAnswers(shutMucr)
  }
}

object JourneyNotSelectedAnswers extends Answers {
  override val `type`: JourneyType = JOURNEY_NOT_SELECTED
}

trait Answers {
  val `type`: JourneyType
  val readyToSubmit: Option[Boolean] = None
  def consignmentReferences: Option[ConsignmentReferences] = None
}

object Answers {
  implicit val arrivalAnswers = Json.format[ArrivalAnswers]
  implicit val departureAnswers = Json.format[DepartureAnswers]
  implicit val associateUcrAnswers = Json.format[AssociateUcrAnswers]
  implicit val disassociateUcrAnswers = Json.format[DisassociateUcrAnswers]
  implicit val shutMucrAnswers = Json.format[ShutMucrAnswers]

  implicit val format: Format[Answers] = Union
    .from[Answers]("type")
    .and[ArrivalAnswers](ARRIVE.toString)
    .and[DepartureAnswers](DEPART.toString)
    .and[AssociateUcrAnswers](ASSOCIATE_UCR.toString)
    .and[DisassociateUcrAnswers](DISSOCIATE_UCR.toString)
    .and[ShutMucrAnswers](SHUT_MUCR.toString)
    .format
}
