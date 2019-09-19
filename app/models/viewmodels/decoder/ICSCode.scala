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

package models.viewmodels.decoder

sealed abstract class ICSCode(override val code: String, override val contentKey: String) extends CodeWithContentKey

object ICSCode {

  val codes: Set[ICSCode] = Set(InvalidationAtTraderRequest, InvalidationByCustoms)

  case object InvalidationAtTraderRequest
      extends ICSCode(code = "3", contentKey = "decoder.icsCode.InvalidationAtTraderRequest")
  case object InvalidationByCustoms extends ICSCode(code = "6", contentKey = "decoder.icsCode.InvalidationByCustoms")

}
