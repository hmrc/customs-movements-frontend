/*
 * Copyright 2022 HM Revenue & Customs
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

import controllers.routes.{LocationController, MovementDetailsController}
import forms.common.{Date, Time}
import forms.{ArrivalDetails, ConsignmentReferences, DepartureDetails, SpecificDateTimeChoice}
import models.DateTimeProvider
import models.cache.{ArrivalAnswers, DepartureAnswers, MovementAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import repository.MockCache
import views.html.specific_date_and_time

import java.time._
import scala.concurrent.ExecutionContext.Implicits.global

class SpecificDataTimeControllerSpec extends ControllerLayerSpec with MockCache with OptionValues {

  private val mockSpecificDataTimePage = mock[specific_date_and_time]
  private val mockDateTimeProvider = mock[DateTimeProvider]

  private val consignmentReferences = ConsignmentReferences("reference", "referenceValue")
  private val fixedDate = Date(LocalDate.of(2020, 6, 18))
  private val fixedTime = Time(LocalTime.of(14, 45, 18))

  private def controller(answers: MovementAnswers) =
    new SpecificDateTimeController(
      SuccessfulAuth(),
      ValidJourney(answers),
      cacheRepository,
      stubMessagesControllerComponents(),
      mockSpecificDataTimePage,
      mockDateTimeProvider,
      navigator
    )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(mockSpecificDataTimePage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockDateTimeProvider.dateNow).thenReturn(fixedDate)
    when(mockDateTimeProvider.timeNow).thenReturn(fixedTime)
  }

  override protected def afterEach(): Unit = {
    reset(mockSpecificDataTimePage, mockDateTimeProvider)
    super.afterEach()
  }

  private def theResponseForm: Form[SpecificDateTimeChoice] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[SpecificDateTimeChoice]])
    verify(mockSpecificDataTimePage).apply(captor.capture(), any())(any(), any())
    captor.getValue
  }

  "SpecificDateTimeController" should {
    "return 200 (OK)" when {
      "display page method is invoked and cache is empty" in {
        val result = controller(ArrivalAnswers(consignmentReferences = Some(consignmentReferences))).displayPage()(getRequest())

        status(result) mustBe OK
        theResponseForm.value mustBe empty
      }

      "display page method is invoked and cache contains data" when {
        "on arrival journey" in {
          val cachedData = SpecificDateTimeChoice(SpecificDateTimeChoice.CurrentDateTime)

          val answers = ArrivalAnswers(consignmentReferences = Some(consignmentReferences), specificDateTimeChoice = Some(cachedData))
          val result = controller(answers).displayPage()(getRequest())

          status(result) mustBe OK
          theResponseForm.value.value mustBe cachedData
        }

        "on departure journey" in {
          val cachedData = SpecificDateTimeChoice(SpecificDateTimeChoice.UserDateTime)

          val answers = DepartureAnswers(consignmentReferences = Some(consignmentReferences), specificDateTimeChoice = Some(cachedData))
          val result = controller(answers).displayPage()(getRequest())

          status(result) mustBe OK
          theResponseForm.value.value mustBe cachedData
        }
      }
    }

    "return 400 (BAD_REQUEST)" when {
      "form is incorrect" in {
        val incorrectForm = Json.toJson(SpecificDateTimeChoice("invalid"))

        val result = controller(ArrivalAnswers(consignmentReferences = Some(consignmentReferences))).submit()(postRequest(incorrectForm))

        status(result) mustBe BAD_REQUEST
      }
    }

    "return 303 (SEE_OTHER)" when {
      "form is correct and user date-time selected on arrival journey" in {
        val dateTimeChoice = SpecificDateTimeChoice(SpecificDateTimeChoice.UserDateTime)
        val answers = ArrivalAnswers(consignmentReferences = Some(consignmentReferences))

        val result = controller(answers).submit()(postRequest(Json.toJson(dateTimeChoice)))

        status(result) mustBe SEE_OTHER
        thePageNavigatedTo.url mustBe MovementDetailsController.displayPage().url
        theCacheUpserted.answers mustBe Some(answers.copy(specificDateTimeChoice = Some(dateTimeChoice)))
      }

      "form is correct and current date-time selected on arrival journey" in {
        val dateTimeChoice = SpecificDateTimeChoice(SpecificDateTimeChoice.CurrentDateTime)
        val answers = ArrivalAnswers(consignmentReferences = Some(consignmentReferences))

        val result =
          controller(answers).submit()(postRequest(Json.toJson(dateTimeChoice)))

        status(result) mustBe SEE_OTHER
        thePageNavigatedTo.url mustBe LocationController.displayPage().url
        theCacheUpserted.answers mustBe Some(
          answers
            .copy(specificDateTimeChoice = Some(dateTimeChoice), arrivalDetails = Some(ArrivalDetails(fixedDate, fixedTime)))
        )
      }

      "form is correct and user date-time selected on departure journey" in {
        val dateTimeChoice = SpecificDateTimeChoice(SpecificDateTimeChoice.UserDateTime)
        val answers = DepartureAnswers(consignmentReferences = Some(consignmentReferences))

        val result = controller(answers).submit()(postRequest(Json.toJson(dateTimeChoice)))

        status(result) mustBe SEE_OTHER
        thePageNavigatedTo.url mustBe MovementDetailsController.displayPage().url
        theCacheUpserted.answers mustBe Some(answers.copy(specificDateTimeChoice = Some(dateTimeChoice)))
      }

      "form is correct and current date-time selected on departure journey" in {
        val dateTimeChoice = SpecificDateTimeChoice(SpecificDateTimeChoice.CurrentDateTime)
        val answers = DepartureAnswers(consignmentReferences = Some(consignmentReferences))

        val result =
          controller(answers).submit()(postRequest(Json.toJson(dateTimeChoice)))

        status(result) mustBe SEE_OTHER
        thePageNavigatedTo.url mustBe LocationController.displayPage().url
        theCacheUpserted.answers mustBe Some(
          answers
            .copy(specificDateTimeChoice = Some(dateTimeChoice), departureDetails = Some(DepartureDetails(fixedDate, fixedTime)))
        )
      }
    }
  }
}
