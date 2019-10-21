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

import forms.Mapping.requiredRadio
import play.api.data.{Form, Forms, Mapping}
import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue}
import utils.validators.forms.FieldValidator.isContainedIn

sealed abstract class Choice(val value: String) {

  def isArrival: Boolean = value == Choice.Arrival.value
  def isDeparture: Boolean = value == Choice.Departure.value
}

object Choice {

  def unapply(status: Choice): Option[String] = Some(status.value)

  def apply(input: String): Choice =
    allChoices.find(_.value == input).getOrElse(throw new IllegalArgumentException("Incorrect choice"))

  implicit object ChoiceValueFormat extends Format[Choice] {
    def reads(status: JsValue): JsResult[Choice] = status match {
      case JsString(choice) =>
        allChoices.find(_.value == choice).map(JsSuccess(_)).getOrElse(JsError("Incorrect choice"))
      case _ => JsError("Incorrect choice")
    }

    def writes(choice: Choice): JsValue = JsString(choice.value)
  }

  case object Arrival extends Choice("arrival")
  case object Departure extends Choice("departure")
  case object AssociateDUCR extends Choice("associateDUCR")
  case object DisassociateDUCR extends Choice("disassociateDUCR")
  case object ShutMUCR extends Choice("shutMUCR")
  case object Submissions extends Choice("submissions")

  val allChoices = Seq(Arrival, Departure, AssociateDUCR, DisassociateDUCR, ShutMUCR, Submissions)

  val choiceId = "Choice"

  val choiceMapping: Mapping[Choice] =
    Forms.mapping(
      "choice" -> requiredRadio("choicePage.input.error.empty")
        .verifying("choicePage.input.error.incorrectValue", isContainedIn(allChoices.map(_.value)))
    )(Choice.apply)(Choice.unapply)

  def form(): Form[Choice] = Form(choiceMapping)
}
