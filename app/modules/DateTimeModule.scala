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

package modules

import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.time.{Clock, ZoneId}

import com.google.inject.AbstractModule
import javax.inject.{Inject, Provider, Singleton}
import modules.DateTimeModule.timezone
import views.ViewDates

class DateTimeModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ZoneId]).toInstance(timezone)
    bind(classOf[Clock]).toInstance(Clock.system(timezone))
  }
}

object DateTimeModule {
  lazy val timezone: ZoneId = ZoneId.of("Europe/London")
}
