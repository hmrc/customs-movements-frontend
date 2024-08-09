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

package forms.common

import forms.{AdditionalConstraintsMapping, ConditionalConstraint}
import play.api.data.Forms.text
import play.api.data.{Forms, Mapping}
import play.api.libs.json.{Json, OFormat}
import uk.gov.voa.play.form.Condition
import utils.validators.forms.FieldValidator._

import java.text.DecimalFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.Locale
import scala.util.Try

case class Time(time: LocalTime) {

  def getClockHour: Int = time.get(ChronoField.CLOCK_HOUR_OF_AMPM)
  def getMinute: Int = time.get(ChronoField.MINUTE_OF_HOUR)
  def getAmPm: String = if (time.get(ChronoField.AMPM_OF_DAY) == 0) Time.am else Time.pm
}

// See Date for explanation of mapping and validation
object Time {
  implicit val format: OFormat[Time] = Json.format[Time]

  val hourKey = "hour"
  val minuteKey = "minute"
  val ampmKey = "ampm"
  val formatter = new DecimalFormat("00")

  val am = "AM"
  val pm = "PM"

  private lazy val time12HourFormatter =
    new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("h:mma").toFormatter(Locale.ENGLISH)

  private def isValidTimeOrAnyEmptyFields(hours: String, minutes: String, ampm: String): Boolean =
    if (isAnyFieldEmpty(Seq(hours, minutes, ampm))) true
    else Try(LocalTime.parse(timeString(hours, minutes, ampm), time12HourFormatter)).isSuccess

  private def timeString(hour: String, minutes: String, ampm: String) =
    s"${hour.toInt}:${f"${minutes.toInt}%02d"}$ampm"

  def isAnyFieldPopulated(fields: Seq[String]): Boolean = fields.exists(_.nonEmpty)
  def isAnyFieldEmpty(fields: Seq[String]): Boolean = fields.exists(_.isEmpty)
  def isAnyFieldPopulatedCondition(fields: Seq[String]): Condition = mapping => fields.exists(field => mapping.getOrElse(field, "").nonEmpty)

  def mapping(prefix: String): Mapping[Time] =
    Forms
      .tuple(hourKey -> hourMapping(prefix), minuteKey -> minuteMapping(prefix), ampmKey -> amPmMapping(prefix))
      .verifying("time.error.allEmpty", time => isAnyFieldPopulated(Seq(time._1, time._2, time._3)))
      .verifying("time.error.invalid", time => isValidTimeOrAnyEmptyFields(time._1.trim, time._2.trim, time._3))
      .transform((bind _).tupled, unbind)

  private def hourMapping(prefix: String): Mapping[String] = AdditionalConstraintsMapping(
    text()
      .verifying("time.hour.error", isEmptyOr(isInRange(1, 12))),
    Seq(ConditionalConstraint(isAnyFieldPopulatedCondition(Seq(prefix + minuteKey, prefix + ampmKey)), "time.hour.missing", nonEmpty))
  )

  private def minuteMapping(prefix: String): Mapping[String] = AdditionalConstraintsMapping(
    text()
      .verifying("time.minute.error", isEmptyOr(isInRange(0, 59))),
    Seq(ConditionalConstraint(isAnyFieldPopulatedCondition(Seq(prefix + hourKey, prefix + ampmKey)), "time.minute.missing", nonEmpty))
  )

  private def amPmMapping(prefix: String): Mapping[String] = AdditionalConstraintsMapping(
    text()
      .verifying("time.ampm.error", isEmptyOr(isContainedIn(Seq(Time.am, Time.pm)))),
    Seq(ConditionalConstraint(isAnyFieldPopulatedCondition(Seq(prefix + minuteKey, prefix + hourKey)), "time.ampm.error", nonEmpty))
  )

  private def bind(hour: String, minutes: String, ampm: String): Time =
    Time(LocalTime.parse(timeString(hour.trim, minutes.trim, ampm), time12HourFormatter))

  private def unbind(time: Time): (String, String, String) =
    (time.getClockHour.toString, formatter.format(time.getMinute.toLong), time.getAmPm)
}
