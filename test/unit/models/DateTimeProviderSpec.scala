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

package models

import java.time._

import base.UnitSpec
import forms.common.{Date, Time}

class DateTimeProviderSpec extends UnitSpec {

  val fixedClock = Clock.fixed(LocalDateTime.of(2020, 6, 18, 21, 53, 59, 999).toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
  val provider = new DateTimeProvider(fixedClock)

  "DateTimeProvider" should {
    "provide correct current date" in {
      provider.dateNow must be(Date(LocalDate.of(2020, 6, 18)))
    }
    "provide correct time to nearest completed minute" in {
      provider.timeNow must be(Time(LocalTime.of(21, 53, 0, 0)))
    }
  }
}
