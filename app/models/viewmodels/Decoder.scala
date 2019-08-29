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

package models.viewmodels

import javax.inject.Singleton

@Singleton
class Decoder {

  def crc(code: String): String = crcMappings(code)

  def roe(code: String): String = roeMappings(code)

  def soe(code: String): String = soeMappings(code)

  private val crcMappings: Map[String, String] =
    Map("000" -> "Success", "101" -> "Pre-lodged Declaration has not arrived ", "102" -> "Declaration has not arrived")
      .withDefaultValue("")

  private val roeMappings: Map[String, String] = Map(
    "1" -> "Documentary Control",
    "2" -> "Physical / External Party Control",
    "3" -> "Non-Blocking Documentary Control",
    "6" -> "No control required",
    "0" -> "Risking not yet performed",
    "H" -> "Pre-lodge pre-fix"
  ).withDefaultValue("")

  private val soeMappings: Map[String, String] = Map(
    "1" -> "Declaration Validation",
    "2" -> "Declaration Goods Release",
    "3" -> "Declaration Clearance",
    "4" -> "Declaration Invalidated",
    "5" -> "Declaration Rejected",
    "6" -> "Declaration Handled Externally",
    "7" -> "Declaration Correction Validation",
    "8" -> "Advance Declaration Registration",
    "9" -> "Declaration Acceptance",
    "10" -> "Declaration Acceptance at Goods Arrival",
    "11" -> "Declaration Rejection at Goods Arrival",
    "12" -> "Declaration Corrected",
    "13" -> "Declaration Supplemented",
    "14" -> "Declaration Risked",
    "15" -> "Customs Position Determined",
    "16" -> "Declaration Clearance after Goods Release",
    "17" -> "Insufficient Guarantees",
    "D" -> "Departed",
    "F" -> "Frustrated"
  ).withDefaultValue("")
}
