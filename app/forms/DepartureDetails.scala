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

package forms

import java.time.{LocalDateTime, LocalTime}

import forms.common.Date
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.{Json, OFormat}

case class DepartureDetails(dateOfDeparture: Date, timeOfDeparture: LocalTime) {

  def moment: LocalDateTime = LocalDateTime.of(dateOfDeparture.asLocalDate, timeOfDeparture)

  override def toString: String = dateOfDeparture.to304Format
}

object DepartureDetails {
  implicit val format: OFormat[DepartureDetails] = Json.format[DepartureDetails]

  val time = {
    def bind(hours: Int, minutes: Int):  LocalTime = LocalTime.of(hours, minutes)

    def unbind(time: LocalTime): Option[(Int, Int)] = Some((time.getHour, time.getMinute))

    Forms.mapping(
      "hour" -> number(),
      "minute" -> number()
    )(bind)(unbind)
  }

  val mapping = Forms.mapping(
    "dateOfDeparture" -> Date.mapping,
    "timeOfDeparture" -> time
  )(DepartureDetails.apply)(DepartureDetails.unapply)
    .verifying("departure.details.error.overdue", _.moment.isAfter(LocalDateTime.now().minusDays(60)))
    .verifying("departure.details.error.future", _.moment.isBefore(LocalDateTime.now()) )
}
