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

import forms.common.{Date, Time}
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.{Json, OFormat}

case class DepartureDetails(dateOfDeparture: Date, timeOfDeparture: Time) {

  def moment: LocalDateTime = LocalDateTime.of(dateOfDeparture.asLocalDate, timeOfDeparture.time)

  override def toString: String = dateOfDeparture.to304Format
}

object DepartureDetails {
  implicit val format: OFormat[DepartureDetails] = Json.format[DepartureDetails]

  val mapping = Forms.mapping(
    "dateOfDeparture" -> Date.mapping,
    "timeOfDeparture" -> Time.mapping
  )(DepartureDetails.apply)(DepartureDetails.unapply)
    .verifying("departure.details.error.overdue", _.moment.isAfter(LocalDateTime.now().minusDays(60)))
    .verifying("departure.details.error.future", _.moment.isBefore(LocalDateTime.now()) )
}
