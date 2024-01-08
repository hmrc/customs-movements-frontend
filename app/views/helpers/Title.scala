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

package views.helpers

import play.api.i18n.Messages

case class Title(headingKey: String, sectionKey: Option[String] = None, headingArgs: Seq[String] = Seq.empty, hasErrors: Boolean = false) {
  def format(implicit messages: Messages): String = {
    val heading = messages(headingKey, headingArgs: _*)
    val service = messages("service.name")
    val withErrors = if (hasErrors) ".withErrors" else ""

    sectionKey match {
      case Some(section) => messages(s"title${withErrors}.withSection.format", heading, messages(section), service)
      case _             => messages(s"title${withErrors}.format", heading, service)
    }
  }
}
