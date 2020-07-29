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

package forms.common

import java.text.DecimalFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter

import play.api.data.Forms.text
import play.api.data.{Forms, Mapping}
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator._

import scala.util.Try

case class Time(time: LocalTime)

object Time {
  implicit val format: OFormat[Time] = Json.format[Time]

  val hourKey = "hour"
  val minuteKey = "minute"
  val ampmKey = "ampm"

  val time12HourFormatter = DateTimeFormatter.ofPattern("h:mma")

  val am = "AM"
  val pm = "PM"

  val mapping: Mapping[Time] = {
    def build(hour: Try[Int], minutes: Try[Int], ampm: String): Try[LocalTime] =
      for {
        h <- hour
        m <- minutes
      } yield {
        val timeString = s"$h:${f"$m%02d"}$ampm"
        LocalTime.parse(timeString, time12HourFormatter)
      }

    def bind(hour: Try[Int], minutes: Try[Int], ampm: String): Time =
      build(hour, minutes, ampm)
        .map(apply)
        .getOrElse(throw new IllegalArgumentException("Could not build time - missing one of parameters"))

    def unbind(time: Time): (Try[Int], Try[Int], String) = {
      val timeString = time.time.format(time12HourFormatter)
      (Try(timeString.split(":")(0).toInt), Try(time.time.getMinute), timeString.takeRight(2))
    }

    val twoDigitFormatter: Mapping[Try[Int]] = {
      val formatter = new DecimalFormat("00")
      text().transform(value => Try(value.toInt), _.map(value => formatter.format(value)).getOrElse(""))
    }

    val amPmMapping: Mapping[String] = text().verifying("time.ampm.error", isEmpty or isContainedIn(Seq(Time.am, Time.pm)))

    Forms
      .tuple(hourKey -> twoDigitFormatter, minuteKey -> twoDigitFormatter, ampmKey -> amPmMapping)
      .verifying("time.error.invalid", (build _).tupled.andThen(_.isSuccess))
      .transform((bind _).tupled, unbind)
  }
}
