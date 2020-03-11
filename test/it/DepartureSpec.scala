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

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime, LocalTime, ZoneOffset}

import com.github.tomakehurst.wiremock.client.WireMock.{equalTo, equalToJson, matchingJsonPath, verify}
import controllers.exception.InvalidFeatureStateException
import forms._
import forms.common.{Date, Time}
import models.cache.DepartureAnswers
import play.api.test.Helpers._

class DepartureSpec extends IntegrationSpec {

  private val date = LocalDate.now()
  private val time = LocalTime.now().truncatedTo(ChronoUnit.MINUTES)
  private val datetime = LocalDateTime.of(date, time).toInstant(ZoneOffset.UTC)

  "Consignment References Page" when {
    "GET" should {
      "throw feature disabled" in {
        // Given
        givenAuthSuccess("eori")
        givenCacheFor("eori", DepartureAnswers())

        // When
        val response = get(controllers.routes.ConsignmentReferencesController.displayPage())

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
        givenCacheFor("eori", DepartureAnswers())

        // When
        val response =
          post(controllers.routes.ConsignmentReferencesController.saveConsignmentReferences(), "reference" -> "M", "mucrValue" -> "GB/123-12345")

        // Then
        intercept[InvalidFeatureStateException] {
          await(response)
        }
      }
    }
  }

