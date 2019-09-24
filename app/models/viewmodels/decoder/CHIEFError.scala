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

import com.github.tototoshi.csv.CSVReader
import play.api.Logger

import scala.io.Source

case class CHIEFError(code: String, description: String)

object CHIEFError {

  private val logger = Logger(this.getClass)

  def apply(code: String): Option[CHIEFError] = chiefErrors.find(_.code == code)

  def apply(list: List[String]): CHIEFError = list match {
    case code :: description :: Nil => CHIEFError(code, description)
    case error =>
      logger.warn("Incorrect list with errors. Error: " + error)
      throw new IllegalArgumentException("Error has incorrect structure")
  }

  private val chiefErrors: List[CHIEFError] = {
    val source = Source.fromURL(getClass.getClassLoader.getResource("code_lists/chief_errors.csv"), "UTF-8")

    val reader = CSVReader.open(source)

    val errors: List[List[String]] = reader.all()

    errors.map(CHIEFError(_))
  }
}
