/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.mvc.{AnyContent, Request}

sealed trait FormAction {
  def label: String = this.getClass.getSimpleName.replace("$", "")
}

case object Continue extends FormAction
case object SaveAndReturnToSummary extends FormAction

object FormAction {

  private val saveAndReturnToSummaryLabel = "SaveAndReturnToSummary"
  private val continueLabel = "Continue"

  def bindFromRequest()(implicit request: Request[AnyContent]): Option[FormAction] =
    request.body.asFormUrlEncoded.flatMap { body =>
      body.flatMap {
        case (`continueLabel`, _)               => Some(Continue)
        case (`saveAndReturnToSummaryLabel`, _) => Some(SaveAndReturnToSummary)
        case _                                  => None
      }.headOption
    }
}
