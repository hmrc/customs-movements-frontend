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

package helpers

import org.scalatest.matchers.{BeMatcher, MatchResult}
import play.api.data.{Form, FormError}

trait FormMatchers {
  val withoutErrors: BeMatcher[Form[_]] = new BeMatcher[Form[_]] {
    def fieldErrors(errors: Seq[FormError]): String =
      errors
        .groupBy(_.key)
        .map {
          case (key, keyError) => s"$key => ${keyError.map(_.message).mkString("[", " ,", "]")}"
        }
        .mkString("\n")

    override def apply(left: Form[_]): MatchResult = {
      val errors = left.errors
      MatchResult(errors.isEmpty, s"Form has errors. Errors ${fieldErrors(errors)}", "Form does not have errors")
    }
  }
}
