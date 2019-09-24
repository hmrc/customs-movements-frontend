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

package forms

import forms.Mapping.requiredRadio
import play.api.data.Forms.text
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class Transport(modeOfTransport: String, nationality: String, transportId: String)

object Transport {
  implicit val format = Json.format[Transport]

  val formId = "Transport"

  object ModesOfTransport {
    val Sea = "1"
    val Rail = "2"
    val Road = "3"
    val Air = "4"
    val PostalOrMail = "5"
    val FixedInstallations = "6"
    val InlandWaterway = "7"
    val Other = "8"
  }

  import ModesOfTransport._

  val allowedModeOfTransport =
    Seq(Sea, Rail, Road, Air, PostalOrMail, FixedInstallations, InlandWaterway, Other)

  val mapping = Forms.mapping(
    "modeOfTransport" -> requiredRadio("transport.modeOfTransport.empty")
      .verifying("transport.modeOfTransport.error", isContainedIn(allowedModeOfTransport)),
    "nationality" -> text()
      .verifying("transport.nationality.empty", nonEmpty)
      .verifying("transport.nationality.error", isEmpty or isValidCountryCode),
    "transportId" -> text()
      .verifying("transport.transportId.empty", nonEmpty)
      .verifying("transport.transportId.error", isEmpty or (noLongerThan(35) and isAlphanumeric))
  )(Transport.apply)(Transport.unapply)

  def form: Form[Transport] =
    Form(mapping)

  def messageKey(mode: String) =
    s"transport.modeOfTransport.${mode match {
      case Sea                => "sea"
      case Rail               => "rail"
      case Road               => "road"
      case Air                => "air"
      case PostalOrMail       => "postalOrMail"
      case FixedInstallations => "fixed"
      case InlandWaterway     => "inlandWaterway"
      case Other              => "other"
      case _                  => "unknown"
    }}"
}
