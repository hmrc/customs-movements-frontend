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
import controllers.consolidations.routes.{AssociateUcrController, ManageMucrController, MucrOptionsController}
import controllers.summary.routes.{AssociateUcrSummaryController, MovementConfirmationController}
import forms._
import models.UcrBlock
import models.cache.{AssociateUcrAnswers, Cache}
import play.api.test.Helpers._

class AssociateUcrISpec extends IntegrationSpec {

  "Manage Mucr Page" when {

    "GET" should {

      "return 200 when queried mucr" in {
        givenAuthSuccess("eori")
        val answerUuid = givenCacheFor(Cache("eori", AssociateUcrAnswers(), UcrBlock("mucr", UcrType.Mucr), false))

        val response = get(ManageMucrController.displayPage, answerUuid)

        status(response) mustBe OK
      }

      "return 303 when queried ducr" in {
        givenAuthSuccess("eori")
        val answerUuid = givenCacheFor(Cache("eori", AssociateUcrAnswers(), UcrBlock("ducr", UcrType.Ducr), false))

        val response = get(ManageMucrController.displayPage, answerUuid)

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(MucrOptionsController.displayPage.url)
      }
    }

    "POST" should {

      "continue for associate this mucr" in {
        givenAuthSuccess("eori")
        val answerUuid = givenCacheFor("eori", AssociateUcrAnswers())

        val response = post(ManageMucrController.submit, answerUuid, "choice" -> ManageMucrChoice.AssociateThisMucr)

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(MucrOptionsController.displayPage.url)
        theAnswersFor("eori") mustBe Some(AssociateUcrAnswers(manageMucrChoice = Some(ManageMucrChoice(ManageMucrChoice.AssociateThisMucr))))
      }

      "continue for associate another mucr" in {
        givenAuthSuccess("eori")
        val answerUuid = givenCacheFor("eori", AssociateUcrAnswers())

        val response = post(ManageMucrController.submit, answerUuid, "choice" -> ManageMucrChoice.AssociateAnotherMucr)

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(AssociateUcrController.displayPage.url)
        theAnswersFor("eori") mustBe Some(AssociateUcrAnswers(manageMucrChoice = Some(ManageMucrChoice(ManageMucrChoice.AssociateAnotherMucr))))
      }
    }
  }

  "UCR Options Page" when {

    "GET" should {
      "return 200" in {
        givenAuthSuccess("eori")
        val answerUuid = givenCacheFor("eori", AssociateUcrAnswers())

        val response = get(MucrOptionsController.displayPage, answerUuid)

        status(response) mustBe OK
      }
    }

    "POST" should {

      "continue" in {
        givenAuthSuccess("eori")
        val answerUuid = givenCacheFor("eori", AssociateUcrAnswers(), UcrBlock("8GB123457359100-TEST0002", UcrType.Ducr))

        val response =
          post(
            MucrOptionsController.save,
            answerUuid,
            "createOrAdd" -> "create",
            "newMucr" -> "GB/82F9-0N2F6500040010TO120P0A30068",
            "existingMucr" -> ""
          )

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(AssociateUcrController.displayPage.url)
        theAnswersFor("eori") mustBe Some(
          AssociateUcrAnswers(
            mucrOptions = Some(MucrOptions(createOrAdd = "create", mucr = "GB/82F9-0N2F6500040010TO120P0A30068")),
            associateUcr = None,
            readyToSubmit = Some(false)
          )
        )
      }
    }
  }

  "Associate UCR Page" when {

    "GET" should {
      "return 200" in {
        givenAuthSuccess("eori")
        val answerUuid = givenCacheFor("eori", AssociateUcrAnswers(mucrOptions = Some(MucrOptions(createOrAdd = "create", mucr = "GB/123-12345"))))

        val response = get(AssociateUcrController.displayPage, answerUuid)

        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        givenAuthSuccess("eori")
        val answerUuid = givenCacheFor("eori", AssociateUcrAnswers(mucrOptions = Some(MucrOptions(createOrAdd = "create", mucr = "GB/123-12345"))))

        val response = post(AssociateUcrController.submit, answerUuid, "kind" -> "mucr", "mucr" -> "GB/321-54321")

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(AssociateUcrSummaryController.displayPage.url)
        theAnswersFor("eori") mustBe Some(
          AssociateUcrAnswers(
            mucrOptions = Some(MucrOptions(createOrAdd = "create", mucr = "GB/123-12345")),
            associateUcr = Some(AssociateUcr(UcrType.Mucr, "GB/321-54321")),
            readyToSubmit = Some(true)
          )
        )
      }
    }
  }

  "Associate UCR Summary Page" when {

    "GET" should {
      "return 200" in {
        givenAuthSuccess("eori")
        val answerUuid = givenCacheFor(
          "eori",
          AssociateUcrAnswers(
            mucrOptions = Some(MucrOptions(createOrAdd = "create", mucr = "GB/123-12345")),
            associateUcr = Some(AssociateUcr(UcrType.Mucr, "GB/321-54321"))
          )
        )

        val response = get(AssociateUcrSummaryController.displayPage, answerUuid)

        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        givenAuthSuccess("eori")
        val answerUuid = givenCacheFor(
          "eori",
          AssociateUcrAnswers(
            mucrOptions = Some(MucrOptions(createOrAdd = "create", mucr = "GB/123-12345")),
            associateUcr = Some(AssociateUcr(UcrType.Mucr, "GB/321-54321"))
          )
        )
        givenMovementsBackendAcceptsTheConsolidation()

        val response = post(AssociateUcrSummaryController.submit, answerUuid)

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(MovementConfirmationController.displayPage.url)
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
