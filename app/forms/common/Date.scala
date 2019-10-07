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

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import play.api.data.Forms.{number, optional}
import play.api.data.{Forms, Mapping}
import play.api.libs.json.{Json, OFormat}

import scala.util.Try

case class Date(day: Option[Int], month: Option[Int], year: Option[Int]) {

  private val format102 = DateTimeFormatter.ofPattern("yyyyMMdd")

  private val inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def to102Format: String = LocalDate.parse(this.toString).format(format102)

  def to304Format: String = {
    import java.time.{LocalDate, LocalTime}
    val dateTime = LocalDate.parse(this.toString, inputFormat)

    dateTime.atTime(LocalTime.of(0, 0, 0)).format(DateTimeFormatter.ISO_DATE_TIME)

  }

  override def toString: String = LocalDate.of(year.getOrElse(0), month.getOrElse(0), day.getOrElse(0)).toString
  def toLocalDateTimeString: String =
    LocalDate.of(year.getOrElse(0), month.getOrElse(0), day.getOrElse(0)).atStartOfDay().toString + ":00"
}

object Date {
  implicit val format: OFormat[Date] = Json.format[Date]

  val yearKey = "year"
  val monthKey = "month"
  val dayKey = "day"

  private val correctDay: Int => Boolean = (day: Int) => day >= 1 && day <= 31
  private val correctMonth: Int => Boolean = (month: Int) => month >= 1 && month <= 12
  private val correctYear: Int => Boolean = (year: Int) => year >= 2000 && year <= 2099

  private val isDateFormatValid: Date => Boolean = date => Try(LocalDate.parse(date.toString)).isSuccess

  val mapping: Mapping[Date] = Forms
    .mapping(
      dayKey -> optional(number().verifying("dateTime.date.day.error", correctDay))
        .verifying("dateTime.date.day.empty", _.nonEmpty),
      monthKey -> optional(number().verifying("dateTime.date.month.error", correctMonth))
        .verifying("dateTime.date.month.empty", _.nonEmpty),
      yearKey -> optional(number().verifying("dateTime.date.year.error", correctYear))
        .verifying("dateTime.date.year.empty", _.nonEmpty)
    )(Date.apply)(Date.unapply)
    .verifying("dateTime.date.error.format", isDateFormatValid)
}
