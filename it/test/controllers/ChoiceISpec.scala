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

package controllers

import base.IntegrationSpec
import controllers.routes.ChoiceController
import forms.Choice
import forms.UcrType.Mucr
import models.UcrBlock
import models.cache._
import play.api.test.Helpers._

class ChoiceISpec extends IntegrationSpec {

  "Display Page" should {

    "return 200 when ucrBlock in cache" in {
      givenAuthSuccess("eori")
      givenCacheFor(Cache("eori", UcrBlock(ucr = "ucr", ucrType = Mucr), false))

      val response = get(ChoiceController.displayChoices)

      status(response) mustBe OK
    }

    "return 200 when no ucrBlock in cache" in {
      givenAuthSuccess("eori")

      val response = get(ChoiceController.displayChoices)

      status(response) mustBe OK
    }
  }

  "Submit" should {
    "continue journey" when {
      "Departure" in {
        givenAuthSuccess("eori")

        val response = post(ChoiceController.submitChoice, "choice" -> Choice.Departure.value)

        status(response) mustBe SEE_OTHER
        theAnswersFor("eori") mustBe Some(DepartureAnswers())
      }

      "Arrival" in {
        givenAuthSuccess("eori")

        val response = post(ChoiceController.submitChoice, "choice" -> Choice.Arrival.value)

        status(response) mustBe SEE_OTHER
        theAnswersFor("eori") mustBe Some(ArrivalAnswers())
      }

      "Associate UCR" in {
        givenAuthSuccess("eori")

        val response = post(ChoiceController.submitChoice, "choice" -> Choice.AssociateUCR.value)

        status(response) mustBe SEE_OTHER
        theAnswersFor("eori") mustBe Some(AssociateUcrAnswers())
      }

      "Disassociate UCR" in {
        givenAuthSuccess("eori")

        val response = post(ChoiceController.submitChoice, "choice" -> Choice.DisassociateUCR.value)

        status(response) mustBe SEE_OTHER
        theAnswersFor("eori") mustBe Some(DisassociateUcrAnswers())
      }

      "Shut MUCR" in {
        givenAuthSuccess("eori")

        val response = post(ChoiceController.submitChoice, "choice" -> Choice.ShutMUCR.value)

        status(response) mustBe SEE_OTHER
        theAnswersFor("eori") mustBe Some(ShutMucrAnswers())
      }
    }
  }
}
