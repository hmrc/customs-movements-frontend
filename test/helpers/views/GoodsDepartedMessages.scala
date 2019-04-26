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

trait GoodsDepartedMessages {

  val goodsDeparted = "goodsDeparted"
  val departedPlace = goodsDeparted + ".departedPlace"

  val goodsDepartedTitle = goodsDeparted + ".title"
  val goodsDepartedQuestion = departedPlace + ".question"
  val goodsDepartedHint = departedPlace + ".hint"
  val goodsDepartedOutOfTheUk = departedPlace + ".outOfTheUk"
  val goodsDepartedBackIntoTheUk = departedPlace + ".backIntoTheUk"
  val goodsDepartedEmpty = departedPlace + ".empty"
  val goodsDepartedError = departedPlace + ".error"
}
