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

import java.text.DecimalFormat
import java.time.LocalTime

import play.api.data.Forms.{optional, text}
import play.api.data.{Forms, Mapping}
import play.api.libs.json.{Json, OFormat}

import scala.util.Try

case class Time(time: LocalTime) {

  override def toString: String = time.toString

}

object Time {
  implicit val format: OFormat[Time] = Json.format[Time]

  val hourKey = "hour"
  val minuteKey = "minute"

  val mapping: Mapping[Time] = {
    def build(hour: Try[Int], minutes: Try[Int]): Try[LocalTime] = {
      for {
        h <- hour
        m <- minutes
        time <- Try(LocalTime.of(h, m))
      } yield time
    }

    def bind(hour: Try[Int], minutes: Try[Int]): Time =
      build(hour, minutes)
        .map(apply)
        .getOrElse(throw new IllegalArgumentException("Could not build time - missing one of parameters"))


    def unbind(time: Time): (Try[Int], Try[Int]) =
      (Try(time.time.getHour), Try(time.time.getMinute))

    val twoDigitFormatter: Mapping[Try[Int]] = {
      val formatter = new DecimalFormat("00")
      text().transform(value => Try(value.toInt), _.map(value => formatter.format(value)).getOrElse(""))
    }

    Forms.tuple(
        hourKey -> twoDigitFormatter,
        minuteKey -> twoDigitFormatter
    ).verifying("time.error.invalid", (build _).tupled.andThen(_.isSuccess)).transform((bind _).tupled, unbind)
  }
}
