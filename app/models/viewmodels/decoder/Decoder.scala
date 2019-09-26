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

import javax.inject.Singleton

@Singleton
class Decoder {

  def crc(code: String): Option[CRCCode] = CRCCode.codes.find(_.code == code)

  def ics(code: String): Option[ICSCode] = ICSCode.codes.find(_.code == code)

  def roe(code: String): Option[ROECode] = ROECode.codes.find(_.code == code)

  def ducrSoe(code: String): Option[SOECode] = SOECode.DucrCodes.find(_.code == code)

  def mucrSoe(code: String): Option[SOECode] = SOECode.MucrCodes.find(_.code == code)

  def actionCode(code: String): Option[ActionCode] = ActionCode.codes.find(_.code == code)

  def error(code: String): Option[CodeWithMessageKey] = {
    val chiefErrorOpt = CHIEFError.allErrors.find(_.code == code)

    if (chiefErrorOpt.isDefined) chiefErrorOpt
    else ILEError.allErrors.find(_.code == code)
  }
}
