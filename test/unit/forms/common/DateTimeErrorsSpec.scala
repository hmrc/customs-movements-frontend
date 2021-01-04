/*
 * Copyright 2021 HM Revenue & Customs
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

import base.BaseSpec
import play.api.data.{Form, FormError}

class DateTimeErrorsSpec extends BaseSpec {

  val form: Form[Date] = Form(Date.mapping)

  "DateTimeError" should {

    "remove duplicates" when {

      "there are two errors with the same message" in {
        val error1 = FormError("key1", "message")
        val error2 = FormError("key2", "message")

        DateTimeErrors.processErrors(Seq(error1, error2), "any", "other") mustBe (Seq(FormError("key1", "message")))
      }
    }

    "transform an error" when {

      "there is a general 'date' error" in {
        val error = FormError("dateKey", "message")

        DateTimeErrors.processErrors(Seq(error), "dateKey", "timeKey") mustBe (Seq(FormError("dateKey.day", "message")))
      }
      "there is a general 'time' error" in {
        val error = FormError("timeKey", "message")

        DateTimeErrors.processErrors(Seq(error), "dateKey", "timeKey") mustBe (Seq(FormError("timeKey.hour", "message")))
      }
    }

    "not transform an error" when {

      "there is a specific 'date' error" in {
        val error = FormError("dateKey.month", "message")

        DateTimeErrors.processErrors(Seq(error), "dateKey", "timeKey") mustBe (Seq(error))
      }
      "there is a specific 'time' error" in {
        val error = FormError("timeKey.minute", "message")

        DateTimeErrors.processErrors(Seq(error), "dateKey", "timeKey") mustBe (Seq(error))
      }
    }

  }

}
