/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.{LocalDate, LocalDateTime, LocalTime}

import forms.common.{Date, Time}
import forms.{ArrivalDetails, ConsignmentReferences, DepartureDetails}
import models.ReturnToStartException
import models.cache.{ArrivalAnswers, DepartureAnswers, MovementAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.libs.json._
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import repository.MockCache
import testdata.MovementsTestData
import views.html.{arrival_details, departure_details}

import scala.concurrent.ExecutionContext.global

class MovementDetailsControllerSpec extends ControllerLayerSpec with MockCache with OptionValues {

  private val mockArrivalDetailsPage = mock[arrival_details]
  private val mockDepartureDetailsPage = mock[departure_details]
  private val consignmentReferences = ConsignmentReferences("reference", "referenceValue")

  private val yesterday = LocalDateTime.now().minusDays(1)

  private def controller(answers: MovementAnswers) =
    new MovementDetailsController(
      SuccessfulAuth(),
      ValidJourney(answers),
      cache,
      stubMessagesControllerComponents(),
      MovementsTestData.movementDetails,
      mockArrivalDetailsPage,
      mockDepartureDetailsPage
    )(global)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(mockArrivalDetailsPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    when(mockDepartureDetailsPage.apply(any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
  }

  override protected def afterEach(): Unit = {
    reset(mockArrivalDetailsPage, mockDepartureDetailsPage)
    super.afterEach()
  }

  private def arrivalResponseForm: Form[ArrivalDetails] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[ArrivalDetails]])
    verify(mockArrivalDetailsPage).apply(captor.capture(), any())(any(), any())
    captor.getValue
  }

  private def departureResponseForm: Form[DepartureDetails] = {
    val captor = ArgumentCaptor.forClass(classOf[Form[DepartureDetails]])
    verify(mockDepartureDetailsPage).apply(captor.capture(), any())(any(), any())
    captor.getValue
  }

  "Movement Details Controller for arrival journey" should {

    "displayPage is called" should {

      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {

          val answers = ArrivalAnswers(consignmentReferences = Some(consignmentReferences))

          val result = controller(answers).displayPage()(getRequest())

          status(result) mustBe OK
          arrivalResponseForm.value mustBe empty
        }

        "display page method is invoked and cache contains data" in {

          val cachedData = ArrivalDetails(Date(LocalDate.of(2019, 2, 10)), Time(LocalTime.of(10, 10)))
          val answers = ArrivalAnswers(arrivalDetails = Some(cachedData), consignmentReferences = Some(consignmentReferences))

          val result = controller(answers).displayPage()(getRequest())

          status(result) mustBe OK
          arrivalResponseForm.value.value mustBe cachedData
        }
      }

      "return to start" when {

        "consignment reference is missing" in {

          intercept[RuntimeException] {
            await(controller(ArrivalAnswers()).displayPage()(getRequest()))
          } mustBe ReturnToStartException
        }
      }
    }

    "saveMovementDetails is called" should {

      "return 303 (SEE_OTHER) and redirect to location page" when {

        "form is correct" in {

          val correctForm = Json.obj(
            "dateOfArrival" -> Json.obj("day" -> yesterday.getDayOfMonth, "month" -> yesterday.getMonthValue, "year" -> yesterday.getYear),
            "timeOfArrival" -> Json.obj("hour" -> yesterday.getHour, "minute" -> yesterday.getMinute)
          )
          val answers = ArrivalAnswers(consignmentReferences = Some(consignmentReferences))

          val result = controller(answers).saveMovementDetails()(postRequest(correctForm))

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.LocationController.displayPage().url
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "form is incorrect" in {

          val incorrectForm: JsValue = JsObject(Map("dateOfArrival" -> JsString("")))
          val answers = ArrivalAnswers(consignmentReferences = Some(consignmentReferences))

          val result = controller(answers).saveMovementDetails()(postRequest(incorrectForm))

          status(result) mustBe BAD_REQUEST
        }
      }
    }
  }

  "Movement Details Controller for departure journey" when {

    "displayPage is called" should {

      "return 200 (OK)" when {

        "display page method is invoked and cache is empty" in {

          val answers = DepartureAnswers(consignmentReferences = Some(consignmentReferences))

          val result = controller(answers).displayPage()(getRequest())

          status(result) mustBe OK
          departureResponseForm.value mustBe empty
        }

        "display page method is invoked and cache contains data" in {

          val cachedData = DepartureDetails(Date(LocalDate.of(2019, 2, 10)), Time(LocalTime.now()))
          val answers = DepartureAnswers(departureDetails = Some(cachedData), consignmentReferences = Some(consignmentReferences))

          val result = controller(answers).displayPage()(getRequest())

          status(result) mustBe OK
          departureResponseForm.value.value mustBe cachedData
        }
      }

      "return to start" when {

        "consignment reference is missing" in {

          intercept[RuntimeException] {
            await(controller(DepartureAnswers()).displayPage()(getRequest()))
          } mustBe ReturnToStartException
        }
      }
    }

    "saveMovementDetails is called" should {

      "return 303 (SEE_OTHER) and redirect to location page" when {

        "form is correct" in {

          val correctForm = Json.obj(
            "dateOfDeparture" -> Json.obj("day" -> yesterday.getDayOfMonth, "month" -> yesterday.getMonthValue, "year" -> yesterday.getYear),
            "timeOfDeparture" -> Json.obj("hour" -> yesterday.getHour, "minute" -> yesterday.getMinute)
          )
          val answers = DepartureAnswers(consignmentReferences = Some(consignmentReferences))

          val result = controller(answers).saveMovementDetails()(postRequest(correctForm))

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.LocationController.displayPage().url
        }
      }

      "return 400 (BAD_REQUEST)" when {

        "form is incorrect" in {

          val incorrectForm: JsValue = JsObject(Map("dateOfDeparture" -> JsString("")))
          val answers = DepartureAnswers(consignmentReferences = Some(consignmentReferences))

          val result = controller(answers).saveMovementDetails()(postRequest(incorrectForm))

          status(result) mustBe BAD_REQUEST
        }
      }
    }
  }

}
