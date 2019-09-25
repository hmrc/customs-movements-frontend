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

package helpers.views

trait TransportMessages {

  val transport = "transport"

  val title = transport + ".title"

  val modeOfTransport = transport + ".modeOfTransport"
  val modeOfTransportQuestion = modeOfTransport + ".question"
  val modeOfTransportHint = modeOfTransport + ".hint"
  val modeOfTransportSea = modeOfTransport + ".1"
  val modeOfTransportRail = modeOfTransport + ".2"
  val modeOfTransportRoad = modeOfTransport + ".3"
  val modeOfTransportAir = modeOfTransport + ".4"
  val modeOfTransportPostalOrMail = modeOfTransport + ".5"
  val modeOfTransportFixed = modeOfTransport + ".6"
  val modeOfTransportInland = modeOfTransport + ".7"
  val modeOfTransportOther = modeOfTransport + ".8"

  val modeOfTransportEmpty = modeOfTransport + ".empty"
  val modeOfTransportError = modeOfTransport + ".error"

  val nationality = transport + ".nationality"

  val nationalityQuestion = nationality + ".question"
  val nationalityHint = nationality + ".hint"

  val nationalityEmpty = nationality + ".empty"
  val nationalityError = nationality + ".error"
}
