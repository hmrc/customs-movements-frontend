/*
 * Copyright 2024 HM Revenue & Customs
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

package models.notifications.queries

import java.time.Instant
import models.notifications.EntryStatus
import play.api.libs.json.{Json, OFormat}

sealed abstract class UcrInfo {
  val ucr: String
  val parentMucr: Option[String]
  val entryStatus: Option[EntryStatus]
  val movements: Seq[MovementInfo]

  lazy val transport: Option[Transport] =
    movements
      .filter(_.transportDetails.isDefined)
      .sortBy(_.movementDateTime)(Ordering.Option[Instant].reverse)
      .headOption
      .flatMap(_.transportDetails)
}

case class MucrInfo(
  ucr: String,
  parentMucr: Option[String] = None,
  entryStatus: Option[EntryStatus] = None,
  isShut: Option[Boolean] = None,
  movements: Seq[MovementInfo] = Seq.empty
) extends UcrInfo

object MucrInfo {
  implicit val format: OFormat[MucrInfo] = Json.format[MucrInfo]
}

case class DucrInfo(
  ucr: String,
  parentMucr: Option[String] = None,
  declarationId: String,
  entryStatus: Option[EntryStatus] = None,
  movements: Seq[MovementInfo] = Seq.empty,
  goodsItem: Seq[GoodsItemInfo] = Seq.empty
) extends UcrInfo

object DucrInfo {
  implicit val format: OFormat[DucrInfo] = Json.format[DucrInfo]
}