  "Specific Date/Time Page" when {
    "GET" should {
      "return 200" in {
        // Given
        givenAuthSuccess("eori")
        givenCacheFor("eori", DepartureAnswers(consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345"))))

        // When
        val response = get(controllers.routes.SpecificDateTimeController.displayPage())

        // Then
        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" when {
        "user elects to enter date time" in {
          // Given
          givenAuthSuccess("eori")
          givenCacheFor("eori", DepartureAnswers(consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345"))))

          // When
          val response = post(controllers.routes.SpecificDateTimeController.submit(), "choice" -> SpecificDateTimeChoice.UserDateTime)

          // Then
          status(response) mustBe SEE_OTHER
          redirectLocation(response) mustBe Some(controllers.routes.MovementDetailsController.displayPage().url)
          theAnswersFor("eori") mustBe Some(
            DepartureAnswers(
              consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345")),
              specificDateTimeChoice = Some(SpecificDateTimeChoice(SpecificDateTimeChoice.UserDateTime))
            )
          )
        }
        "user elects to current date time" in {
          // Given
          givenAuthSuccess("eori")
          givenCacheFor("eori", DepartureAnswers(consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345"))))

          // When
          val response = post(controllers.routes.SpecificDateTimeController.submit(), "choice" -> SpecificDateTimeChoice.CurrentDateTime)

          // Then
          status(response) mustBe SEE_OTHER
          redirectLocation(response) mustBe Some(controllers.routes.LocationController.displayPage().url)
          theAnswersFor("eori") mustBe Some(
            DepartureAnswers(
              consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345")),
              departureDetails = Some(DepartureDetails(dateTimeProvider.dateNow, dateTimeProvider.timeNow)),
              specificDateTimeChoice = Some(SpecificDateTimeChoice(SpecificDateTimeChoice.CurrentDateTime))
            )
          )
        }
      }
    }
  }

  "Movement Details Page" when {
    "GET" should {
      "return 200" in {
        // Given
        givenAuthSuccess("eori")
        givenCacheFor("eori", DepartureAnswers(consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345"))))

        // When
        val response = get(controllers.routes.MovementDetailsController.displayPage())

        // Then
        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        // Given
        givenAuthSuccess("eori")
        givenCacheFor("eori", DepartureAnswers(consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345"))))

        // When
        val response = post(
          controllers.routes.MovementDetailsController.saveMovementDetails(),
          "dateOfDeparture.day" -> date.getDayOfMonth.toString,
          "dateOfDeparture.month" -> date.getMonthValue.toString,
          "dateOfDeparture.year" -> date.getYear.toString,
          "timeOfDeparture.hour" -> time.getHour.toString,
          "timeOfDeparture.minute" -> time.getMinute.toString
        )

        // Then
        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.routes.LocationController.displayPage().url)
        theAnswersFor("eori") mustBe Some(
          DepartureAnswers(
            consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345")),
            departureDetails = Some(DepartureDetails(Date(date), Time(time)))
          )
        )
      }
    }
  }

  "Location Page" when {
    "GET" should {
      "return 200" in {
        // Given
        givenAuthSuccess("eori")
        givenCacheFor(
          "eori",
          DepartureAnswers(
            consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345")),
            departureDetails = Some(DepartureDetails(Date(date), Time(time)))
          )
        )

        // When
        val response = get(controllers.routes.LocationController.displayPage())

        // Then
        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        // Given
        givenAuthSuccess("eori")
        givenCacheFor(
          "eori",
          DepartureAnswers(
            consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345")),
            departureDetails = Some(DepartureDetails(Date(date), Time(time)))
          )
        )

        // When
        val response = post(controllers.routes.LocationController.saveLocation(), "code" -> "GBAUEMAEMAEMA")

        // Then
        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.routes.TransportController.displayPage().url)
        theAnswersFor("eori") mustBe Some(
          DepartureAnswers(
            consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345")),
            departureDetails = Some(DepartureDetails(Date(date), Time(time))),
            location = Some(Location("GBAUEMAEMAEMA"))
          )
        )
      }
    }
  }

  "Transport Page" when {
    "GET" should {
      "return 200" in {
        // Given
        givenAuthSuccess("eori")
        givenCacheFor(
          "eori",
          DepartureAnswers(
            consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345")),
            departureDetails = Some(DepartureDetails(Date(date), Time(time))),
            location = Some(Location("GBAUEMAEMAEMA"))
          )
        )

        // When
        val response = get(controllers.routes.TransportController.displayPage())

        // TThen
        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        // Given
        givenAuthSuccess("eori")
        givenCacheFor(
          "eori",
          DepartureAnswers(
            consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345")),
            departureDetails = Some(DepartureDetails(Date(date), Time(time))),
            location = Some(Location("GBAUEMAEMAEMA"))
          )
        )

        // When
        val response =
          post(controllers.routes.TransportController.saveTransport(), "modeOfTransport" -> "1", "nationality" -> "FR", "transportId" -> "123")

        // Then
        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.routes.SummaryController.displayPage().url)
        theAnswersFor("eori") mustBe Some(
          DepartureAnswers(
            consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345")),
            departureDetails = Some(DepartureDetails(Date(date), Time(time))),
            location = Some(Location("GBAUEMAEMAEMA")),
            transport = Some(Transport("1", "123", "FR"))
          )
        )
      }
    }
  }

  "Movement Summary Page" when {
    "GET" should {
      "return 200" in {
        // Given
        givenAuthSuccess("eori")
        givenCacheFor(
          "eori",
          DepartureAnswers(
            consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345")),
            departureDetails = Some(DepartureDetails(Date(date), Time(time))),
            location = Some(Location("GBAUEMAEMAEMA")),
            transport = Some(Transport("1", "123", "FR"))
          )
        )

        // When
        val response = get(controllers.routes.SummaryController.displayPage())

        // Then
        status(response) mustBe OK
      }
    }

    "POST" should {
      "continue" in {
        // Given
        givenAuthSuccess("eori")
        givenCacheFor(
          "eori",
          DepartureAnswers(
            consignmentReferences = Some(ConsignmentReferences("M", "GB/123-12345")),
            departureDetails = Some(DepartureDetails(Date(date), Time(time))),
            location = Some(Location("GBAUEMAEMAEMA")),
            transport = Some(Transport("1", "123", "FR"))
          )
        )
        givenTheMovementsBackendAcceptsTheMovement()

        // When
        val response = post(controllers.routes.SummaryController.submitMovementRequest())

        // Then
        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.routes.MovementConfirmationController.displayPage().url)
        theAnswersFor("eori") mustBe None
        verify(
          postRequestedForMovement()
            .withRequestBody(equalToJson(s"""{
                   |"eori":"eori",
                   |"choice":"Departure",
                   |"consignmentReference":{"reference":"M","referenceValue":"GB/123-12345"},
                   |"movementDetails":{"dateTime":"$datetime"},
                   |"location":{"code":"GBAUEMAEMAEMA"},
                   |"transport":{"modeOfTransport":"1","transportId":"123", "nationality":"FR"}
                   |}""".stripMargin))
        )

        val expectedTimeFormatted = time.format(DateTimeFormatter.ISO_TIME)
        val submissionPayloadRequestBuilder = postRequestedForAudit()
          .withRequestBody(matchingJsonPath("auditType", equalTo("departure")))
          .withRequestBody(matchingJsonPath("detail.eori", equalTo("eori")))
          .withRequestBody(matchingJsonPath("detail.MovementDetails.dateOfDeparture.date", equalTo(date.toString)))
          .withRequestBody(matchingJsonPath("detail.MovementDetails.timeOfDeparture.time", equalTo(expectedTimeFormatted)))
          .withRequestBody(matchingJsonPath("detail.Transport.modeOfTransport", equalTo("1")))
          .withRequestBody(matchingJsonPath("detail.Transport.transportId", equalTo("123")))
          .withRequestBody(matchingJsonPath("detail.Transport.nationality", equalTo("FR")))
          .withRequestBody(matchingJsonPath("detail.ConsignmentReferences.reference", equalTo("M")))
          .withRequestBody(matchingJsonPath("detail.ConsignmentReferences.referenceValue", equalTo("GB/123-12345")))
          .withRequestBody(matchingJsonPath("detail.Location.code", equalTo("GBAUEMAEMAEMA")))

        val submissionResultRequestBuilder = postRequestedForAudit()
          .withRequestBody(matchingJsonPath("auditType", equalTo("departure")))
          .withRequestBody(matchingJsonPath("detail.eori", equalTo("eori")))
          .withRequestBody(matchingJsonPath("detail.ucr", equalTo("GB/123-12345")))
          .withRequestBody(matchingJsonPath("detail.ucrType", equalTo("M")))
          .withRequestBody(matchingJsonPath("detail.messageCode", equalTo("EDL")))
          .withRequestBody(matchingJsonPath("detail.submissionResult", equalTo("Success")))

        verifyEventually(submissionPayloadRequestBuilder)
        verifyEventually(submissionResultRequestBuilder)
      }
    }
  }
}
