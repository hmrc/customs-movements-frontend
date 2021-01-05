/*
 * Copyright 2021 HM Revenue & Customs
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import play.api.data.Forms._
import play.api.data.{Forms, Mapping}
import play.api.libs.json.{Json, OFormat}
import utils.validators.forms.FieldValidator._

import scala.util.Try

case class Date(date: LocalDate) {

  private val format102 = DateTimeFormatter.ofPattern("yyyyMMdd")

  def to102Format: String = date.format(format102)

  def to304Format: String = {
    import java.time.LocalTime
    date.atTime(LocalTime.of(0, 0, 0)).format(DateTimeFormatter.ISO_DATE_TIME)

  }

  override def toString: String = date.toString

  def asLocalDate: LocalDate = date
}

object Date {
  implicit val format: OFormat[Date] = Json.format[Date]

  val yearKey = "year"
  val monthKey = "month"
  val dayKey = "day"

  val mapping: Mapping[Date] = {

    val twoDigitFormatter = new DecimalFormat("00")
    val fourDigitFormatter = new DecimalFormat("0000")

    def build(day: Try[Int], month: Try[Int], year: Try[Int]): Try[LocalDate] =
      for {
        d <- day
        m <- month
        y <- year
      } yield LocalDate.of(y, m, d)

    def validate(day: Try[Int], month: Try[Int], year: Try[Int]): Boolean = build(day, month, year).isSuccess

    def bind(day: Try[Int], month: Try[Int], year: Try[Int]): Date =
      build(day, month, year)
        .map(apply)
        .getOrElse(throw new IllegalArgumentException("Could not bind local date when any is empty"))

    def unbind(date: Date): (Try[Int], Try[Int], Try[Int]) = {
      val value = date.date
      (Try(value.getDayOfMonth), Try(value.getMonthValue), Try(value.getYear))
    }

    val dayMapping: Mapping[Try[Int]] = {
      text()
        .verifying("date.day.error", isInRange(1, 31))
        .transform[Try[Int]](value => Try(value.toInt), _.map(value => twoDigitFormatter.format(value)).getOrElse(""))
    }

    val monthMapping: Mapping[Try[Int]] = {
      text()
        .verifying("date.month.error", isInRange(1, 12))
        .transform[Try[Int]](value => Try(value.toInt), _.map(value => twoDigitFormatter.format(value)).getOrElse(""))
    }

    val yearMapping: Mapping[Try[Int]] = {
      text()
        .verifying("date.year.error", isInRange(2000, 3000))
        .transform[Try[Int]](value => Try(value.toInt), _.map(value => fourDigitFormatter.format(value)).getOrElse(""))
    }

    Forms
      .tuple(dayKey -> dayMapping, monthKey -> monthMapping, yearKey -> yearMapping)
      .verifying("date.error.invalid", (validate _).tupled)
      .transform((bind _).tupled, unbind)
  }
}
