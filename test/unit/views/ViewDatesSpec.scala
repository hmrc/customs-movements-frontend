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

import base.UnitSpec
import play.api.i18n.Lang
import play.api.test.Helpers.{stubLangs, stubMessagesApi}
import views.ViewDates.zoneId

import java.time.{Instant, LocalDate, LocalDateTime, LocalTime, ZonedDateTime}
import java.util.Locale

class ViewDatesSpec extends UnitSpec {

  "ViewDates" when {

    "the Locale is English" should {
      implicit val messages = stubMessagesApi().preferred(List(Lang(Locale.ENGLISH)))

      val instant = Instant.parse("2023-08-31T23:55:00Z")

      "format an Instant correctly" in {
        ViewDates.formatDate(instant) mustBe "1 September 2023"
        ViewDates.formatDateAtTime(instant) mustBe "1 September 2023 at 12:55am"
        ViewDates.formatTime(instant) mustBe "12:55am"
      }

      "format a LocalDate correctly" in {
        val localDate = LocalDate.parse("2023-08-31")
        ViewDates.formatDate(localDate) mustBe "31 August 2023"
      }

      "format a LocalDateTime correctly" in {
        val localDateTime = LocalDateTime.parse("2023-08-31T23:55:00")
        ViewDates.formatDate(localDateTime) mustBe "31 August 2023"
        ViewDates.formatDateAtTime(localDateTime) mustBe "31 August 2023 at 11:55pm"
        ViewDates.formatTime(localDateTime) mustBe "11:55pm"
      }

      "format a LocalTime correctly" in {
        val localTime = LocalTime.ofInstant(instant, zoneId)
        ViewDates.formatTime(localTime) mustBe "12:55am"
      }

      "format a ZonedDateTime correctly" in {
        val zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId)
        ViewDates.formatDate(zonedDateTime) mustBe "1 September 2023"
        ViewDates.formatDateAtTime(zonedDateTime) mustBe "1 September 2023 at 12:55am"
        ViewDates.formatTime(zonedDateTime) mustBe "12:55am"
      }
    }

    "the Locale is Welsh" should {
      implicit val messages = stubMessagesApi(langs = stubLangs(List(Lang("cy")))).preferred(List(Lang("cy")))

      val months =
        Array("", "Ionawr", "Chwefror", "Mawrth", "Ebrill", "Mai", "Mehefin", "Gorffennaf", "Awst", "Medi", "Hydref", "Tachwedd", "Rhagfyr")

      "format an Instant correctly" in {
        for (ix <- 1 to 12) {
          val month = months(ix)

          val instant = Instant.parse(f"2023-$ix%02d-01T11:00:00Z")
          val localDate = LocalDate.parse(f"2023-$ix%02d-01")
          val localDateTime = LocalDateTime.parse(f"2023-$ix%02d-01T11:00:00")
          val localTime = localDateTime.toLocalTime
          val zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId)

          ViewDates.formatDate(instant) mustBe s"1 $month 2023"
          ViewDates.formatDate(localDate) mustBe s"1 $month 2023"
          ViewDates.formatDate(localDateTime) mustBe s"1 $month 2023"
          ViewDates.formatDate(zonedDateTime) mustBe s"1 $month 2023"

          val expectedTimeInLondon = if ((4 to 10).contains(ix)) "12:00yh" else "11:00yb"

          ViewDates.formatDateAtTime(instant) mustBe s"1 $month 2023 am $expectedTimeInLondon"
          ViewDates.formatDateAtTime(localDateTime) mustBe s"1 $month 2023 am 11:00yb"
          ViewDates.formatDateAtTime(zonedDateTime) mustBe s"1 $month 2023 am $expectedTimeInLondon"

          ViewDates.formatTime(instant) mustBe expectedTimeInLondon
          ViewDates.formatTime(localDateTime) mustBe "11:00yb"
          ViewDates.formatTime(localTime) mustBe "11:00yb"
          ViewDates.formatTime(zonedDateTime) mustBe expectedTimeInLondon
        }
      }
    }
  }
}
