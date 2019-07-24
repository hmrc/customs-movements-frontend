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

package controllers.consolidations

import base.MovementBaseSpec
import forms.Choice
import forms.Choice.AllowedChoiceValues
import play.api.test.Helpers._

class DisassociateDucrConfirmationControllerSpec extends MovementBaseSpec {

  private val uri = uriWithContextPath("/disassociate-ducr-confirmation")

  trait SetUp {
    authorizedUser()
    withCaching(Choice.choiceId, Some(Choice(AllowedChoiceValues.DisassociateDUCR)))
  }

  "Disassociate Ducr Confirmation Controller" should {

    "return 200 for get request" when {

      "cache contains data" in new SetUp {
        val result = route(app, getRequest(uri)).get

        status(result) must be(OK)
      }
    }
  }
}
