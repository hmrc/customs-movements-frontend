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

package models.viewmodels.ilequery

import base.UnitSpec
import models.notifications.queries.Transport
import models.viewmodels.decoder.Decoder

class IleQueryCodeConverterSpec extends UnitSpec {

  val converter = new IleQueryCodeConverter(new Decoder)

  "IleQueryCodeConverter" should {

    "convert transport with identifier and nationality" in {
      converter
        .transport(Transport(None, Some("FR"), Some("ID1234")))
        .asHtml
        .toString() mustBe "ID1234, France, Including Monaco, the French overseas departments (French Guiana, Guadeloupe, Martinique and Reunion) and the French northern part of St Martin"
    }

    "convert transport with only nationality" in {
      converter.transport(Transport(None, Some("PL"), None)).asHtml.toString() mustBe "Poland"
    }

    "convert transport with only identifier" in {
      converter.transport(Transport(None, None, Some("Atlantic Conveyor"))).asHtml.toString() mustBe "Atlantic Conveyor"
    }
  }

}
