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
import controllers.summary.routes.{DisassociateUcrSummaryController, MovementConfirmationController}
import forms.{DisassociateUcr, UcrType}
import models.cache.DisassociateUcrAnswers
import play.api.test.Helpers._

class DissociateUcrISpec extends IntegrationSpec {

  "Disassociate UCR Summary Page" when {
    "GET" should {
      "return 200" in {
        // Given
        givenAuthSuccess("eori")
        givenCacheFor("eori", DisassociateUcrAnswers(ucr = Some(DisassociateUcr(kind = UcrType.Mucr, mucr = Some("GB/321-54321"), ducr = None))))

        // When
        val response = get(DisassociateUcrSummaryController.displayPage)

        // Then
        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        // Given
        givenAuthSuccess("eori")
        givenCacheFor("eori", DisassociateUcrAnswers(ucr = Some(DisassociateUcr(kind = UcrType.Mucr, mucr = Some("GB/321-54321"), ducr = None))))
        givenMovementsBackendAcceptsTheConsolidation()

        // When
        val response = post(DisassociateUcrSummaryController.submit)

        // Then
        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(MovementConfirmationController.displayPage.url)
        theAnswersFor("eori") mustBe None
        verify(
          postRequestedForConsolidation()
            .withRequestBody(equalToJson("""{"eori":"eori","ucr":"GB/321-54321","consolidationType":"MucrDisassociation"}"""))
        )
        verifyEventually(
          postRequestedForAudit()
            .withRequestBody(matchingJsonPath("auditType", equalTo("disassociate")))
            .withRequestBody(matchingJsonPath("detail.eori", equalTo("eori")))
            .withRequestBody(matchingJsonPath("detail.ucr", equalTo("GB/321-54321")))
            .withRequestBody(matchingJsonPath("detail.submissionResult", equalTo("Success")))
        )
      }
    }
  }
}
