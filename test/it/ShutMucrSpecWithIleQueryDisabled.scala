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

import forms.ShutMucr
import models.cache.ShutMucrAnswers
import play.api.Configuration
import play.api.test.Helpers._

class ShutMucrSpecWithIleQueryDisabled extends IntegrationSpec {

  override def ileQueryFeatureConfiguration: Configuration =
    Configuration.from(Map("microservice.services.features.ileQuery" -> "disabled"))

  "Enter MUCR Page" when {
    "GET" should {
      "return 200" in {
        // Given
        givenAuthSuccess("eori")
        givenCacheFor("eori", ShutMucrAnswers())

        // When
        val response = get(controllers.consolidations.routes.ShutMucrController.displayPage())

        // Then
        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        // Given
        givenAuthSuccess("eori")
        givenCacheFor("eori", ShutMucrAnswers())

        // When
        val response = post(controllers.consolidations.routes.ShutMucrController.submitForm(), "mucr" -> "GB/123-12345")

        // Then
        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.consolidations.routes.ShutMucrSummaryController.displayPage().url)
        theAnswersFor("eori") mustBe Some(ShutMucrAnswers(shutMucr = Some(ShutMucr("GB/123-12345"))))
      }
    }
  }

}
