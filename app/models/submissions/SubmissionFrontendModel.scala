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

package models.submissions

import java.time.Instant
import java.util.UUID

import models.UcrBlock
import play.api.libs.json._

case class SubmissionFrontendModel(
  uuid: String = UUID.randomUUID().toString,
  eori: String,
  conversationId: String,
  ucrBlocks: Seq[UcrBlock],
  actionType: ActionType,
  requestTimestamp: Instant = Instant.now()
) {

  def hasMucr: Boolean = ucrBlocks.exists(_.ucrType == "M")

  def extractMucr: Option[String] = ucrBlocks.find(_.ucrType == "M").map(_.ucr)

  def extractFirstUcr: Option[String] = ucrBlocks.headOption.map(_.ucr)
}

object SubmissionFrontendModel {
  implicit val formats = Json.format[SubmissionFrontendModel]
}
