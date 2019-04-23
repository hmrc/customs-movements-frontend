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

trait CommonMessages {

  val errorPrefix: String = "error"
  val ucrError: String = errorPrefix + ".ducr"
  val errorSummaryTitle: String = errorPrefix + ".summary.title"
  val errorSummaryText: String = errorPrefix + ".summary.text"

  val site: String = "site"
  val backCaption: String = site + ".back"
  val saveAndContinueCaption: String = site + ".save_and_continue"

  val globalError: String = "global.error"
  val globalErrorTitle: String = globalError + ".title"
  val globalErrorHeading: String = globalError + ".heading"
  val globalErrorMessage: String = globalError + ".message"
}
