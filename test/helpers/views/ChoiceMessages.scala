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

package helpers.views

trait ChoiceMessages {
  def movementChoice = "movement.choice"
  // TODO: description is used as title
  def title = movementChoice + ".description"
  def supplementaryDec = "declaration.choice.SMP"
  def standardDec = "declaration.choice.STD"
  def arrival = movementChoice + ".EAL"
  def departure = movementChoice + ".EDL"
  def associate = movementChoice + ".ASS"
  def disassociate = movementChoice + ".EAC"
  def cancelDec = "declaration.choice.CAN"
  def recentDec = "declaration.choice.SUB"
  def choiceEmpty = "choicePage.input.error.empty"
  def choiceError = "choicePage.input.error.incorrectValue"
}
