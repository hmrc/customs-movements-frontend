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
import services.Countries.allCountries
import utils.validators.forms.FieldValidator._

case class Location(locationType: String, qualifierCode: String, locationCode: String, country: String) {

  def asString: String = country + locationType + qualifierCode + locationCode
}

object Location {
  implicit val format = Json.format[Location]

  val formId = "Location"

  val correctLocationType: Set[String] = Set("A", "B", "C", "D")

  val correctQualifierCode: Set[String] = Set("U", "Y")

  val mapping = Forms.mapping(
    "locationType" -> requiredRadio("locationType.empty")
      .verifying("locationType.error", isContainedIn(correctLocationType)),
    "qualifierCode" -> requiredRadio("qualifierCode.empty")
      .verifying("qualifierCode.error", isContainedIn(correctQualifierCode)),
    "locationCode" -> text()
      .verifying("locationCode.error", isEmpty or isAlphanumeric and noShorterThan(6) and noLongerThan(13)),
    "country" -> text()
      .verifying("location.country.empty", nonEmpty)
      .verifying("location.country.error", isEmpty or (input => allCountries.exists(_.countryCode == input)))
  )(Location.apply)(Location.unapply)

  def form(): Form[Location] = Form(mapping)

}
