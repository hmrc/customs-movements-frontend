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

/** CHIEF errors mapping based on Status DE-code document.
  * Details can be found in Exports Notifications Behaviour sheet.
  *
  * @param code the code value
  * @param messageKey description for this error code
  */
case class CHIEFError(override val code: String, override val messageKey: String) extends CodeWithMessageKey

object CHIEFError {

  private val logger = Logger(this.getClass)
  private val sourcePath = "code_lists/chief_errors.csv"

  def apply(list: List[String]): CHIEFError = list match {
    case code :: messageKey :: Nil => CHIEFError(code, messageKey)
    case error =>
      logger.warn(s"Record in ILE errors config file [$sourcePath] is incorrect: " + error)
      throw new IllegalArgumentException("Errors file has incorrect structure")
  }

  val allErrors: List[CHIEFError] = {
    val source = Source.fromURL(getClass.getClassLoader.getResource(sourcePath), "UTF-8")
    val reader = CSVReader.open(source)

    reader.all().map(CHIEFError(_))
  }
}
