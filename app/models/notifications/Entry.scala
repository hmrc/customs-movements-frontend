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

package models.notifications

import models.UcrBlock
import play.api.libs.json.Json

final case class Entry(ucrBlock: Option[UcrBlock] = None, goodsItem: Seq[GoodsItem] = Seq.empty, entryStatus: Option[EntryStatus] = None) {
  def ucrType: Option[String] = ucrBlock.map(_.ucrType)
  def ics: Option[String] = entryStatus.flatMap(_.ics)
  def roe: Option[String] = entryStatus.flatMap(_.roe)
  def soe: Option[String] = entryStatus.flatMap(_.soe)
}

object Entry {
  implicit val format = Json.format[Entry]
}
