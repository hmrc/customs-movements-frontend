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

  val ducrError: String = "error.ducr"
  val mucrErrorEmpty: String = "error.mucr.empty"
  val mucrErrorFormat: String = "error.mucr.format"
  val errorSummaryTitle: String = "error.summary.title"
  val errorSummaryText: String = "error.summary.text"

  val backCaption: String = "site.back"
  val saveAndContinueCaption: String = "site.save_and_continue"
  val continueCaption: String = "site.continue"
  val backToStartPageCaption: String = "site.backToStartPage"
  val confirmAndSubmitCaption: String = "site.confirmAndSubmit"
  val changeCaption: String = "site.change"

  val globalErrorTitle: String = "global.error.title"
  val globalErrorHeading: String = "global.error.heading"
  val globalErrorMessage: String = "global.error.message"
}
