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

package models.cache

import java.time.{Instant, ZoneOffset}

import forms.DucrPartChiefChoice
import models.UcrBlock
import play.api.libs.json._

case class Cache(
  eori: String,
  answers: Option[Answers],
  queryUcr: Option[UcrBlock],
  ducrPartChiefChoice: Option[DucrPartChiefChoice],
  updated: Option[Instant] = Some(Instant.now())
) {

  def update(answers: Answers): Cache = this.copy(answers = Some(answers), updated = Some(Instant.now()))

  def isDucrPartChief: Boolean = ducrPartChiefChoice.exists(_.isDucrPart)
}

object Cache {
  implicit private val formatInstant: OFormat[Instant] = new OFormat[Instant] {
    override def writes(datetime: Instant): JsObject =
      Json.obj("$date" -> datetime.toEpochMilli)

    override def reads(json: JsValue): JsResult[Instant] =
      json match {
        case JsObject(map) if map.contains("$date") =>
          map("$date") match {
            case JsNumber(v) => JsSuccess(Instant.ofEpochMilli(v.toLong).atOffset(ZoneOffset.UTC).toInstant)
            case _           => JsError("Unexpected Date Format. Expected a Number (Epoch Milliseconds)")
          }
        case _ => JsError("Unexpected Date Format. Expected an object containing a $date field.")
      }
  }
  implicit val format: OFormat[Cache] = Json.format[Cache]

  def apply(eori: String, queryUcr: UcrBlock): Cache = new Cache(eori, None, Some(queryUcr), None)
}
