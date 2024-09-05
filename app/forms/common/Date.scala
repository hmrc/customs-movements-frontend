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
import play.api.data.Forms._
import play.api.data.{Forms, Mapping}
import play.api.libs.json.{Json, OFormat}
import uk.gov.voa.play.form.Condition
import utils.validators.forms.FieldValidator._

import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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

  private lazy val twoDigitFormatter = new DecimalFormat("00")
  private lazy val fourDigitFormatter = new DecimalFormat("0000")

  private def isValidDateOrAnyEmptyFields(day: String, month: String, year: String): Boolean =
    if (isAnyFieldEmpty(Seq(day, month, year))) true
    else Try(LocalDate.of(year.toInt, month.toInt, day.toInt)).isSuccess

  def isAnyFieldPopulated(fields: Seq[String]): Boolean = fields.exists(_.nonEmpty)
  def isAnyFieldEmpty(fields: Seq[String]): Boolean = fields.exists(_.isEmpty)
  def isAnyFieldPopulatedCondition(fields: Seq[String]): Condition = mapping => fields.exists(field => mapping.getOrElse(field, "").nonEmpty)

  def mapping(prefix: String): Mapping[Date] =
    Forms
      .tuple(dayKey -> dayMapping(prefix), monthKey -> monthMapping(prefix), yearKey -> yearMapping(prefix))
      // Fire error if all date fields are empty (isAnyFieldNotEmpty fails)
      // This will never fire if any individual field-level "missing" errors have fired (see dayMapping below)
      .verifying("date.error.allEmpty", date => isAnyFieldPopulated(Seq(date._1, date._2, date._3)))
      // This error only fires if all fields have a value to check it's a valid date
      .verifying("date.error.invalid", date => isValidDateOrAnyEmptyFields(date._1.trim, date._2.trim, date._3.trim))
      .transform((form2model _).tupled, model2form)

  private def dayMapping(prefix: String): Mapping[String] = AdditionalConstraintsMapping(
    text()
      .verifying("date.day.error", isEmptyOr(isInRange(1, 31))),
    // Apply constraint if any field other than this one has a value (this stops field-level errors firing when all fields are empty)
    // Then fire error if this field is empty (nonEmpty constraint fails)
    // If all given fields are empty and constraint is not applied, this field either has a value
    // or will be caught downstream by "allEmpty" validation (see mapping above)
    Seq(ConditionalConstraint(isAnyFieldPopulatedCondition(Seq(prefix + monthKey, prefix + yearKey)), "date.day.missing", nonEmpty))
  )

  private def monthMapping(prefix: String): Mapping[String] = AdditionalConstraintsMapping(
    text()
      .verifying("date.month.error", isEmptyOr(isInRange(1, 12))),
    Seq(ConditionalConstraint(isAnyFieldPopulatedCondition(Seq(prefix + dayKey, prefix + yearKey)), "date.month.missing", nonEmpty))
  )

  private def yearMapping(prefix: String): Mapping[String] = AdditionalConstraintsMapping(
    text()
      .verifying("date.year.error", isEmptyOr(isInRange(2000, 3000))),
    Seq(ConditionalConstraint(isAnyFieldPopulatedCondition(Seq(prefix + monthKey, prefix + dayKey)), "date.year.missing", nonEmpty))
  )

  private def form2model(day: String, month: String, year: String): Date =
    Date(LocalDate.of(year.trim.toInt, month.trim.toInt, day.trim.toInt))

  private def model2form(date: Date): (String, String, String) = {
    val value = date.date
    (
      twoDigitFormatter.format(value.getDayOfMonth.toLong),
      twoDigitFormatter.format(value.getMonthValue.toLong),
      fourDigitFormatter.format(value.getYear.toLong)
    )
  }
}
