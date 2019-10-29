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

import java.time.{ZoneId, ZonedDateTime}

import forms.common.{Date, Time}
import play.api.data.{Forms, Mapping}
import play.api.libs.json.{Json, OFormat}

case class ArrivalDetails(dateOfArrival: Date, timeOfArrival: Time) {
  def goodsArrivalMoment(zoneId: ZoneId): ZonedDateTime =
    ZonedDateTime.of(dateOfArrival.date, timeOfArrival.time, zoneId)
}

object ArrivalDetails {
  implicit val format: OFormat[ArrivalDetails] = Json.format[ArrivalDetails]

  def mapping(zoneId: ZoneId): Mapping[ArrivalDetails] =
    Forms
      .mapping("dateOfArrival" -> Date.mapping, "timeOfArrival" -> Time.mapping)(ArrivalDetails.apply)(ArrivalDetails.unapply)
      .verifying("arrival.details.error.overdue", _.goodsArrivalMoment(zoneId).isAfter(ZonedDateTime.now(zoneId).minusDays(60)))
      .verifying("arrival.details.error.future", _.goodsArrivalMoment(zoneId).isBefore(ZonedDateTime.now(zoneId)))
}
