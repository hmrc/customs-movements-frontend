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
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator.isContainedIn

case class Choice(value: String)

object Choice {
  implicit val format = Json.format[Choice]

  val choiceId = "Choice"

  import AllowedChoiceValues._
  private val correctChoices = Set(Arrival, Departure, AssociateDUCR, DisassociateDUCR, ShutMucr, Submissions)

  val choiceMapping: Mapping[Choice] = Forms.mapping(
    "choice" -> requiredRadio("choicePage.input.error.empty")
      .verifying("choicePage.input.error.incorrectValue", isContainedIn(correctChoices))
  )(Choice.apply)(Choice.unapply)

  def form(): Form[Choice] = Form(choiceMapping)

  object AllowedChoiceValues {
    val Arrival = "EAL"
    val Departure = "EDL"
    val AssociateDUCR = "Associate"
    val DisassociateDUCR = "EAC"
    val ShutMucr = "CST"
    val Submissions = "SUB"
  }
}
