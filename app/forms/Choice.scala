/*
 * Copyright 2024 HM Revenue & Customs
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

import forms.EnhancedMapping.requiredRadio
import models.cache.JourneyType.{ARRIVE, ASSOCIATE_UCR, DEPART, DISSOCIATE_UCR, JourneyType, SHUT_MUCR}
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json._
import utils.validators.forms.FieldValidator.isContainedIn

abstract class Choice(val value: String) {

  lazy val isArrival: Boolean = value == Choice.Arrival.value
  lazy val isDeparture: Boolean = value == Choice.Departure.value
  lazy val isFindConsignment: Boolean = value == Choice.FindConsignment.value
  lazy val isShutMUCR: Boolean = value == Choice.ShutMUCR.value
}

object Choice {

  def apply(input: String): Choice =
    allChoices.find(_.value == input).getOrElse(throw new IllegalArgumentException(s"Incorrect choice [$input]"))

  def apply(`type`: JourneyType): Choice =
    (`type`: @unchecked) match {
      case ARRIVE         => Arrival
      case DEPART         => Departure
      case ASSOCIATE_UCR  => AssociateUCR
      case DISSOCIATE_UCR => DisassociateUCR
      case SHUT_MUCR      => ShutMUCR
    }

  def unapply(status: Choice): Option[String] = Some(status.value)

  implicit object ChoiceValueFormat extends Format[Choice] {
    def reads(status: JsValue): JsResult[Choice] = status match {
      case JsString(choice) =>
        allChoices.find(_.value == choice).map(JsSuccess(_)).getOrElse(JsError(s"Incorrect choice [$choice]"))
      case _ => JsError("Incorrect choice, expected a String")
    }

    def writes(choice: Choice): JsValue = JsString(choice.value)
  }

  case object Arrival extends Choice("arrival")
  case object Departure extends Choice("departure")
  case object AssociateUCR extends Choice("associateUCR")
  case object DisassociateUCR extends Choice("disassociateUCR")
  case object FindConsignment extends Choice("findConsignment")
  case object ShutMUCR extends Choice("shutMUCR")
  case object Submissions extends Choice("submissions")

  val allChoices = List(FindConsignment, Arrival, Departure, AssociateUCR, DisassociateUCR, ShutMUCR, Submissions)

  val consignmentChoices = List(Arrival, Departure, AssociateUCR, DisassociateUCR, ShutMUCR)

  val choiceId = "Choice"

  val choiceMapping: Mapping[Choice] =
    Forms.mapping(
      "choice" -> requiredRadio("choicePage.input.error.empty")
        .verifying("choicePage.input.error.incorrectValue", isContainedIn(allChoices.map(_.value)))
    )(Choice.apply)(Choice.unapply)

  val form: Form[Choice] = Form(choiceMapping)
}
