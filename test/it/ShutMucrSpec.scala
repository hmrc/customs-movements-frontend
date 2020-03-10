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

import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, equalToJson, matchingJsonPath, verify}
import controllers.exception.InvalidFeatureStateException
import forms.ShutMucr
import models.cache.ShutMucrAnswers
import play.api.test.Helpers._

class ShutMucrSpec extends IntegrationSpec {

  "Enter MUCR Page" when {
    "GET" should {
      "throw feature disabled" in {
        // Given
        givenAuthSuccess("eori")
        givenCacheFor("eori", ShutMucrAnswers())

        // When
        val response = get(controllers.consolidations.routes.ShutMucrController.displayPage())

        // Then
        intercept[InvalidFeatureStateException] {
          await(response)
        }
      }
    }

    "POST" should {
      "throw feature disabled" in {
        // Given
        givenAuthSuccess("eori")
        givenCacheFor("eori", ShutMucrAnswers())

        // When
        val response = post(controllers.consolidations.routes.ShutMucrController.submitForm(), "mucr" -> "GB/123-12345")

        // Then
        intercept[InvalidFeatureStateException] {
          await(response)
        }
      }
    }
  }

  "Shut MUCR Summary Page" when {
    "GET" should {
      "return 200" in {
        // Given
        givenAuthSuccess("eori")
        givenCacheFor("eori", ShutMucrAnswers(shutMucr = Some(ShutMucr("GB/123-12345"))))

        // When
        val response = get(controllers.consolidations.routes.ShutMucrSummaryController.displayPage())

        // Then
        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        // Given
        givenAuthSuccess("eori")
        givenCacheFor("eori", ShutMucrAnswers(shutMucr = Some(ShutMucr("GB/123-12345"))))
        givenMovementsBackendAcceptsTheConsolidation()

        // When
        val response = post(controllers.consolidations.routes.ShutMucrSummaryController.submit())

        // Then
        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.consolidations.routes.ShutMucrConfirmationController.displayPage().url)
        theCacheFor("eori") mustBe None
        verify(
          postRequestedForConsolidation()
            .withRequestBody(equalToJson("""{"eori":"eori","mucr":"GB/123-12345","consolidationType":"ShutMucr"}"""))
        )
        verifyEventually(
          postRequestedForAudit()
            .withRequestBody(matchingJsonPath("auditType", equalTo("shut-mucr")))
            .withRequestBody(matchingJsonPath("detail.eori", equalTo("eori")))
            .withRequestBody(matchingJsonPath("detail.mucr", equalTo("GB/123-12345")))
            .withRequestBody(matchingJsonPath("detail.submissionResult", equalTo("Success")))
        )
      }
    }
  }
}
