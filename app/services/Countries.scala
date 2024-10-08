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

package services

import play.api.libs.json._

case class Country(countryName: String, countryCode: String) {
  def asString(): String = s"$countryName - $countryCode"
}

case object Country {
  implicit val formats: OFormat[Country] = Json.format[Country]
}

object Countries {

  val allCountries: List[Country] = {
    val jsonFile = getClass.getResourceAsStream("/code_lists/location-autocomplete-canonical-list.json")

    def fromJsonFile: List[Country] =
      Json.parse(jsonFile) match {
        case JsArray(cs) =>
          // Using collection.Seq instead of Seq due to Json.parse return type
          cs.toList.collect { case JsArray(collection.Seq(c: JsString, cc: JsString)) =>
            Country(c.value, countryCode(cc.value))
          }
        case _ =>
          throw new IllegalArgumentException("Could not read JSON array of countries from : " + jsonFile)
      }
    fromJsonFile.sortBy(_.countryName)
  }

  private def countryCode: String => String = cc => cc.split(":")(1).trim

  def countryName(code: String): String = allCountries.find(_.countryCode == code).map(_.countryName).getOrElse(code)
  def country(code: String): Option[Country] = allCountries.find(_.countryCode == code)
}
