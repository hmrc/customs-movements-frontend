/*
 * Copyright 2022 HM Revenue & Customs
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

package forms.common

import play.api.data.FormError

object DateTimeErrors {

  def processErrors(errors: Seq[FormError], dateKey: String, timeKey: String): Seq[FormError] = {
    val removeDuplicate = errors match {
      case e1 :: e2 :: _ if e1.messages == e2.messages => Seq(e1)
      case _                                           => errors
    }
    removeDuplicate.map(err =>
      err.copy(key = err.key match {
        case `dateKey` => s"$dateKey.day"
        case `timeKey` => s"$timeKey.hour"
        case _         => err.key
      })
    )
  }
}
