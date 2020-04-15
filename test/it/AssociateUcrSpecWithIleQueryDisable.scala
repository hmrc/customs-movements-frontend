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
import forms.{AssociateUcr, MucrOptions, UcrType}
import models.cache.AssociateUcrAnswers
import play.api.Configuration
import play.api.test.Helpers._

class AssociateUcrSpecWithIleQueryDisable extends IntegrationSpec {

  override def ileQueryFeatureConfiguration: Configuration =
    Configuration.from(Map("microservice.services.features.ileQuery" -> "disabled"))

  "UCR Options Page" when {
    "GET" should {
      "return 200" in {
        givenAuthSuccess("eori")
        givenCacheFor("eori", AssociateUcrAnswers())

        val response = get(controllers.consolidations.routes.MucrOptionsController.displayPage())

        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        givenAuthSuccess("eori")
        givenCacheFor("eori", AssociateUcrAnswers())

        val response = post(
          controllers.consolidations.routes.MucrOptionsController.save(),
          "createOrAdd" -> "create",
          "newMucr" -> "GB/123-12345",
          "existingMucr" -> ""
        )

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.consolidations.routes.AssociateUcrController.displayPage().url)
        theAnswersFor("eori") mustBe Some(AssociateUcrAnswers(mucrOptions = Some(MucrOptions(createOrAdd = "create", newMucr = "GB/123-12345"))))
      }
    }
  }

  "Associate UCR Page" when {
    "GET" should {
      "return 200" in {
        givenAuthSuccess("eori")
        givenCacheFor("eori", AssociateUcrAnswers(mucrOptions = Some(MucrOptions(createOrAdd = "create", newMucr = "GB/123-12345"))))

        val response = get(controllers.consolidations.routes.AssociateUcrController.displayPage())

        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        givenAuthSuccess("eori")
        givenCacheFor("eori", AssociateUcrAnswers(mucrOptions = Some(MucrOptions(createOrAdd = "create", newMucr = "GB/123-12345"))))

        val response = post(controllers.consolidations.routes.AssociateUcrController.submit(), "kind" -> "mucr", "mucr" -> "GB/321-54321")

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.consolidations.routes.AssociateUcrSummaryController.displayPage().url)
        theAnswersFor("eori") mustBe Some(
          AssociateUcrAnswers(
            mucrOptions = Some(MucrOptions(createOrAdd = "create", newMucr = "GB/123-12345")),
            associateUcr = Some(AssociateUcr(UcrType.Mucr, "GB/321-54321"))
          )
        )
      }
    }
  }

  "Associate UCR Summary Page" when {
    "GET" should {
      "return 200" in {
        givenAuthSuccess("eori")
        givenCacheFor(
          "eori",
          AssociateUcrAnswers(
            mucrOptions = Some(MucrOptions(createOrAdd = "create", newMucr = "GB/123-12345")),
            associateUcr = Some(AssociateUcr(UcrType.Mucr, "GB/321-54321"))
          )
        )

        val response = get(controllers.consolidations.routes.AssociateUcrSummaryController.displayPage())

        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        givenAuthSuccess("eori")
        givenCacheFor(
          "eori",
          AssociateUcrAnswers(
            mucrOptions = Some(MucrOptions(createOrAdd = "create", newMucr = "GB/123-12345")),
            associateUcr = Some(AssociateUcr(UcrType.Mucr, "GB/321-54321"))
          )
        )
        givenMovementsBackendAcceptsTheConsolidation()

        val response = post(controllers.consolidations.routes.AssociateUcrSummaryController.submit())

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.consolidations.routes.AssociateUcrConfirmationController.displayPage().url)
        theAnswersFor("eori") mustBe None
        verify(
          postRequestedForConsolidation()
            .withRequestBody(equalToJson("""{"eori":"eori","mucr":"GB/123-12345","ucr":"GB/321-54321","consolidationType":"MucrAssociation"}"""))
        )
        verifyEventually(
          postRequestedForAudit()
            .withRequestBody(matchingJsonPath("auditType", equalTo("associate")))
            .withRequestBody(matchingJsonPath("detail.eori", equalTo("eori")))
            .withRequestBody(matchingJsonPath("detail.mucr", equalTo("GB/123-12345")))
            .withRequestBody(matchingJsonPath("detail.ducr", equalTo("GB/321-54321")))
            .withRequestBody(matchingJsonPath("detail.submissionResult", equalTo("Success")))
        )
      }
    }
  }
}
