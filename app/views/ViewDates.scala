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

package views

import play.api.i18n.Messages

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import javax.inject.Singleton

@Singleton
class ViewDates() {
  def timezone = ZoneId.of("Europe/London")

  private val atTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(" 'at' h:mma").withZone(timezone)
  private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mma").withZone(timezone)
  private val dayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d").withZone(timezone)
  private val monthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M").withZone(timezone)
  private val yearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("uuu").withZone(timezone)

  def formatDateAtTime(temporal: TemporalAccessor)(implicit messages: Messages): String =
    formatAmPm(
      dayFormatter.format(temporal)
        + s" ${messages(s"month.${monthFormatter.format(temporal)}")} "
        + yearFormatter.format(temporal)
        + atTimeFormatter.format(temporal)
    )

  def formatDate(temporal: TemporalAccessor)(implicit messages: Messages): String =
    dayFormatter.format(temporal) + s" ${messages(s"month.${monthFormatter.format(temporal)}")} " + yearFormatter.format(temporal)

  def formatTime(temporal: TemporalAccessor): String = formatAmPm(timeFormatter.format(temporal))

  private def formatAmPm(value: String) =
    value
      .replace("AM", "am")
      .replace("PM", "pm")
}
