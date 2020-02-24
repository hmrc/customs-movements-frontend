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

package models.notifications.queries

import models.UcrBlock
import models.notifications.queries.IleQueryResponseExchangeType.{SuccessfulResponseExchange, UcrNotFoundResponseExchange}
import models.viewmodels.decoder.ROECode
import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.play.json.Union

sealed trait IleQueryResponseExchangeData {
  val typ: IleQueryResponseExchangeType
}

object IleQueryResponseExchangeData {

  implicit val format: Format[IleQueryResponseExchangeData] = Union
    .from[IleQueryResponseExchangeData]("typ")
    .and[SuccessfulResponseExchangeData](SuccessfulResponseExchange.toString)
    .and[UcrNotFoundResponseExchangeData](UcrNotFoundResponseExchange.toString)
    .format

  case class SuccessfulResponseExchangeData(
    queriedDucr: Option[DucrInfo] = None,
    queriedMucr: Option[MucrInfo] = None,
    parentMucr: Option[MucrInfo] = None,
    childDucrs: Seq[DucrInfo] = Seq.empty,
    childMucrs: Seq[MucrInfo] = Seq.empty
  ) extends IleQueryResponseExchangeData {
    override val typ = IleQueryResponseExchangeType.SuccessfulResponseExchange

    lazy val sortedChildrenUcrs: Seq[UcrInfo] = (childDucrs ++ childMucrs).sortBy(_.entryStatus.flatMap(_.roe).getOrElse(ROECode.NoneRoe))

    lazy val queriedUcr: UcrInfo = {
      if (queriedDucr.isDefined && queriedMucr.isDefined)
        throw new IllegalStateException("SuccessfulResponseExchangeData contains both queriedDucr and queriedMucr")
      queriedDucr
        .orElse(queriedMucr)
        .getOrElse(throw new IllegalStateException("SuccessfulResponseExchangeData must have either queriedDucr or queriedMucr"))
    }
  }

  object SuccessfulResponseExchangeData {
    implicit val format: OFormat[SuccessfulResponseExchangeData] = Json.format[SuccessfulResponseExchangeData]
  }

  case class UcrNotFoundResponseExchangeData(
    messageCode: String,
    actionCode: String,
    ucrBlock: Option[UcrBlock] = None,
    movementReference: Option[String] = None,
    errorCodes: Seq[String] = Seq.empty
  ) extends IleQueryResponseExchangeData {
    override val typ = IleQueryResponseExchangeType.UcrNotFoundResponseExchange
  }

  object UcrNotFoundResponseExchangeData {
    implicit val format: OFormat[UcrNotFoundResponseExchangeData] = Json.format[UcrNotFoundResponseExchangeData]
  }
}
