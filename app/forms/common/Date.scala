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

  val twoDigitFormatter = new DecimalFormat("00")
  val fourDigitFormatter = new DecimalFormat("0000")

  def validate(day: Try[Int], month: Try[Int], year: Try[Int]): Boolean = build(day, month, year).isSuccess
  def validateNonEmptyFields(day: String, month: String, year: String): Boolean = Seq(day, month, year).forall(_.nonEmpty)
  def isAnyFieldNotEmpty(fields: Seq[String]): Condition = mapping => fields.exists(field => mapping.getOrElse(field, "").nonEmpty)

  def mapping(prefix: String): Mapping[Date] =
    Forms
      .tuple(dayKey -> dayMapping(prefix), monthKey -> monthMapping(prefix), yearKey -> yearMapping(prefix))
      .verifying("date.error.allEmpty", date => validateNonEmptyFields(date._1, date._2, date._3))
      .transform[(Try[Int], Try[Int], Try[Int])](
        stringTuple => (Try(stringTuple._1.toInt), Try(stringTuple._2.toInt), Try(stringTuple._3.toInt)),
        tryTuple =>
          (
            tryTuple._1.map(int => twoDigitFormatter.format(int)).getOrElse(""),
            tryTuple._2.map(int => twoDigitFormatter.format(int)).getOrElse(""),
            tryTuple._3.map(int => fourDigitFormatter.format(int)).getOrElse("")
        )
      )
      .verifying("date.error.invalid", date => validate(date._1, date._2, date._3))
      .transform((form2model _).tupled, model2form)

  private def dayMapping(prefix: String): Mapping[String] = AdditionalConstraintsMapping(
    text()
      .verifying("date.day.error", isEmptyOr(isInRange(1, 31))),
    Seq(ConditionalConstraint(isAnyFieldNotEmpty(Seq(prefix + monthKey, prefix + yearKey)), "date.day.missing", nonEmpty))
  )

  private def monthMapping(prefix: String): Mapping[String] = AdditionalConstraintsMapping(
    text()
      .verifying("date.month.error", isEmptyOr(isInRange(1, 12))),
    Seq(ConditionalConstraint(isAnyFieldNotEmpty(Seq(prefix + dayKey, prefix + yearKey)), "date.month.missing", nonEmpty))
  )

  private def yearMapping(prefix: String): Mapping[String] = AdditionalConstraintsMapping(
    text()
      .verifying("date.year.error", isEmptyOr(isInRange(2000, 3000))),
    Seq(ConditionalConstraint(isAnyFieldNotEmpty(Seq(prefix + monthKey, prefix + dayKey)), "date.year.missing", nonEmpty))
  )

  private def form2model(day: Try[Int], month: Try[Int], year: Try[Int]): Date =
    build(day, month, year)
      .map(apply)
      .getOrElse(throw new IllegalArgumentException("Could not bind local date when any is empty"))

  private def model2form(date: Date): (Try[Int], Try[Int], Try[Int]) = {
    val value = date.date
    (Try(value.getDayOfMonth), Try(value.getMonthValue), Try(value.getYear))
  }

  private def build(day: Try[Int], month: Try[Int], year: Try[Int]): Try[LocalDate] =
    for {
      d <- day
      m <- month
      y <- year
    } yield LocalDate.of(y, m, d)
}
