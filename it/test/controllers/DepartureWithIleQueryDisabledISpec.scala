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
import forms._
import models.cache.DepartureAnswers
import play.api.Configuration
import play.api.test.Helpers._

class DepartureWithIleQueryDisabledISpec extends IntegrationSpec {

  override def ileQueryFeatureConfiguration: Configuration =
    Configuration.from(Map("microservice.services.features.ileQuery" -> "disabled"))

  "Consignment References Page" when {
    "GET" should {
      "return 200" in {
        // Given
        givenAuthSuccess("eori")
        givenCacheFor("eori", DepartureAnswers())

        // When
        val response = get(controllers.routes.ConsignmentReferencesController.displayPage)

        // TThen
        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        // Given
        givenAuthSuccess("eori")
        givenCacheFor("eori", DepartureAnswers())

        // When
        val response =
          post(controllers.routes.ConsignmentReferencesController.saveConsignmentReferences, "reference" -> "M", "mucrValue" -> "GB/123-12345")

        // Then
        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.routes.SpecificDateTimeController.displayPage.url)
        theAnswersFor("eori") mustBe Some(DepartureAnswers(consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345"))))
      }
    }
  }

}
