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

import forms.Choice
import models.UcrBlock
import models.cache._
import play.api.test.Helpers._

class ChoiceSpec extends IntegrationSpec {

  "Display Page" should {

    "return 200 when queryUcr in cache" in {
      givenAuthSuccess("eori")
      givenCacheFor(Cache("eori", UcrBlock("ucr", "M")))

      val response = get(controllers.routes.ChoiceController.displayChoiceForm())

      status(response) mustBe OK
    }

    "return 300 when no queryUcr in cache" in {
      givenAuthSuccess("eori")

      val response = get(controllers.routes.ChoiceController.displayChoiceForm())

      status(response) mustBe SEE_OTHER
      redirectLocation(response) mustBe Some(controllers.ileQuery.routes.FindConsignmentController.displayQueryForm().url)
    }
  }

  "Submit" should {
    "continue journey" when {
      "Departure" in {
        givenAuthSuccess("eori")

        val response = post(controllers.routes.ChoiceController.submitChoice(), "choice" -> Choice.Departure.value)

        status(response) mustBe SEE_OTHER
        theAnswersFor("eori") mustBe Some(DepartureAnswers())
      }

      "Arrival" in {
        givenAuthSuccess("eori")

        val response = post(controllers.routes.ChoiceController.submitChoice(), "choice" -> Choice.Arrival.value)

        status(response) mustBe SEE_OTHER
        theAnswersFor("eori") mustBe Some(ArrivalAnswers())
      }

      "Associate UCR" in {
        givenAuthSuccess("eori")

        val response = post(controllers.routes.ChoiceController.submitChoice(), "choice" -> Choice.AssociateUCR.value)

        status(response) mustBe SEE_OTHER
        theAnswersFor("eori") mustBe Some(AssociateUcrAnswers())
      }

      "Dissociate UCR" in {
        givenAuthSuccess("eori")

        val response = post(controllers.routes.ChoiceController.submitChoice(), "choice" -> Choice.DisassociateUCR.value)

        status(response) mustBe SEE_OTHER
        theAnswersFor("eori") mustBe Some(DisassociateUcrAnswers())
      }

      "Shut MUCR" in {
        givenAuthSuccess("eori")

        val response = post(controllers.routes.ChoiceController.submitChoice(), "choice" -> Choice.ShutMUCR.value)

        status(response) mustBe SEE_OTHER
        theAnswersFor("eori") mustBe Some(ShutMucrAnswers())
      }
    }
  }
}
