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
import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, equalToJson, matchingJsonPath, verify}
import controllers.summary.routes.{MovementConfirmationController, ShutMucrSummaryController}
import forms.ShutMucr
import models.cache.ShutMucrAnswers
import play.api.test.Helpers._

class ShutMucrISpec extends IntegrationSpec {

  "Shut MUCR Summary Page" when {
    "GET" should {
      "return 200" in {
        // Given
        givenAuthSuccess("eori")
        givenCacheFor("eori", ShutMucrAnswers(shutMucr = Some(ShutMucr("GB/123-12345"))))

        // When
        val response = get(ShutMucrSummaryController.displayPage)

        // Then
        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        // Given
        givenAuthSuccess("eori")
        givenCacheFor("eori", ShutMucrAnswers(shutMucr = Some(ShutMucr("GB/82F9-0N2F6500040010TO120P0A30998"))))
        givenMovementsBackendAcceptsTheConsolidation()

        // When
        val response = post(ShutMucrSummaryController.submit)

        // Then
        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(MovementConfirmationController.displayPage.url)
        theCacheFor("eori") mustBe None
        verify(
          postRequestedForConsolidation()
            .withRequestBody(equalToJson("""{"eori":"eori","mucr":"GB/82F9-0N2F6500040010TO120P0A30998","consolidationType":"ShutMucr"}"""))
        )
        verifyEventually(
          postRequestedForAudit()
            .withRequestBody(matchingJsonPath("auditType", equalTo("shut-mucr")))
            .withRequestBody(matchingJsonPath("detail.eori", equalTo("eori")))
            .withRequestBody(matchingJsonPath("detail.mucr", equalTo("GB/82F9-0N2F6500040010TO120P0A30998")))
            .withRequestBody(matchingJsonPath("detail.submissionResult", equalTo("Success")))
        )
      }
    }
  }
}
