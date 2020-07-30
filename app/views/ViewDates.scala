/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

import javax.inject.Singleton

@Singleton
class ViewDates() {
  def timezone = ZoneId.of("Europe/London")

  private val dateAtTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM uuu 'at' h:mma").withZone(timezone)
  private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM uuu").withZone(timezone)
  private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mma").withZone(timezone)

  def formatDateAtTime(temporal: TemporalAccessor): String =
    formatAmPm(
      dateAtTimeFormatter
        .format(temporal)
    )

  def formatDate(temporal: TemporalAccessor): String = dateFormatter.format(temporal)
  def formatTime(temporal: TemporalAccessor): String = formatAmPm(timeFormatter.format(temporal))

  private def formatAmPm(value: String) =
    value
      .replace("AM", "am")
      .replace("PM", "pm")
}
