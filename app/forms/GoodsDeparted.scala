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
import forms.Transport.ModesOfTransport._
import play.api.data.{Form, Forms}
import play.api.libs.json.Json
import utils.validators.forms.FieldValidator._

case class GoodsDeparted(departedPlace: String)

object GoodsDeparted {
  implicit val format = Json.format[GoodsDeparted]

  val formId = "GoodsDeparted"

  object AllowedPlaces {
    val outOfTheUk = "outOfTheUk"
    val backIntoTheUk = "backIntoTheUk"
  }

  import AllowedPlaces._

  val allowedPlaces: Seq[String] = Seq(outOfTheUk, backIntoTheUk)

  val mapping = Forms.mapping(
    "departedPlace" -> requiredRadio("goodsDeparted.departedPlace.empty")
      .verifying("goodsDeparted.departedPlace.error", isContainedIn(allowedPlaces))
  )(GoodsDeparted.apply)(GoodsDeparted.unapply)

  def form(): Form[GoodsDeparted] = Form(mapping)

  def messageKey(place: String) =
    s"transport.modeOfTransport.$place"
}
