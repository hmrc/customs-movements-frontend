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

package forms.common

import java.time.LocalTime

import play.api.data.{Forms, Mapping}
import play.api.data.Forms.{optional, text}
import play.api.libs.json.{Json, OFormat}

import scala.util.Try

case class Time(hour: Option[String], minute: Option[String]) {

  override def toString: String =
    LocalTime.of(hour.map(_.toInt).getOrElse(0), minute.map(_.toInt).getOrElse(0)).toString

  def formatTime(): Time = {
    import java.text.DecimalFormat

    val formatter = new DecimalFormat("00")

    val formattedHour = hour.map(value => formatter.format(value.toInt))
    val formattedMinute = minute.map(value => formatter.format(value.toInt))

    Time(formattedHour, formattedMinute)
  }
}

object Time {
  implicit val format: OFormat[Time] = Json.format[Time]

  val hourKey = "hour"
  val minuteKey = "minute"

  private val correctHour: String => Boolean = (hour: String) => Try(hour.toInt).map(value => value >= 0 && value <= 23).getOrElse(false)

  private val correctMinute: String => Boolean = (minute: String) => Try(minute.toInt).map(value => value >= 0 && value <= 59).getOrElse(false)

  val mapping: Mapping[Time] = Forms
    .mapping(
      hourKey -> optional(text().verifying("dateTime.time.hour.error", correctHour))
        .verifying("dateTime.time.hour.empty", _.nonEmpty),
      minuteKey -> optional(text().verifying("dateTime.time.minute.error", correctMinute))
        .verifying("dateTime.time.minute.empty", _.nonEmpty)
    )(Time.apply)(Time.unapply)
}
