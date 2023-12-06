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

import play.api.Logging
import play.api.i18n.Messages

import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.time.{Instant, LocalDate, LocalDateTime, Month, OffsetDateTime, ZoneId, ZonedDateTime}
import scala.util.{Failure, Success, Try}

object ViewDates extends Logging {

  def formatDate(temporal: TemporalAccessor)(implicit messages: Messages): String =
    Try(translateMonthOnWelsh(temporal, dateFormatter.format(temporal))) match {
      case Success(result) => result
      case Failure(exc) =>
        val className = temporal.getClass.getSimpleName
        logger.warn(s"Cannot extract date from a [$className] instance")
        throw exc
    }

  def formatDateAtTime(temporal: TemporalAccessor)(implicit messages: Messages): String = {
    val result =
      Try(translateAmPmOnWelsh(dateTimeFormatter.format(temporal))) match {
        case Success(result) => result
        case Failure(exc) =>
          val className = temporal.getClass.getSimpleName
          logger.warn(s"Cannot extract date and time from a [$className] instance")
          throw exc
      }

    translateMonthOnWelsh(temporal, result)
  }

  def formatTime(temporal: TemporalAccessor)(implicit messages: Messages): String =
    Try(translateAmPmOnWelsh(timeFormatter.format(temporal))) match {
      case Success(result) => result
      case Failure(exc) =>
        val className = temporal.getClass.getSimpleName
        logger.warn(s"Cannot extract the time from a [$className] instance")
        throw exc
    }

  private def translateAmPmOnWelsh(result: String)(implicit messages: Messages): String =
    if (messages.lang.code.toLowerCase != "cy")
      result
        .replace("AM", "am")
        .replace("PM", "pm")
    else
      result
        .replaceFirst("(?i)am", "yb")
        .replaceFirst("(?i)pm", "yh")
        .replace(" at ", " am ")

  private def translateMonthOnWelsh(temporal: TemporalAccessor, result: String)(implicit messages: Messages): String =
    if (messages.lang.code.toLowerCase != "cy") result
    else
      Try(monthsForWelsh(monthValue(temporal) - 1)) match {
        case Success(translation) => result.replace(translation._1, translation._2)
        case Failure(_) =>
          val className = temporal.getClass.getSimpleName
          logger.warn(s"Cannot extract the month from a [$className] instance (while translating to Welsh)")
          result
      }

  private def monthValue(temporal: TemporalAccessor): Int =
    temporal match {
      case instant: Instant               => instant.atZone(zoneId).getMonthValue
      case localDate: LocalDate           => localDate.getMonthValue
      case localDateTime: LocalDateTime   => localDateTime.getMonthValue
      case zonedDateTime: ZonedDateTime   => zonedDateTime.getMonthValue
      case offsetDateTime: OffsetDateTime => offsetDateTime.getMonthValue
      case month: Month                   => month.getValue
      case _                              => -1
    }

  val zoneId = ZoneId.of("Europe/London")

  private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM uuu").withZone(zoneId)
  private val dateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM uuu 'at' h:mma").withZone(zoneId)
  private val timeFormatter = DateTimeFormatter.ofPattern("h:mma").withZone(zoneId)

  private val monthsForWelsh = Array(
    "January" -> "Ionawr",
    "February" -> "Chwefror",
    "March" -> "Mawrth",
    "April" -> "Ebrill",
    "May" -> "Mai",
    "June" -> "Mehefin",
    "July" -> "Gorffennaf",
    "August" -> "Awst",
    "September" -> "Medi",
    "October" -> "Hydref",
    "November" -> "Tachwedd",
    "December" -> "Rhagfyr"
  )
}
