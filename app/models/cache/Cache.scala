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

package models.cache

import forms.{DucrPartChiefChoice, UcrType}
import models.{now, UcrBlock}
import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

case class Cache(
  eori: String,
  answers: Option[Answers],
  ucrBlock: Option[UcrBlock],
  ucrBlockFromIleQuery: Boolean,
  ducrPartChiefChoice: Option[DucrPartChiefChoice],
  updated: Option[Instant] = Some(now)
) {

  def is(ucrType: UcrType): Boolean = ucrBlock.exists(_.is(ucrType))

  def isDucrPartChief: Boolean = ducrPartChiefChoice.exists(_.isDucrPart)

  def update(answers: Answers): Cache = this.copy(answers = Some(answers), updated = Some(now))
}

object Cache {
  implicit private val formatInstant: Format[Instant] = MongoJavatimeFormats.instantFormat
  implicit val format: OFormat[Cache] = Json.format[Cache]

  def apply(eori: String): Cache =
    new Cache(eori, None, None, false, None)

  def apply(eori: String, ucrBlock: UcrBlock, ucrBlockFromIleQuery: Boolean): Cache =
    new Cache(eori, None, Some(ucrBlock), ucrBlockFromIleQuery, None)

  def apply(eori: String, answers: Answers, ucrBlock: UcrBlock, ucrBlockFromIleQuery: Boolean): Cache =
    new Cache(eori, Some(answers), Some(ucrBlock), ucrBlockFromIleQuery, None)

  def apply(eori: String, answers: Answers): Cache =
    new Cache(eori, Some(answers), None, false, None)
}
